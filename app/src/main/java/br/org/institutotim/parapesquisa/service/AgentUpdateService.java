package br.org.institutotim.parapesquisa.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.util.Pair;
import android.util.SparseArray;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.ParaPesquisaApp;
import br.org.institutotim.parapesquisa.data.api.ParaPesquisaApi;
import br.org.institutotim.parapesquisa.data.api.request.RescheduleRequest;
import br.org.institutotim.parapesquisa.data.api.request.SubmissionUpdate;
import br.org.institutotim.parapesquisa.data.api.response.ApiError;
import br.org.institutotim.parapesquisa.data.api.response.PaginatedResponse;
import br.org.institutotim.parapesquisa.data.api.response.SingleResponse;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.event.SyncCompletedEvent;
import br.org.institutotim.parapesquisa.data.model.AboutText;
import br.org.institutotim.parapesquisa.data.model.Submission;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.Summary;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserForm;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.helper.NotificationHelper;
import br.org.institutotim.parapesquisa.ui.helper.SummaryHelper;
import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import timber.log.Timber;

public class AgentUpdateService extends IntentService {

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
    @Inject
    ParaPesquisaOpenHelper paraPesquisaOpenHelper;

    UserData user;

    public AgentUpdateService() {
        super("AgentUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ParaPesquisaApp.get(this).getComponent().inject(this);
        user = preferences.getUser();

        try {
            SparseArray<Summary> oldSummaries = mSummaryHelper.generateSummaries();

            sendPendingSubmissions();
            updateUserData();
            List<UserForm> forms = retrieveForms();
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

    private Long getSubmissionId(final UserSubmission submission) {
        if (submission.getId() == null || submission.getId() == 0 || paraPesquisaOpenHelper.hasTemp(submission.getId()))
            return null;
        return submission.getId();
    }

    private void sendPendingSubmissions() {
        List<UserSubmission> submissions = helper.getPendingSubmissions();
        Long submissionId;
        for (int i = 0; i < submissions.size(); i++) {
            UserSubmission submission = submissions.get(i);
            submissionId = submission.getId();
            long realSubmissionId = -1;
            if (submission.getStatus() == null || submission.getStatus().equals(SubmissionStatus.WAITING_CORRECTION)) {
                submission = submission.updateStatus(SubmissionStatus.WAITING_APPROVAL);
            }
            if (getSubmissionId(submission) == null) {
                realSubmissionId = api.sendSubmission(submission.getFormId(), Submission.builder()
                        .id(getSubmissionId(submission))
                        .answers(submission.getAnswers())
                        .startedAt(submission.getLatestLog() != null ? submission.getLatestLog().getWhen() : DateTime.now())
                        .build()).getResponse().getId();
                submission = submission.newInstanceWithId(realSubmissionId);
            } else {
                SubmissionUpdate update = new SubmissionUpdate();
                update.setAnswers(submission.getAnswers());
                update.setStatus(submission.getStatus());
                api.updateSubmission(submission.getFormId(), getSubmissionId(submission), update);
            }

            switch (submission.getStatus()) {
                case CANCELLED:
                    long reasonId = helper.getCancelReasonId(submissionId) == -1 ? helper.getCancelReasonId(realSubmissionId) : helper.getCancelReasonId(submissionId);
                    api.reschedule(submission.getFormId(), getSubmissionId(submission), new RescheduleRequest(reasonId));
                    break;
                case RESCHEDULED:
                    Pair<DateTime, Long> pair = helper.getRescheduleReasonBySubmissionId(submissionId) == null ?
                            helper.getRescheduleReasonBySubmissionId(realSubmissionId) : helper.getRescheduleReasonBySubmissionId(submissionId);
                    api.reschedule(submission.getFormId(), getSubmissionId(submission), new RescheduleRequest(pair.first, pair.second));
                    break;
            }
            helper.removePendingSubmission(submissionId);
        }
        helper.clearPendingSubmissions();
    }


    private void updateUserData() {
        SingleResponse<UserData> response = api.getUserData(user.getId());
        preferences.setUser(response.getResponse());
        user = response.getResponse();
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

    private void retrieveSubmissions(List<UserForm> forms) {
        List<UserSubmission> submissions = new ArrayList<>();
        for (int i = 0; i < forms.size(); i++) {
            UserForm form = forms.get(i);
            PaginatedResponse<UserSubmission> response = api.getUserSubmissions(form.getFormId(), user.getId());
            submissions.addAll(response.getResponse());
            if (response.getPagination() != null) {
                while (response.getPagination().getNext() != null) {
                    response = api.getUserSubmissions(form.getFormId(), user.getId(), response.getPagination().getNext());
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
