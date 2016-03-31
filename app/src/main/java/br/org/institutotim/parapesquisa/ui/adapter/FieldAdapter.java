package br.org.institutotim.parapesquisa.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.activity.AgentSubmissionCorrectionActivity;
import br.org.institutotim.parapesquisa.ui.activity.BaseSubmissionViewActivity;
import br.org.institutotim.parapesquisa.ui.activity.ModeratorSubmissionApprovalActivity;
import br.org.institutotim.parapesquisa.ui.validator.FieldValidator;
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

import static br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper.fillDataFor;
import static br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper.fillDataForCorrection;
import static br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper.fillDataForModeration;
import static br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper.getAnswerForField;
import static br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper.getLayoutIdFor;
import static br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper.getViewHolder;
import static br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper.hasCorrection;
import static br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper.hideError;
import static br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper.showError;
import static java.util.Collections.sort;

public class FieldAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseViewHolder.OnAnswerValueChangedListener {

    private UserSubmission mSubmission;
    private List<Field> mFields;
    private SparseArrayCompat<Integer> errors = new SparseArrayCompat<>();
    private boolean disable;
    private boolean correction;
    private boolean moderator;

    private Map<Field, Answer> mAnswers = new ArrayMap<>();
    private Map<Field, SubmissionCorrection> corrections = new ArrayMap<>();


    public FieldAdapter(Section section, @Nullable UserSubmission submission, @Nullable List<Answer> answers, boolean disable, boolean correction, boolean moderator, List<SubmissionCorrection> corrections) {
        this.mSubmission = submission;
        this.disable = disable;
        this.correction = correction;
        this.moderator = moderator;
        this.mFields = new ArrayList<>();
        for (int i = 0; i < section.getFields().size(); i++) {
            Field field = section.getFields().get(i);

            Boolean value = BaseSubmissionViewActivity.getReadOnlyStatus(field.getId());

            boolean shouldDisable = value != null && value;

            if (!shouldDisable && !field.isReadOnly())
                mFields.add(field);
        }
        sortFields();

        setupAnswers(answers);
        setupCorrections(corrections);
    }

    public void removeAnswer(Field field) {
        mAnswers.remove(field);
    }

    private void sortFields() {
        sort(mFields, (lhs, rhs) -> {
            if (lhs.getOrder() == null || rhs.getOrder() == null)
                return lhs.getId() < rhs.getId() ? -1 : 1;
            if (lhs.getOrder() < rhs.getOrder()) return -1;
            if (lhs.getOrder() > rhs.getOrder()) return 1;
            return 0;
        });
    }

    public void setmFields(List<Field> mFields) {
        synchronized (mFields) {
            this.mFields = mFields;
			sortFields();
        }
    }

    private void setupCorrections(List<SubmissionCorrection> correctionsList) {
        if (correctionsList == null) {
            return;
        }

        for (int i = 0; i < correctionsList.size(); i++) {
            final SubmissionCorrection submissionCorrection = correctionsList.get(i);
            final Field field = getField(submissionCorrection.getFieldId());
            if (field != null) {
                corrections.put(field, submissionCorrection);
            }
        }
    }

    private void setupAnswers(@Nullable List<Answer> answers) {
        if (answers == null) {
            return;
        }

        for (int i = 0; i < answers.size(); i++) {
            Answer answer = answers.get(i);
            if (answer != null) {
                Field field = getField(answer.getFieldId());
                if (field != null) {
                    mAnswers.put(field, answer);
                }
            }
        }
    }

    @Nullable
    private Field getField(long id) {
        for (int i = 0; i < mFields.size(); i++) {
            if (mFields.get(i).getId() == id) {
                return mFields.get(i);
            }
        }
        return null;
    }

    public int getFieldPosition(final Field field) {
        if (field != null) {
            for (int i = 0; i < mFields.size(); i++) {
                final Field fieldResult = mFields.get(i);
                if (fieldResult != null && field.getId() == fieldResult.getId())
                    return i;
            }
        }

        return -1;
    }

