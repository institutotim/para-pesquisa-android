package br.org.institutotim.parapesquisa.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.ParaPesquisaApp;
import br.org.institutotim.parapesquisa.data.api.ParaPesquisaApi;
import br.org.institutotim.parapesquisa.data.api.request.Moderation;
import br.org.institutotim.parapesquisa.data.api.request.SubmissionUpdate;
import br.org.institutotim.parapesquisa.data.api.response.ApiError;
import br.org.institutotim.parapesquisa.data.api.response.PaginatedResponse;
import br.org.institutotim.parapesquisa.data.api.response.SingleResponse;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.event.SyncCompletedEvent;
import br.org.institutotim.parapesquisa.data.model.AboutText;
import br.org.institutotim.parapesquisa.data.model.Attribution;
import br.org.institutotim.parapesquisa.data.model.AttributionTransfer;
import br.org.institutotim.parapesquisa.data.model.ModerationAction;
import br.org.institutotim.parapesquisa.data.model.Summary;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserForm;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.helper.NotificationHelper;
import br.org.institutotim.parapesquisa.ui.helper.SummaryHelper;
import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import timber.log.Timber;

public class ModeratorUpdateService extends IntentService {

    @Inject
    ParaPesquisaApi api;
    @Inject
    ParaPesquisaPreferences preferences;
    @Inject
    ParaPesquisaOpenHelper helper;
    @Inject
    SummaryHelper mSummaryHelper;
    @Inject
    NotificationHelper mNotificationHelper;

    UserData user;

    public ModeratorUpdateService() {
        super("ModeratorUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ParaPesquisaApp.get(this).getComponent().inject(this);
        user = preferences.getUser();

        try {
            SparseArray<Summary> oldSummaries = mSummaryHelper.generateSummaries();

            updateUserData();
            sendTransfers();
            sendSubmissions();
            List<UserForm> forms = retrieveForms();
            retrieveAttributions();
            retrieveSubmissions(forms);
            retrieveAboutText();

            SparseArray<Summary> newSummaries = mSummaryHelper.generateSummaries();
            //helper.clearNotifications();
            helper.saveNotifications(mNotificationHelper.checkNotifications(this, oldSummaries, newSummaries));

            EventBus.getDefault().post(new SyncCompletedEvent(SyncCompletedEvent.Result.SUCCESS));
        } catch (Exception e) {
            if (e instanceof RetrofitError) {
                try {
                    ApiError json = (ApiError) ((RetrofitError) e).getBodyAs(ApiError.class);
                    EventBus.getDefault().post(new SyncCompletedEvent(SyncCompletedEvent.Result.ERROR, json.getErrorDescription()));
                } catch (Exception ex) {
                    EventBus.getDefault().post(new SyncCompletedEvent(SyncCompletedEvent.Result.ERROR, e.getMessage() != null ? e.getMessage() :
                            "Failed to complete sync"));
                }
                Timber.e(e, "Failed to complete sync");
            } else {
                Timber.e(e, "Failed to complete sync");
                EventBus.getDefault().post(new SyncCompletedEvent(SyncCompletedEvent.Result.ERROR, e.getMessage() != null ? e.getMessage() :
                        "Failed to complete sync"));
            }

            if (intent.getBooleanExtra("sign_in", false)) {
                helper.clearAll();
                preferences.clearSession();
            }
        }

        stopSelf();
    }

    private void updateUserData() {
        SingleResponse<UserData> response = api.getUserData(user.getId());
        preferences.setUser(response.getResponse());
        user = response.getResponse();
    }

    private void sendTransfers() {
        List<AttributionTransfer> transfers = helper.getTransfers();
        for (int i = 0; i < transfers.size(); i++) {
            api.transferAssignment(transfers.get(i));
        }
        helper.clearTransfers();
    }

    private void sendSubmissions() {
        List<UserSubmission> approvedSubmissions = helper.getApprovedSubmissions();
        for (int i = 0; i < approvedSubmissions.size(); i++) {
            UserSubmission submission = approvedSubmissions.get(i);
            api.moderate(submission.getFormId(), submission.getId(), new Moderation(ModerationAction.APPROVED));
        }
        helper.removeApprovedSubmissions();

        List<UserSubmission> reprovedSubmissions = helper.getRepprovedSubmissions();
        for (int i = 0; i < reprovedSubmissions.size(); i++) {
            UserSubmission submission = reprovedSubmissions.get(i);
            api.moderate(submission.getFormId(), submission.getId(), new Moderation(ModerationAction.REJECTED));
        }
        helper.removeRepprovedSubmissions();

        List<UserSubmission> pendingSubmission = helper.getPendingSubmissions();
        for (int i = 0; i < pendingSubmission.size(); i++) {
            UserSubmission submission = pendingSubmission.get(i);
            SubmissionUpdate update = new SubmissionUpdate();
            update.setCorrections(submission.getCorrections());
            update.setStatus(submission.getStatus());
            api.updateSubmission(submission.getFormId(), submission.getId(), update);
        }
        helper.removePendingSubmissions();
    }

    private List<UserForm> retrieveForms() {
        PaginatedResponse<UserForm> response = api.getUserForms(user.getId());
        List<UserForm> forms = new ArrayList<>(response.getResponse());
        if (response.getPagination() != null) {
            while (response.getPagination().getNext() != null) {
                response = api.getUserForms(user.getId(), response.getPagination().getNext());
                forms.addAll(response.getResponse());
                if (response.getPagination() == null) {
                    break;
                }
            }
        }
        helper.deleteUserForms();
        helper.saveUserForms(forms);

        return forms;
    }

    private void retrieveAttributions() {
        PaginatedResponse<Attribution> attributions = api.getAttributions(user.getId());
        helper.deleteAttributions();
        helper.saveAttributions(attributions.getResponse());
    }

    private void retrieveSubmissions(List<UserForm> forms) {
        List<UserSubmission> submissions = new ArrayList<>();
        for (int i = 0; i < forms.size(); i++) {
            UserForm form = forms.get(i);
            PaginatedResponse<UserSubmission> response = api.getSubmissions(user.getId(), form.getFormId());
            submissions.addAll(response.getResponse());
            if (response.getPagination() != null) {
                while (response.getPagination().getNext() != null) {
                    response = api.getSubmissions(user.getId(), form.getFormId(), response.getPagination().getNext());
                    submissions.addAll(response.getResponse());
                    if (response.getPagination() == null) {
                        break;
                    }
                }
            }

        }
        helper.deleteSubmissions();
        helper.saveSubmissions(submissions);
    }

    private void retrieveAboutText() {
        PaginatedResponse<AboutText> texts = api.getAboutText();
        helper.deleteAboutText();
        helper.saveAboutText(texts.getResponse());
    }
}
