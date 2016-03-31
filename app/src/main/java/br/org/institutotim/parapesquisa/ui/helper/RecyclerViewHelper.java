package br.org.institutotim.parapesquisa.ui.helper;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FieldAction;
import br.org.institutotim.parapesquisa.data.model.FieldType;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.UserForm;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.activity.AgentSubmissionCorrectionActivity;
import br.org.institutotim.parapesquisa.ui.activity.ModeratorSubmissionApprovalActivity;
import br.org.institutotim.parapesquisa.ui.viewholder.BaseViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.CheckboxViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.CpfViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.DateTimeViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.EmailViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.LabelViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.MoneyViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.NumberViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.OrderedListViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.PrivateViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.RadioViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.SelectViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.TextViewHolder;
import br.org.institutotim.parapesquisa.ui.viewholder.UrlViewHolder;
import br.org.institutotim.parapesquisa.util.StringUtils;

public class RecyclerViewHelper {

    @LayoutRes
    public static int getLayoutIdFor(int viewType) {
        FieldType type = FieldType.values()[viewType];
        switch (type) {
            case TEXT:
            case NUMBER:
                return R.layout.field_text;
            case CPF:
                return R.layout.field_cpf;
            case LABEL:
                return R.layout.field_label;
            case EMAIL:
                return R.layout.field_email;
            case MONEY:
                return R.layout.field_money;
            case DATETIME:
                return R.layout.field_datetime;
            case CHECKBOX:
                return R.layout.field_checkbox;
            case PRIVATE:
                return R.layout.field_private;
            case RADIO:
                return R.layout.field_radio;
            case SELECT:
                return R.layout.field_select;
            case URL:
                return R.layout.field_url;
            case ORDERED_LIST:
                return R.layout.field_ordered_list;
        }
        return android.R.layout.simple_list_item_1;
    }

    @Nullable
    public static Answer getAnswerForField(Field field, UserSubmission submission) {
        if (submission == null) {
            return null;
        }

        return submission.getAnswerForField(field.getId());
    }

