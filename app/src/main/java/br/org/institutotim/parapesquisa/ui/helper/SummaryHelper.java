package br.org.institutotim.parapesquisa.ui.helper;

import android.util.SparseArray;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.Summary;
import br.org.institutotim.parapesquisa.data.model.UserForm;

public class SummaryHelper {


    private final ParaPesquisaOpenHelper mHelper;

    public SummaryHelper(ParaPesquisaOpenHelper helper) {
        this.mHelper = helper;
    }

    public SparseArray<Summary> generateSummaries() {
        SparseArray<Summary> summaries = new SparseArray<>();

        List<UserForm> forms = mHelper.getUserForms();
        for (UserForm form : forms) {
            summaries.append(Long.valueOf(form.getFormId()).intValue(), extractSummary(form));
        }

        return summaries;
    }

    private Summary extractSummary(UserForm form) {
        DateTime dateTime = form.getForm().getPubEnd();
        if (dateTime == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                dateTime = new DateTime(sdf.parse(Summary.NULL_DATE));
            } catch (ParseException e) {
                dateTime = DateTime.now().plusDays(999);
            }
        }
        return Summary.builder()
                .approved((int) mHelper.getSubmissionsCount(form.getFormId(), SubmissionStatus.APPROVED))
                .date(dateTime)
                .quota(form.getQuota())
                .remaining((int) mHelper.getRemainingSurveys(form.getFormId()))
                .waitingCorrection((int) mHelper.getSubmissionsCount(form.getFormId(), SubmissionStatus.WAITING_CORRECTION))
                .waitingApproval((int) mHelper.getSubmissionsCount(form.getFormId(), SubmissionStatus.WAITING_APPROVAL))
                .build();
    }

    public Summary setRepproved(Summary old, Summary newSummary) {
        return Summary.builder()
                .repproved(newSummary.getRemaining() - old.getRemaining())
                .date(newSummary.getDate())
                .waitingApproval(newSummary.getWaitingApproval())
                .waitingCorrection(newSummary.getWaitingCorrection())
                .approved(newSummary.getApproved())
                .quota(newSummary.getQuota())
                .remaining(newSummary.getRemaining())
                .build();

    }
}
