package br.org.institutotim.parapesquisa.ui.helper;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.HashSet;
import java.util.Set;

import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.model.Attribution;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.GlobalStats;
import br.org.institutotim.parapesquisa.data.model.Stats;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.UserForm;

public class StatsHelper {

    private final ParaPesquisaOpenHelper mHelper;

    public StatsHelper(ParaPesquisaOpenHelper helper) {
        this.mHelper = helper;
    }

    public GlobalStats getGlobalStats() {
        GlobalStats stats = new GlobalStats();

        int approved = 0, assigned = 0, cancelled = 0, pendingApproval = 0, pendingCorrection = 0, rescheduled = 0, done = 0;
        for (UserForm form : mHelper.getUserForms()) {
            approved += mHelper.getSubmissionsCount(form.getFormId(), SubmissionStatus.APPROVED) +
                    mHelper.getApprovedSubmissionsCount(form.getFormId());
            assigned += mHelper.getSubmissionsCount(form.getFormId(), SubmissionStatus.WAITING_APPROVAL); // FIXME What is assigned to me?
            cancelled += mHelper.getSubmissionsCount(form.getFormId(), SubmissionStatus.CANCELLED);
            pendingApproval += mHelper.getSubmissionsCount(form.getFormId(), SubmissionStatus.WAITING_APPROVAL);
            pendingCorrection += mHelper.getSubmissionsCount(form.getFormId(), SubmissionStatus.WAITING_CORRECTION) +
                    mHelper.getPendingSubmissionsCount(form.getFormId());
            rescheduled += mHelper.getSubmissionsCount(form.getFormId(), SubmissionStatus.RESCHEDULED);

            done += mHelper.getSubmissionsCount(form.getFormId()) - mHelper.getRepprovedSubmissionsCount(form.getFormId());
        }

        stats.setApproved(approved);
        stats.setAssignedToMe(assigned);
        stats.setCancelled(cancelled);
        stats.setPendingApproval(pendingApproval);
        stats.setPendingCorrection(pendingCorrection);
        stats.setRescheduled(rescheduled);

        Set<Long> users = new HashSet<>();
        int goal = 0;
        for (Attribution attribution : mHelper.getAttributions()) {
            goal += attribution.getQuota();
            users.add(attribution.getUser().getId());
        }
        stats.setRemaining(goal - done);
        stats.setSurveyTakers(users.size());
        stats.setTotalGoal(goal);

        return stats;
    }

    public Stats getFormStats(long formId) {
        Stats stats = new Stats();

        FormData form = mHelper.getForm(formId);
        if (form != null && form.getPubEnd() != null) {
            stats.setRemainingDays(Math.max(0, Days.daysBetween(DateTime.now(), new DateTime(form.getPubEnd())).getDays() + 1));
        } else {
            stats.setRemainingDays(999);
        }

        Set<Long> users = new HashSet<>();
        int goal = 0;
        for (Attribution attribution : mHelper.getAttributions()) {
            if (attribution.getFormId() == formId) {
                goal += attribution.getQuota();
                users.add(attribution.getUser().getId());
            }
        }
        stats.setGoal(goal);
        stats.setSurveyTakers(users.size());
        stats.setRemaining((int) (goal - (mHelper.getSubmissionsCount(formId) +
                mHelper.getPendingSubmissionsCount(formId) -
                mHelper.getRepprovedSubmissionsCount(formId) +
                mHelper.getApprovedSubmissionsCount(formId))));

        stats.setApproved((int) mHelper.getSubmissionsCount(formId, SubmissionStatus.APPROVED));
        stats.setPendingCorrection((int) mHelper.getSubmissionsCount(formId, SubmissionStatus.WAITING_CORRECTION));
        stats.setPendingApproval((int) mHelper.getSubmissionsCount(formId, SubmissionStatus.WAITING_APPROVAL));
        stats.setRescheduled((int) mHelper.getSubmissionsCount(formId, SubmissionStatus.RESCHEDULED));
        stats.setCancelled((int) mHelper.getSubmissionsCount(formId, SubmissionStatus.CANCELLED));

        return stats;
    }
}