    private boolean hasField(long id) {
        return getField(id) != null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = getViewHolder(LayoutInflater.from(parent.getContext()).inflate(getLayoutIdFor(viewType), parent, false), viewType);
        if (holder instanceof BaseViewHolder) {
            ((BaseViewHolder) holder).setOnAnswerValueChangedListener(this);
        }
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        return mFields.get(position).getType().ordinal();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Field field = mFields.get(position);
        if (holder instanceof TextViewHolder
                || holder instanceof RadioViewHolder
                || holder instanceof DateTimeViewHolder
                || holder instanceof NumberViewHolder
                || holder instanceof LabelViewHolder
                || holder instanceof CheckboxViewHolder
                || holder instanceof SelectViewHolder
                || holder instanceof CpfViewHolder
                || holder instanceof EmailViewHolder
                || holder instanceof UrlViewHolder
                || holder instanceof MoneyViewHolder
                || holder instanceof OrderedListViewHolder
                || holder instanceof PrivateViewHolder) {
            final Answer answer = mAnswers.containsKey(field) ?
                    mAnswers.get(field) : getAnswerForField(field, mSubmission);

            if (moderator) {
                final SubmissionCorrection submissionCorrection = corrections.get(field);
                fillDataForModeration(holder, field, answer, submissionCorrection);
            } else {
                if (!correction) {
                    fillDataFor(holder, field, answer, disable);
                    if (errors.get(position, 0) != 0) {
                        showError(holder, errors.get(position));
                    } else {
                        hideError(holder);
                    }
                } else {
                    fillDataForCorrection(holder, field, mAnswers.values(), answer, mSubmission);
                }
            }
        } else {
            ((TextView) holder.itemView.findViewById(android.R.id.text1)).setText((holder.getAdapterPosition() + 1) + " " + field.getLabel());
        }
    }

    @Override
    public int getItemCount() {
        return mFields.size();
    }

    public List<Answer> getAnswers() {
        return new ArrayList<>(mAnswers.values());
    }

    public List<SubmissionCorrection> getCorrections() {
        return new ArrayList<>(corrections.values());
    }



    @Override
    public void onChange(Field field, Answer answer) {
        mAnswers.put(field, answer);
    }

    @Override
    public void addComment(Field field, String comment) {
        if (!hasField(field.getId()))
            return;

        final SubmissionCorrection submissionCorrection = SubmissionCorrection.builder()
                .createdAt(DateTime.now())
                .fieldId(field.getId())
                .message(comment)
                .userId(ModeratorSubmissionApprovalActivity.user.getId())
                .build();

        corrections.put(field, submissionCorrection);
    }

    @Override
    public void removeComment(Field field) {
        if (hasField(field.getId()))
            corrections.remove(field);
    }

    public int validate() {
        synchronized (mFields) {
            int valid = -1;

            if (disable || (correction && AgentSubmissionCorrectionActivity.correctionType == AgentSubmissionCorrectionActivity.CORRECTIONS_READ_ONLY))
                return valid;

            errors.clear();
            for (int i = 0; i < mFields.size(); i++) {
                final Field field = mFields.get(i);
                if (field.isReadOnly())
                    continue;

                if (mSubmission != null && SubmissionStatus.WAITING_CORRECTION.equals(mSubmission.getStatus()) && !hasCorrection(field, mSubmission))
                    continue;

                int message = new FieldValidator().validate(field, mAnswers.get(field));
                if (message != 0) {
                    errors.put(i, message);
                    if (valid == -1) {
                        valid = i;
                    }
                }
            }
            if (valid != -1) {
                notifyDataSetChanged();
            }
            return valid;
        }
    }

    public void refreshDataExceptField(Field field) {
        /*int size = mFields.size() - 1;
        for (int i = size; i >= 0; i--) {
            if (mFields.get(i).getId() != field.getId()) {
                notifyItemChanged(i);
            }
        }
        */
        synchronized (mFields) {
            notifyDataSetChanged();
        }
    }
}
