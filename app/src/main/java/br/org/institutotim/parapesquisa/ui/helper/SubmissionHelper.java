package br.org.institutotim.parapesquisa.ui.helper;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.SubmissionLog;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;

public class SubmissionHelper {

    private final SectionHelper mSectionHelper;
    private final FieldHelper mFieldHelper;
    private final CorrectionHelper mCorrectionHelper;

    public SubmissionHelper(SectionHelper sectionHelper, FieldHelper fieldHelper, CorrectionHelper correctionHelper) {
        this.mSectionHelper = sectionHelper;
        this.mFieldHelper = fieldHelper;
        this.mCorrectionHelper = correctionHelper;
    }

    public UserSubmission extractAnswers(Date timestamp, List<View> sections, @Nullable UserSubmission submission) {
        return extractAnswers(timestamp, sections, submission, null);
    }

    public UserSubmission extractAnswers(Date timestamp, List<View> sections, @Nullable UserSubmission submission,
                                         @Nullable Integer currentPage) {

        List<Answer> answers = new ArrayList<>();
        if (submission != null && submission.getAnswers() != null && !submission.getAnswers().isEmpty()) {
            answers.addAll(submission.getAnswers());
        }

        for (View section : sections) {
            List<View> fields = mSectionHelper.getFieldsFromSection(section);
            for (View field : fields) {
                Answer value = mFieldHelper.extractValueForSubmission(field);
                if (value != null) answers.add(value);
            }
        }

        return generateUserSubmission(timestamp, answers, submission, currentPage);
    }

    public UserSubmission extractAnswersBySections(Date timestamp, List<Answer> answers, @Nullable UserSubmission submission) {
        return extractAnswersBySections(timestamp, answers, submission, null);
    }

    public UserSubmission extractAnswersBySections(Date timestamp, List<Answer> answers, @Nullable UserSubmission submission,
                                                   @Nullable Integer currentPage) {

        if (submission != null && submission.getAnswers() != null && !submission.getAnswers().isEmpty()) {
            for (int i = 0; i < submission.getAnswers().size(); i++) {
                final Answer answered = submission.getAnswers().get(i);
                final long position = getAnswerPosition(answers, answered);
                if (position == -1)
                    answers.add(answered);
            }
        }

        return generateUserSubmission(timestamp, answers, submission, currentPage);
    }

    private int getAnswerPosition(List<Answer> answers, Answer answered) {
        if (answered == null) {
            return -1;
        }
        for (int i = 0; i < answers.size(); i++) {
            final Answer answer = answers.get(i);
            if (answer != null && answer.getFieldId() == answered.getFieldId())
                return i;
        }
        return -1;
    }

    public UserSubmission generateUserSubmission(Date timestamp, List<Answer> answers, @Nullable UserSubmission submission,
                                                 @Nullable Integer currentPage) {
        List<SubmissionLog> logs = new ArrayList<>();
        logs.add(SubmissionLog.builder()
                .when(new DateTime(timestamp))
                .build());

        return UserSubmission.builder()
                .answers(answers)
                .id(submission != null ? submission.getId() : null)
                .log(logs)
                .currentPage(currentPage)
                .build();
    }

    public boolean hasExtraData(List<UserSubmission> submissions) {
        if (submissions == null || submissions.isEmpty()) return false;

        for (UserSubmission submission : submissions) {
            if (SubmissionStatus.NEW.equals(submission.getStatus())) return true;
        }

        return false;
    }

    public void showExtraDataPicker(Context context, List<String> submissions, MaterialDialog.ListCallback callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.title_select_option)
                .items(submissions.toArray(new String[submissions.size()]))
                .itemsCallback(callback)
                .negativeText(R.string.button_cancel)
                .show();
    }

    public List<UserSubmission> getNewSubmissionsOptions(List<UserSubmission> submissions) {
        List<UserSubmission> options = new ArrayList<>();
        for (UserSubmission submission : submissions) {
            if (submission.getStatus().equals(SubmissionStatus.NEW)) {
                options.add(submission);
            }
        }
        return options;
    }

    public List<String> getList(FormData form, List<UserSubmission> options) {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < form.getSections().size(); i++) {
            Section section = form.getSections().get(i);
            for (int j = 0; j < section.getFields().size(); j++) {
                Field field = section.getFields().get(j);
                if (field.isIdentifier()) {
                    for (int k = 0; k < options.size(); k++) {
                        UserSubmission submission = options.get(k);
                        Answer answer = submission.getAnswerForField(field.getId());
                        list.add(answer != null ? answer.getValues() : "UNAVAILABLE");
                    }
                    break;
                }
            }
            if (!list.isEmpty()) break;
        }

        return list;
    }

    public UserSubmission updateAnswers(List<View> sections, UserSubmission submission) {
        List<Answer> answers = new ArrayList<>();

        for (View section : sections) {
            List<View> fields = mSectionHelper.getFieldsFromSection(section);
            for (View field : fields) {
                Field json = (Field) field.getTag();
                if (needsCorrection(json, submission)) {
                    Answer value = mFieldHelper.extractValueForSubmission(field);
                    if (value != null) answers.add(value);
                }
            }
        }

        updateAnswerField(answers, submission);

        return submission;
    }

    private void updateAnswerField(List<Answer> answers, UserSubmission submission) {
        List<Answer> currentAnswers = submission.getAnswers();

        for (Answer answer : answers) {
            replaceAnswer(answer, currentAnswers);
        }
    }

    public UserSubmission updateAnswersBySections(List<Answer> answers, UserSubmission submission) {
        List<Answer> currentAnswers = submission.getAnswers();

        for (Answer answer : answers) {
            replaceAnswer(answer, currentAnswers);
        }

        return submission;
    }

    private void replaceAnswer(Answer newAnswer, List<Answer> currentAnswers) {
        Answer oldAnswer = null;
        if (newAnswer == null) {
            return;
        }
        for (int i = 0; i < currentAnswers.size(); i++) {
            Answer answer = currentAnswers.get(i);
            if (newAnswer.getFieldId() == answer.getFieldId()) {
                oldAnswer = answer;
            }
        }

        if (oldAnswer != null) currentAnswers.remove(oldAnswer);
        currentAnswers.add(newAnswer);
    }

    private boolean needsCorrection(Field json, UserSubmission submission) {
        return mCorrectionHelper.needsCorrection(json, submission.getCorrections());
    }
}