    @Nullable
    public static Answer getAnswerForField(Field field, List<Answer> answers) {
        if (answers == null) {
            return null;
        }

        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i).getFieldId() == field.getId()) {
                return answers.get(i);
            }
        }
        return null;
    }

    public static void fillDataFor(RecyclerView.ViewHolder holder, @NonNull Field field, Answer answer, boolean disable) {
        ((BaseViewHolder) holder).fillData(field, answer);

        if (disable || field.isReadOnly())
            ((BaseViewHolder) holder).disable();
        else
            ((BaseViewHolder) holder).enable();
    }

    public static void fillDataForCorrection(RecyclerView.ViewHolder holder, @NonNull Field field, Collection<Answer> answers, Answer answer, UserSubmission submission) {
        int correctionType = AgentSubmissionCorrectionActivity.correctionType;
        if (AgentSubmissionCorrectionActivity.CORRECTIONS_READ_ONLY == correctionType) {
            fillDataFor(holder, field, answer, true);
            if (hasCorrection(field, submission))
                ((BaseViewHolder) holder).highlightTitle();
        } else {
            boolean shouldEdit = false;
            List<SubmissionCorrection> corrections = submission.getCorrections();
            if (corrections != null) {
                List<FieldAction> fieldActions = field.getActions();
                for (Answer correctionAnswer : answers) {
                    if (correctionAnswer != null && correctionAnswer.getLastValues() != null) {
                        String[] values = StringUtils.split(correctionAnswer.getValues(), "\\\\");
                        String[] lastValues = StringUtils.split(correctionAnswer.getLastValues(), "\\\\");

                        if (values == null || lastValues == null) {
                            continue;
                        }
                        boolean containsLastValuesDisabled = false;

                        for (int j = 0; j < fieldActions.size(); j++) {
                            FieldAction fieldAction = fieldActions.get(j);
                            for (int i = 0; i < lastValues.length; i++) {
                                String value = lastValues[i];
                                if (fieldAction.getWhen() != null) {
                                    if (fieldAction.getWhen().contains(value)) {
                                        containsLastValuesDisabled = containsLastValuesDisabled || fieldAction.getDisable().contains(field.getId());
                                    }
                                }
                            }
                        }

                        boolean containsValuesDisabled = false;
                        for (int j = 0; j < fieldActions.size(); j++) {
                            FieldAction fieldAction = fieldActions.get(j);
                            for (int i = 0; i < values.length; i++) {
                                String value = values[i];
                                if (fieldAction.getWhen() != null) {
                                    if (fieldAction.getWhen().contains(value)) {
                                        containsValuesDisabled = containsValuesDisabled || fieldAction.getDisable().contains(field.getId());
                                    }
                                }
                            }
                        }
                        if (containsLastValuesDisabled && !containsValuesDisabled) {
                            shouldEdit = true;
                            break;
                        }
                    }
                }
            }
            if (hasCorrection(field, submission) || shouldEdit)
                inflateFilledForEdit(holder, field, answer, submission);
            else
                fillDataFor(holder, field, answer, true);
        }
    }

    public static void fillDataForModeration(RecyclerView.ViewHolder holder, @NonNull Field field, Answer answer, SubmissionCorrection submissionCorrection) {
        fillDataFor(holder, field, answer, true);
        final BaseViewHolder baseViewHolder = (BaseViewHolder) holder;
        baseViewHolder.addCommentArea(submissionCorrection, ModeratorSubmissionApprovalActivity.user);
    }

    private static void inflateFilledForEdit(RecyclerView.ViewHolder holder, @NonNull Field field, Answer answer, UserSubmission submission) {
        fillDataFor(holder, field, answer, false);
        final BaseViewHolder baseViewHolder = (BaseViewHolder) holder;
        baseViewHolder.highlightTitle();
        SubmissionCorrection correction = getCorrection(field, submission);
        baseViewHolder.addCommentReadOnly(correction, AgentSubmissionCorrectionActivity.user);
    }

    private static SubmissionCorrection getCorrection(Field field, UserSubmission submission) {
        if (submission.getCorrections() == null) return null;

        for (SubmissionCorrection correction : submission.getCorrections()) {
            if (correction.getFieldId() == field.getId()) return correction;
        }

        return null;
    }

    public static boolean hasCorrection(Field field, @Nullable UserSubmission submission) {
        if (submission == null || submission.getCorrections() == null) return false;

        for (SubmissionCorrection correction : submission.getCorrections()) {
            if (correction.getFieldId() == field.getId()) return true;
        }

        return false;
    }

    public static void showError(RecyclerView.ViewHolder holder, @StringRes int text) {
        if (holder instanceof BaseViewHolder) {
            ((BaseViewHolder) holder).showError(text);
        }
    }

    public static void hideError(RecyclerView.ViewHolder holder) {
        if (holder instanceof BaseViewHolder) {
            ((BaseViewHolder) holder).hideError();
        }
    }

    public static void checkForAnswer(Map<Field, Answer> answers, RecyclerView.ViewHolder holder) {
        Field tag = (Field) holder.itemView.getTag();
        if (tag != null) {
            answers.put(tag, ((BaseViewHolder) holder).extractAnswer());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends RecyclerView.ViewHolder> T getViewHolder(@NonNull View itemView, int viewType) {
        FieldType type = FieldType.values()[viewType];
        switch (type) {
            case TEXT:
                return (T) new TextViewHolder(itemView);
            case CPF:
                return (T) new CpfViewHolder(itemView);
            case LABEL:
                return (T) new LabelViewHolder(itemView);
            case EMAIL:
                return (T) new EmailViewHolder(itemView);
            case MONEY:
                return (T) new MoneyViewHolder(itemView);
            case DATETIME:
                return (T) new DateTimeViewHolder(itemView);
            case CHECKBOX:
                return (T) new CheckboxViewHolder(itemView);
            case PRIVATE:
                return (T) new PrivateViewHolder(itemView);
            case RADIO:
                return (T) new RadioViewHolder(itemView);
            case SELECT:
                return (T) new SelectViewHolder(itemView);
            case URL:
                return (T) new UrlViewHolder(itemView);
            case ORDERED_LIST:
                return (T) new OrderedListViewHolder(itemView);
            case NUMBER:
                return (T) new NumberViewHolder(itemView);
        }
        return (T) new RecyclerView.ViewHolder(itemView) {
        };
    }

    public static ActionWrapper processActions(Field field, Answer answer) {
        final List<FieldAction> fieldActions = field.getActions();
        final ActionWrapper actionWrapper = new ActionWrapper(field);
        final ActionWrapper selectedFieldAction = new ActionWrapper(field);

        if (answer != null) {
            String[] values = StringUtils.split(answer.getValues(), "\\\\");
            handleActions(fieldActions, values, actionWrapper, selectedFieldAction);
        } else {
            for (int i = 0; i < fieldActions.size(); i++) {
                FieldAction fieldAction = fieldActions.get(i);
                actionWrapper.addEnableSections(fieldAction.getDisableSections());
                actionWrapper.addEnable(fieldAction.getDisable());
            }
        }

        actionWrapper.getEnable().removeAll(selectedFieldAction.getDisable());
        actionWrapper.getEnableSections().removeAll(selectedFieldAction.getDisableSections());

        actionWrapper.addDisable(selectedFieldAction.getDisable());
        actionWrapper.addEnable(selectedFieldAction.getEnable());
        actionWrapper.addDisableSections(selectedFieldAction.getDisableSections());

        return actionWrapper;
    }

    private static void handleActions(List<FieldAction> fieldActions, String[] values, ActionWrapper actionWrapper, ActionWrapper selectedFieldAction) {
        for (int j = 0; j < fieldActions.size(); j++) {
            FieldAction fieldAction = fieldActions.get(j);
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (fieldAction.getWhen() != null) {
                    if (fieldAction.getWhen().contains(value)) {
                        selectedFieldAction.addEnable(fieldAction.getEnable());
                        selectedFieldAction.addDisable(fieldAction.getDisable());
                        selectedFieldAction.addDisableSections(fieldAction.getDisableSections());
                    } else {
                        actionWrapper.addEnableSections(fieldAction.getDisableSections());
                        actionWrapper.addEnable(fieldAction.getDisable());
                    }
                }
            }
        }
    }

    public static class ActionWrapper {

        Field field;

        List<Long> disable = new ArrayList<>();
        List<Long> enable = new ArrayList<>();
        List<Long> enableSections = new ArrayList<>();
        List<Long> disableSections = new ArrayList<>();

        final boolean disableReadOnly;
        final boolean enableReadOnly;
        final boolean disableSectionReadOnly;

        public ActionWrapper(Field field) {
            this(field, true, false, true);
        }

        public ActionWrapper(Field field, boolean disableReadOnly, boolean enableReadOnly, boolean disableSectionReadOnly) {
            this.field = field;
            this.disableReadOnly = disableReadOnly;
            this.enableReadOnly = enableReadOnly;
            this.disableSectionReadOnly = disableSectionReadOnly;
        }

        public Field getField() {
            return field;
        }

        public boolean getDisableReadOnly() {
            return disableReadOnly;
        }

        public boolean getEnableReadOnly() {
            return enableReadOnly;
        }

        public List<Long> getEnableSections() {
            return enableSections;
        }

        public boolean getDisableSectionReadOnly() {
            return disableSectionReadOnly;
        }

        public void addDisable(List<Long> disable) {
            if (disable == null || disable.isEmpty()) return;
            this.disable.addAll(disable);
        }

        public void addEnable(List<Long> enable) {
            if (enable == null || enable.isEmpty()) return;
            this.enable.addAll(enable);
        }

        public void addEnableSections(List<Long> enableSections) {
            if (enableSections == null || enableSections.isEmpty()) return;
            this.enableSections.addAll(enableSections);
        }

        public void addDisableSections(List<Long> disableSections) {
            if (disableSections == null || disableSections.isEmpty()) return;
            this.disableSections.addAll(disableSections);
        }

        public List<Long> getDisable() {
            return disable;
        }

        public List<Long> getEnable() {
            return enable;
        }

        public List<Long> getDisableSections() {
            return disableSections;
        }
    }

}
