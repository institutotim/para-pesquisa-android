package br.org.institutotim.parapesquisa.ui.helper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.model.Notification;
import br.org.institutotim.parapesquisa.data.model.Summary;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserRole;
import br.org.institutotim.parapesquisa.ui.adapter.NotificationAdapter;
import br.org.institutotim.parapesquisa.util.DateUtils;

public class NotificationHelper {

    private final ParaPesquisaPreferences mPreferences;
    private final ParaPesquisaOpenHelper mHelper;
    private final SummaryHelper mSummaryHelper;

    public NotificationHelper(ParaPesquisaOpenHelper helper, ParaPesquisaPreferences preferences, SummaryHelper summaryHelper) {
        this.mPreferences = preferences;
        this.mHelper = helper;
        this.mSummaryHelper = summaryHelper;
    }

    public List<Notification> checkNotifications(Context context, SparseArray<Summary> oldData, SparseArray<Summary> newData) {
        UserData user = mPreferences.getUser();
        if (user != null && user.getRole().equals(UserRole.AGENT)) {
            return checkNotificationsForSurveyTaker(context, oldData, newData);
        }

        return checkNotificationsForModerator(context, oldData, newData);
    }

    private List<Notification> checkNotificationsForModerator(Context context, SparseArray<Summary> oldData, SparseArray<Summary> newData) {

        List<Notification> notifications = new ArrayList<>();

        checkNewForms(context, oldData, newData, notifications);

        for (int i = 0; i < newData.size(); i++) {
            Summary summary = newData.get(newData.keyAt(i));
            Summary oldSummary = oldData.get(newData.keyAt(i));
            if (oldSummary != null) {
                checkWaitingApproval(context, newData.keyAt(i), summary, oldSummary, notifications);
            }
        }

        return notifications;
    }

    private void checkWaitingApproval(Context context, long formId, Summary newSummary, Summary oldSummary, List<Notification> notifications) {
        if (newSummary.getWaitingApproval() != oldSummary.getWaitingApproval()) {
            notifications.add(Notification.builder()
                    .date(DateTime.now())
                    .message(context.getResources().getQuantityString(R.plurals.waiting_approval_submissions,
                            newSummary.getWaitingApproval(), newSummary.getWaitingApproval(), mHelper.getFormName(formId)))
                    .build());
        }
    }

    private List<Notification> checkNotificationsForSurveyTaker(Context context, SparseArray<Summary> oldData, SparseArray<Summary> newData) {

        List<Notification> notifications = new ArrayList<>();

        checkNewForms(context, oldData, newData, notifications);

        for (int i = 0; i < newData.size(); i++) {
            Summary summary = newData.get(newData.keyAt(i));
            Summary oldSummary = oldData.get(newData.keyAt(i));
            if (oldSummary != null) {
                checkApprovedSubmissions(context, newData.keyAt(i), summary, oldSummary, notifications);
                checkDate(context, newData.keyAt(i), summary, oldSummary, notifications);
                checkQuota(context, newData.keyAt(i), summary, oldSummary, notifications);
                checkWaitingCorrection(context, newData.keyAt(i), summary, oldSummary, notifications);
                checkReproved(context, newData.keyAt(i), summary, oldSummary, notifications);
            }
        }

        return notifications;
    }

    private void checkReproved(Context context, long formId, Summary newSummary, Summary oldSummary, List<Notification> notifications) {
        mSummaryHelper.setRepproved(oldSummary, newSummary);
        if (newSummary.getRepproved() != null && newSummary.getRepproved() > 0) {
            notifications.add(Notification.builder()
                    .date(DateTime.now())
                    .icon(0)
                    .message(context.getResources().getQuantityString(R.plurals.reproved_submissions,
                            newSummary.getRepproved(), newSummary.getRepproved(), mHelper.getFormName(formId)))
                    .build());
        }
    }

    private void checkWaitingCorrection(Context context, long formId, Summary newSummary, Summary oldSummary, List<Notification> notifications) {
        if (newSummary.getWaitingCorrection() != oldSummary.getWaitingCorrection()) {
            notifications.add(Notification.builder()
                    .date(DateTime.now())
                    .icon(0)
                    .message(context.getResources().getQuantityString(R.plurals.waiting_correction_submissions,
                            newSummary.getWaitingCorrection(), newSummary.getWaitingCorrection(), mHelper.getFormName(formId)))
                    .build());
        }
    }

    private void checkQuota(Context context, long formId, Summary newSummary, Summary oldSummary, List<Notification> notifications) {
        if (newSummary.getQuota() != oldSummary.getQuota()) {
            notifications.add(Notification.builder()
                    .date(DateTime.now())
                    .icon(0)
                    .message(context.getString(R.string.message_quota_changed,
                            mHelper.getFormName(formId), newSummary.getQuota()))
                    .build());
        }
    }

    private void checkDate(Context context, long formId, Summary newSummary, Summary oldSummary, List<Notification> notifications) {
        if (!newSummary.getDate().isEqual(oldSummary.getDate())) {
            notifications.add(Notification.builder()
                    .date(DateTime.now())
                    .icon(0)
                    .message(context.getString(R.string.message_date_changed,
                            mHelper.getFormName(formId), DateUtils.getShortDateInstanceWithoutYears().print(newSummary.getDate())))
                    .build());
        }
    }

    private void checkApprovedSubmissions(Context context, long formId, Summary newSummary, Summary oldSummary, List<Notification> notifications) {
        if (newSummary.getApproved() > oldSummary.getApproved()) {
            int value = newSummary.getApproved() - oldSummary.getApproved();

            notifications.add(Notification.builder()
                    .date(DateTime.now())
                    .icon(0)
                    .message(context.getResources().getQuantityString(R.plurals.approved_submissions,
                            value, value, mHelper.getFormName(formId)))
                    .build());
        }
    }

    private void checkNewForms(Context context, SparseArray<Summary> oldData, SparseArray<Summary> newData, List<Notification> notifications) {
        if (oldData.size() > newData.size()) {
            int value = oldData.size() - newData.size();
            notifications.add(Notification.builder()
                    .date(DateTime.now())
                    .icon(0)
                    .message(context.getResources().getQuantityString(R.plurals.forms_removed,
                            value, value))
                    .build());
        } else if (oldData.size() < newData.size()) {
            int value = newData.size() - oldData.size();
            notifications.add(Notification.builder()
                    .date(DateTime.now())
                    .icon(0)
                    .message(context.getResources().getQuantityString(R.plurals.new_forms_assigned,
                            value, value))
                    .build());
        }
    }

    public void setUpNotifications(TextView counter, RecyclerView list, View button, TextView message) {
        List<Notification> notifications = mHelper.getNotifications();
        if (notifications.isEmpty()) {
            counter.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);
        } else {
            counter.setVisibility(View.VISIBLE);
            counter.setText(String.valueOf(notifications.size()));
            list.setVisibility(View.VISIBLE);
            list.setAdapter(new NotificationAdapter(notifications));
            button.setVisibility(View.VISIBLE);
            message.setVisibility(View.GONE);
        }

    }
}
