package br.org.institutotim.parapesquisa.ui.helper;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.event.AddCommentEvent;
import br.org.institutotim.parapesquisa.data.event.RemoveCommentEvent;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FieldOption;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserRole;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.watcher.CurrencyTextWatcher;
import br.org.institutotim.parapesquisa.util.DateUtils;
import br.org.institutotim.parapesquisa.util.StringUtils;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class ModeratorFieldHelper {

    private final ParaPesquisaPreferences mPreferences;
    private final AnswerHelper mAnswerHelper;
    private final ModeratorHelper mModeratorHelper;
    private final FieldActionHelper mFieldActionHelper;

    public ModeratorFieldHelper(ParaPesquisaPreferences preferences, AnswerHelper answerHelper,
                                ModeratorHelper moderatorHelper, FieldActionHelper fieldActionHelper) {
        mPreferences = preferences;
        mAnswerHelper = answerHelper;
        mModeratorHelper = moderatorHelper;
        mFieldActionHelper = fieldActionHelper;
    }

    public View inflate(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber) {
        switch (field.getType()) {
            case TEXT:
                return inflateTextField(context, container, field, fieldNumber, sectionNumber, null, null, false);
            case CPF:
                return inflateCpfField(context, container, field, fieldNumber, sectionNumber, null, false);
            case LABEL:
                return inflateLabelField(context, container, field);
            case EMAIL:
                return inflateTextField(context, container, field, fieldNumber, sectionNumber, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, null, false);
            case MONEY:
                return inflateMoneyField(context, container, field, fieldNumber, sectionNumber, null, false);
            case DATETIME:
                return inflateDateTimeField(context, container, field, fieldNumber, sectionNumber, null, false);
            case CHECKBOX:
                return inflateCheckboxField(context, container, field, fieldNumber, sectionNumber, null, false);
            case PRIVATE:
                return inflatePrivateField(context, container, field, fieldNumber, sectionNumber, null, false);
            case RADIO:
                return inflateRadioField(context, container, field, fieldNumber, sectionNumber, null, false);
            case SELECT:
                return inflateSelectField(context, container, field, fieldNumber, sectionNumber, null, false);
            case URL:
                return inflateTextField(context, container, field, fieldNumber, sectionNumber, InputType.TYPE_TEXT_VARIATION_URI, null, false);
            case ORDERED_LIST:
                return inflateOrderedListField(context, container, field, fieldNumber, sectionNumber, null, false);
            case NUMBER:
                return inflateTextField(context, container, field, fieldNumber, sectionNumber, InputType.TYPE_CLASS_NUMBER, null, false);
        }

        return null;
    }

    private View inflateMoneyField(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission, boolean readOnly) {
        View view = fillCommonData(context, R.layout.field_text, container, field, fieldNumber, sectionNumber);

        EditText textField = ButterKnife.findById(view, R.id.field);
        textField.addTextChangedListener(new CurrencyTextWatcher(textField));
        textField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);

        if (submission != null) {
            for (int i = 0; i < submission.getAnswers().size(); i++) {
                Answer answer = submission.getAnswers().get(i);
                if (answer.getFieldId() == field.getId()) {
                    textField.setEnabled(!readOnly);
                    textField.setText(answer.getValues());
                }
            }
        }

        return view;
    }

    private View inflateLabelField(Context context, ViewGroup container, Field field) {
        View view = LayoutInflater.from(context).inflate(R.layout.field_label, container, false);
        view.setTag(field);

        TextView label = ButterKnife.findById(view, R.id.title);
        label.setText(field.getLabel());

        TextView description = ButterKnife.findById(view, R.id.description);
        if (field.getDescription() == null || field.getDescription().isEmpty()) {
            description.setVisibility(View.GONE);
        } else {
            description.setText(field.getDescription());
        }

        return view;
    }

    private View inflateOrderedListField(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission, boolean readOnly) {
        View view;

        if (submission == null) {
            view = fillCommonData(context, R.layout.field_ordered_list, container, field, fieldNumber, sectionNumber);

            DragLinearLayout group = ButterKnife.findById(view, R.id.field_ordered_list);
            if (field.getOptions() != null) {
                for (int i = 0; i < field.getOptions().size(); i++) {
                    FieldOption option = field.getOptions().get(i);
                    View item = LayoutInflater.from(context).inflate(R.layout.item_list_draggable, group, false);
                    TextView textView = ButterKnife.findById(item, R.id.title);
                    textView.setText(option.getLabel());
                    item.setTag(option);
                    group.addView(item);
                    group.setViewDraggable(item, item);
                }
            }
        } else {
            view = fillCommonData(context, readOnly ? R.layout.field_checkbox : R.layout.field_ordered_list, container, field, fieldNumber, sectionNumber);
            LinearLayout group = ButterKnife.findById(view, readOnly ? R.id.field_checkbox : R.id.field_ordered_list);
            for (int i = 0; i < submission.getAnswers().size(); i++) {
                Answer answer = submission.getAnswers().get(i);
                if (answer.getFieldId() == field.getId()) {
                    String[] values = StringUtils.split(answer.getValues(), "\\\\");
                    for (String value : values) {
                        if (field.getOptions() != null) {
                            for (int j = 0; j < field.getOptions().size(); j++) {
                                FieldOption option = field.getOptions().get(j);
                                if (option.getValue().equals(value)) {
                                    View item = LayoutInflater.from(context).inflate(R.layout.item_list_draggable, group, false);
                                    TextView textView = ButterKnife.findById(item, R.id.title);
                                    textView.setText(option.getLabel());
                                    item.setTag(option);
                                    group.addView(item);

                                    if (!readOnly) {
                                        ((DragLinearLayout) group).setViewDraggable(item, item);
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

        return view;
    }

    private View inflateCpfField(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission, boolean readOnly) {
        View view = fillCommonData(context, R.layout.field_cpf, container, field, fieldNumber, sectionNumber);

        if (submission != null) {
            for (int i = 0; i < submission.getAnswers().size(); i++) {
                Answer answer = submission.getAnswers().get(i);
                if (answer.getFieldId() == field.getId()) {
                    TextView textField = ButterKnife.findById(view, R.id.field);
                    textField.setEnabled(!readOnly);
                    textField.setText(answer.getValues());
                }
            }
        }

        return view;
    }

    private View inflateCheckboxField(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission, boolean readOnly) {
        View view = fillCommonData(context, R.layout.field_checkbox, container, field, fieldNumber, sectionNumber);

        ViewGroup group = ButterKnife.findById(view, R.id.field_checkbox);
        if (field.getOptions() != null) {
            for (FieldOption option : field.getOptions()) {
                if (!option.getValue().equals("other") || readOnly) {
                    CheckBox check = new CheckBox(context);
                    if (!option.getValue().equals("other") || (submission != null && !mAnswerHelper.hasOtherValue(submission.getAnswerForField(field.getId()),
                            field.getOptions()))) {
                        check.setText(option.getLabel());
                    } else {
                        check.setChecked(true);
                        check.setText(mAnswerHelper.getOtherValue(submission.getAnswerForField(field.getId()),
                                field.getOptions()));
                    }
                    check.setTag(option);
                    check.setOnCheckedChangeListener((buttonView, isChecked) -> mFieldActionHelper.verifyAction(field, option, isChecked));
                    group.addView(check, 0);

                    if (submission != null) {
                        check.setEnabled(!readOnly);
                        for (Answer answer : submission.getAnswers()) {
                            if (answer.getFieldId() == field.getId()) {
                                String[] values = StringUtils.split(answer.getValues(), "\\\\");
                                if (Arrays.asList(values).contains(option.getValue()))
                                    check.setChecked(true);
                            }
                        }
                    }
                } else {
                    View check = LayoutInflater.from(context).inflate(R.layout.checkbox_other, group, false);
                    check.setTag(option);
                    group.addView(check);

                    if (submission != null && mAnswerHelper.hasOtherValue(submission.getAnswerForField(field.getId()),
                            field.getOptions())) {
                        TextView text = (TextView) ((ViewGroup) check).getChildAt(1);
                        text.setText(mAnswerHelper.getOtherValue(submission.getAnswerForField(field.getId()),
                                field.getOptions()));
                        ((CheckBox) ((ViewGroup) check).getChildAt(0)).toggle();
                    }
                }
            }
        }

        return view;
    }

    private View inflatePrivateField(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission, boolean readOnly) {
        View view;
        if (submission == null) {
            view = fillCommonData(context, R.layout.field_private, container, field, fieldNumber, sectionNumber);

            TextView textField = ButterKnife.findById(view, R.id.field);
            textField.setTag(Boolean.FALSE);
            ButterKnife.findById(view, R.id.confirm).setOnClickListener(v -> {
                if (!textField.getText().toString().trim().isEmpty()) {
                    textField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    textField.setEnabled(false);
                }
            });
        } else {
            view = fillCommonData(context, !readOnly ? R.layout.field_private : R.layout.field_text, container, field, fieldNumber, sectionNumber);
            TextView textField = ButterKnife.findById(view, R.id.field);
            textField.setEnabled(!readOnly);
            if (!readOnly) {
                ButterKnife.findById(view, R.id.confirm).setOnClickListener(v -> {
                    if (!textField.getText().toString().trim().isEmpty()) {
                        textField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        textField.setEnabled(false);
                    }
                });
            }
            UserData user = mPreferences.getUser();
            if (user != null && user.getRole().equals(UserRole.AGENT) && readOnly) {
                textField.setText(context.getString(R.string.message_private_answer));
            } else {
                for (int i = 0; i < submission.getAnswers().size(); i++) {
                    Answer answer = submission.getAnswers().get(i);
                    if (answer.getFieldId() == field.getId()) {
                        textField.setText(answer.getValues());
                        textField.setEnabled(false);
                        textField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            }
        }

        return view;
    }

    private View inflateRadioField(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission, boolean readOnly) {
        View view = fillCommonData(context, R.layout.field_radio, container, field, fieldNumber, sectionNumber);

        RadioGroup group = ButterKnife.findById(view, R.id.field_radio);
        final List<RadioButton> others = new ArrayList<>();
        for (FieldOption option : field.getOptions()) {
            if (!option.getValue().equals("other") || readOnly) {
                RadioButton radio = new RadioButton(context);
                if (!option.getValue().equals("other") || (submission != null && !mAnswerHelper.hasOtherValue(submission.getAnswerForField(field.getId()),
                        field.getOptions()))) {
                    radio.setText(option.getLabel());
                } else {
                    ((Activity) context).runOnUiThread(() -> radio.toggle());
                    radio.setText(mAnswerHelper.getOtherValue(submission.getAnswerForField(field.getId()),
                            field.getOptions()));
                }

                radio.setTag(option);
                group.addView(radio, 0);

                if (submission != null) {
                    radio.setEnabled(!readOnly);
                    for (int j = 0; j < submission.getAnswers().size(); j++) {
                        Answer answer = submission.getAnswers().get(j);
                        if (answer.getFieldId() == field.getId()) {
                            String[] values = StringUtils.split(answer.getValues(), "\\\\");
                            if (Arrays.asList(values).contains(option.getValue())) radio.toggle();
                        }
                    }
                }
            } else {
                ViewGroup radio = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.radio_other, group, false);
                RadioButton other = (RadioButton) radio.getChildAt(0);
                others.add(other);
                other.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        group.clearCheck();
                        other.toggle();
                        mFieldActionHelper.verifyAction(field, group);
                    }
                });
                radio.setTag(option);
                group.addView(radio);

                if (submission != null && mAnswerHelper.hasOtherValue(submission.getAnswerForField(field.getId()),
                        field.getOptions())) {
                    TextView text = (TextView) radio.getChildAt(1);
                    text.setText(mAnswerHelper.getOtherValue(submission.getAnswerForField(field.getId()),
                            field.getOptions()));
                    other.toggle();
                }
            }
        }

        return view;
    }

    private View inflateSelectField(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission, boolean readOnly) {
        View view = fillCommonData(context, R.layout.field_select, container, field, fieldNumber, sectionNumber);

        Spinner spinner = ButterKnife.findById(view, R.id.field_select);
        ArrayAdapter<FieldOption> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                android.R.id.text1, field.getOptions());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        if (submission != null) {
            spinner.setEnabled(!readOnly);
            for (int i = 0; i < submission.getAnswers().size(); i++) {
                Answer answer = submission.getAnswers().get(i);
                if (!answer.getValues().isEmpty() && answer.getFieldId() == field.getId()) {
                    for (int j = 0; j < field.getOptions().size(); j++) {
                        if (field.getOptions().get(j).getValue().equals(answer.getValues()))
                            spinner.setSelection(j);
                    }
                }
            }
        }

        return view;
    }

    private View inflateDateTimeField(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission, boolean readOnly) {
        View view = fillCommonData(context, R.layout.field_datetime, container, field, fieldNumber, sectionNumber);

        final TextView textField = ButterKnife.findById(view, R.id.field);
        textField.setTag(DateTime.now());
        textField.setOnClickListener(v -> {
            DateTime date = (DateTime) textField.getTag();

            final DatePickerDialog dialog = new DatePickerDialog(context, (view1, year, monthOfYear, dayOfMonth) -> {
                DateTime selectedDate = new DateTime().withTime(0, 0, 0, 0).withYear(year).withMonthOfYear(monthOfYear + 1).withDayOfMonth(dayOfMonth);
                textField.setTag(selectedDate);
                textField.setText(DateUtils.formatShortDate(context, selectedDate.toDate()));
            }, date.getYear(), date.getMonthOfYear() - 1 /* damn Java Calendar... */, date.getDayOfMonth());

            dialog.show();
        });

        if (submission != null) {
            for (Answer answer : submission.getAnswers()) {
                if (answer.getFieldId() == field.getId()) {
                    try {
                        textField.setEnabled(!readOnly);
                        textField.setText(DateUtils.formatShortDate(context, answer.getValues()));
                    } catch (Exception e) {
                        try {
                            textField.setText(DateUtils.formatShortDate(context, new SimpleDateFormat("dd-MM-yyyy").parse(answer.getValues())));
                        } catch (ParseException e1) {
                            Log.wtf("Para Pesquisa", "De fato, isso não deveria acontecer", e);
                        }
                    }
                }
            }
        }

        return view;
    }

    private View inflateTextField(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, Integer inputType, UserSubmission submission, boolean readOnly) {
        View view = fillCommonData(context, R.layout.field_text, container, field, fieldNumber, sectionNumber);

        if (inputType != null) {
            TextView textField = ButterKnife.findById(view, R.id.field);
            textField.setInputType(InputType.TYPE_CLASS_TEXT | inputType);
        } else if (field.getLayout() != null) {
            TextView textField = ButterKnife.findById(view, R.id.field);
            InputFilter[] filterArray = new InputFilter[1];
            switch (field.getLayout()) {
                case SMALL:
                    filterArray[0] = new InputFilter.LengthFilter(4);
                    textField.setFilters(filterArray);
                    textField.setSingleLine();
                    break;
                case MEDIUM:
                    filterArray[0] = new InputFilter.LengthFilter(30);
                    textField.setFilters(filterArray);
                    textField.setSingleLine();
                    break;
                case BIG:
                    textField.setMinLines(4);
                    filterArray[0] = new InputFilter.LengthFilter(500);
                    textField.setFilters(filterArray);
                    break;
            }
        }

        if (submission != null) {

            for (int i = 0; i < submission.getAnswers().size(); i++) {
                Answer answer = submission.getAnswers().get(i);
                if (answer.getFieldId() == field.getId()) {
                    TextView textField = ButterKnife.findById(view, R.id.field);
                    textField.setEnabled(!readOnly);
                    textField.setText(answer.getValues());
                }
            }
        }

        return view;
    }

    private View fillCommonData(final Context context, @LayoutRes int layout, ViewGroup container, Field field,
                                int fieldNumber, int sectionNumber) {

        final View view = LayoutInflater.from(context).inflate(layout, container, false);
        view.setTag(field);
        TextView label = ButterKnife.findById(view, R.id.title);
        label.setText(field.getLabel());

        TextView number = ButterKnife.findById(view, R.id.number);
        number.setText(String.format("%d.%d", sectionNumber, fieldNumber));

        ButterKnife.findById(view, R.id.description).setVisibility(View.GONE);
        ButterKnife.findById(view, R.id.comment_area).setVisibility(View.VISIBLE);

        StringBuilder builder = new StringBuilder();
        ImageView commentAnchor = ButterKnife.findById(view, R.id.comment_anchor);
        commentAnchor.setVisibility(View.VISIBLE);
        commentAnchor.setOnClickListener(v -> {
            MaterialDialog dialog = getDialogBuilder(context, builder, field, view).build();
            dialog.setTitle(R.string.title_add_comment);
            EditText content = (EditText) dialog.getCustomView();
            content.setText("");
            dialog.show();
        });

        ButterKnife.findById(view, R.id.edit_anchor).setOnClickListener(v -> {
            MaterialDialog dialog = getDialogBuilder(context, builder, field, view).build();
            dialog.setTitle(R.string.title_edit_comment);
            EditText content = (EditText) dialog.getCustomView();
            content.setText(builder.toString());
            dialog.show();
        });
        ButterKnife.findById(view, R.id.delete_anchor).setOnClickListener(v -> new MaterialDialog.Builder(context)
                .title(R.string.title_remove_comment)
                .positiveText(R.string.button_yes)
                .negativeText(R.string.button_no)
                .content(R.string.message_remove_comment_confirmation)
                .autoDismiss(true)
                .onPositive((materialDialog, dialogAction) -> {
                    mModeratorHelper.removeComment(view);
                    EventBus.getDefault().post(new RemoveCommentEvent(field));
                    builder.setLength(0);
                })
                .show());

        return view;
    }

    private MaterialDialog.Builder getDialogBuilder(Context context, StringBuilder builder, Field field, View view) {
        return new MaterialDialog.Builder(context)
                .title(R.string.title_add_comment)
                .negativeText(R.string.button_cancel)
                .positiveText(R.string.button_done)
                .customView(R.layout.comment_edit_text, false)
                .autoDismiss(true)
                .onPositive((materialDialog1, dialogAction1) -> {
                    EditText content = (EditText) materialDialog1.getCustomView();
                    if (!content.getText().toString().trim().isEmpty()) {
                        mModeratorHelper.addComment(context, view, content.getText().toString().trim());
                        builder.setLength(0);
                        builder.append(content.getText().toString().trim());
                        EventBus.getDefault().post(new AddCommentEvent(field, content.getText().toString().trim()));
                        content.setText("");
                    }
                });
    }

    public View inflateFilled(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission, boolean readOnly) {
        if (!hasAnswer(field, submission) && field.isReadOnly()) return null;

        switch (field.getType()) {
            case TEXT:
                return inflateTextField(context, container, field, fieldNumber, sectionNumber, null, submission, readOnly);
            case CPF:
                return inflateCpfField(context, container, field, fieldNumber, sectionNumber, submission, readOnly);
            case EMAIL:
                return inflateTextField(context, container, field, fieldNumber, sectionNumber, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, submission, readOnly);
            case MONEY:
                return inflateMoneyField(context, container, field, fieldNumber, sectionNumber, submission, readOnly);
            case DATETIME:
                return inflateDateTimeField(context, container, field, fieldNumber, sectionNumber, submission, readOnly);
            case CHECKBOX:
                return inflateCheckboxField(context, container, field, fieldNumber, sectionNumber, submission, readOnly);
            case PRIVATE:
                return inflatePrivateField(context, container, field, fieldNumber, sectionNumber, submission, readOnly);
            case RADIO:
                return inflateRadioField(context, container, field, fieldNumber, sectionNumber, submission, readOnly);
            case SELECT:
                return inflateSelectField(context, container, field, fieldNumber, sectionNumber, submission, readOnly);
            case URL:
                return inflateTextField(context, container, field, fieldNumber, sectionNumber, InputType.TYPE_TEXT_VARIATION_URI, submission, readOnly);
            case ORDERED_LIST:
                return inflateOrderedListField(context, container, field, fieldNumber, sectionNumber, submission, readOnly);
            case NUMBER:
                return inflateTextField(context, container, field, fieldNumber, sectionNumber, InputType.TYPE_CLASS_NUMBER, submission, readOnly);
        }

        return null;
    }

    public boolean hasAnswer(Field field, UserSubmission submission) {
        if (submission == null) return true;

        for (int i = 0; i < submission.getAnswers().size(); i++) {
            Answer answer = submission.getAnswers().get(i);
            if (answer.getFieldId() == field.getId())
                return true;
        }

        return false;
    }
}

