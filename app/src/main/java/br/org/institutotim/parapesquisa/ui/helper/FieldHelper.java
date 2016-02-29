package br.org.institutotim.parapesquisa.ui.helper;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.jmedeisis.draglinearlayout.DragLinearLayout;

import org.joda.time.DateTime;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.event.FormUpdatedEvent;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FieldOption;
import br.org.institutotim.parapesquisa.data.model.FieldType;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserRole;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.validator.FieldValidator;
import br.org.institutotim.parapesquisa.ui.watcher.CurrencyTextWatcher;
import br.org.institutotim.parapesquisa.util.DateUtils;
import br.org.institutotim.parapesquisa.util.StringUtils;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class FieldHelper {

    private final AnswerHelper mAnswerHelper;
    private final ModeratorHelper mModeratorHelper;
    private final ParaPesquisaPreferences mPreferences;
    private final FieldValidator mFieldValidator;

    public FieldHelper(AnswerHelper answerHelper, ModeratorHelper moderatorHelper, ParaPesquisaPreferences preferences,
                       FieldValidator fieldValidator) {
        this.mAnswerHelper = answerHelper;
        this.mModeratorHelper = moderatorHelper;
        this.mPreferences = preferences;
        this.mFieldValidator = fieldValidator;
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
                if (answer.getFieldId() == field.getId() && field.getOptions() != null) {
                    String[] values = StringUtils.split(answer.getValues(), "\\\\");
                    for (String value : values) {
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
        if (field.getOptions() == null) return view;

        for (int i = 0; i < field.getOptions().size(); i++) {
            FieldOption option = field.getOptions().get(i);
            if (!option.getValue().equals("other") || readOnly) {
                CheckBox check = new CheckBox(context);
                if (!option.getValue().equals("other") || (submission != null && !mAnswerHelper.hasOtherValue(submission.getAnswerForField(field.getId()),
                        field.getOptions()))) {
                    ((Activity) context).runOnUiThread(() -> check.setText(option.getLabel()));
                } else {
                    check.setChecked(true);
                    check.setText(mAnswerHelper.getOtherValue(submission.getAnswerForField(field.getId()), field.getOptions()));
                }
                check.setTag(option);
                check.setOnCheckedChangeListener((buttonView, isChecked) -> EventBus.getDefault().post(new FormUpdatedEvent()));
                group.addView(check);

                if (submission != null) {
                    check.setEnabled(!readOnly);
                    for (int j = 0; j < submission.getAnswers().size(); j++) {
                        Answer answer = submission.getAnswers().get(j);
                        if (answer.getFieldId() == field.getId()) {
                            String[] values = StringUtils.split(answer.getValues(), "\\\\");
                            if (Arrays.asList(values).contains(option.getValue()))
                                ((Activity) context).runOnUiThread(() -> check.setChecked(true));
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
        if (field.getOptions() != null) {
            for (FieldOption option : field.getOptions()) {
                if (!option.getValue().equals("other") || readOnly) {
                    final RadioButton radio = new RadioButton(context);
                    if (!option.getValue().equals("other") || (submission != null && !mAnswerHelper.hasOtherValue(submission.getAnswerForField(field.getId()),
                            field.getOptions()))) {
                        radio.setText(option.getLabel());
                    } else {
                        ((Activity) context).runOnUiThread(() -> radio.toggle());
                        radio.setText(mAnswerHelper.getOtherValue(submission.getAnswerForField(field.getId()),
                                field.getOptions()));
                    }

                    radio.setTag(option);
                    group.addView(radio);

                    if (submission != null) {
                        ((Activity) context).runOnUiThread(() -> radio.setEnabled(!readOnly));
                        for (int i = 0; i < submission.getAnswers().size(); i++) {
                            Answer answer = submission.getAnswers().get(i);
                            if (answer.getFieldId() == field.getId()) {
                                String[] values = StringUtils.split(answer.getValues(), "\\\\");
                                if (Arrays.asList(values).contains(option.getValue()))
                                    ((Activity) context).runOnUiThread(() -> radio.toggle());
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
                            EventBus.getDefault().post(new FormUpdatedEvent());
                        }
                    });
                    radio.setTag(option);
                    group.addView(radio);

                    if (submission != null && mAnswerHelper.hasOtherValue(submission.getAnswerForField(field.getId()),
                            field.getOptions())) {
                        TextView text = (TextView) radio.getChildAt(1);
                        text.setText(mAnswerHelper.getOtherValue(submission.getAnswerForField(field.getId()),
                                field.getOptions()));
                        ((Activity) context).runOnUiThread(() -> other.toggle());
                    }
                }
            }
        }
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            if (!others.isEmpty()) others.get(0).setChecked(false);
            EventBus.getDefault().post(new FormUpdatedEvent());
        });

        return view;
    }

    private View inflateSelectField(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission, boolean readOnly) {
        View view = fillCommonData(context, R.layout.field_select, container, field, fieldNumber, sectionNumber);

        Spinner spinner = ButterKnife.findById(view, R.id.field_select);
        ArrayAdapter<FieldOption> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                android.R.id.text1, field.getOptions() != null ? field.getOptions() : Collections.emptyList());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(new FormUpdatedEvent());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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
            for (int i = 0; i < submission.getAnswers().size(); i++) {
                Answer answer = submission.getAnswers().get(i);
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

    private View fillCommonData(Context context, @LayoutRes int layout, ViewGroup container, Field field,
                                int fieldNumber, int sectionNumber) {

        View view = LayoutInflater.from(context).inflate(layout, container, false);
        view.setTag(field);
        TextView label = ButterKnife.findById(view, R.id.title);
        label.setText(field.getLabel());

        TextView number = ButterKnife.findById(view, R.id.number);
        number.setText(String.format("%d.%d", sectionNumber, fieldNumber));

        TextView description = ButterKnife.findById(view, R.id.description);
        if (field.getDescription() == null || field.getDescription().isEmpty()) {
            description.setVisibility(View.GONE);
        } else {
            description.setText(field.getDescription());
        }

        return view;
    }

    public Answer extractValueForSubmission(View view) {
        if (view.getVisibility() == View.GONE) return null;

        Field field = (Field) view.getTag();
        switch (field.getType()) {
            case TEXT:
                return extractTextFieldValue(field, view);
            case CPF:
                return extractCpfFieldValue(field, view);
            case EMAIL:
                return extractEmailFieldValue(field, view);
            case MONEY:
                return extractMoneyFieldValue(field, view);
            case DATETIME:
                return extractDateTimeFieldValue(field, view);
            case CHECKBOX:
                return extractCheckboxFieldValue(field, view);
            case PRIVATE:
                return extractTextFieldValue(field, view);
            case RADIO:
                return extractRadioFieldValue(field, view);
            case SELECT:
                return extractSelectFieldValue(field, view);
            case URL:
                return extractUrlFieldValue(field, view);
            case ORDERED_LIST:
                return extractOrderedListFieldValue(field, view);
            case NUMBER:
                return extractNumberFieldValue(field, view);
        }

        return null;
    }

    private Answer extractOrderedListFieldValue(Field field, View view) {
        ViewGroup group = ButterKnife.findById(view, R.id.field_ordered_list);
        if (group == null) group = ButterKnife.findById(view, R.id.field_checkbox);

        List<String> values = new ArrayList<>();
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            FieldOption option = (FieldOption) child.getTag();
            values.add(option.getValue());
        }

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_ARRAY)
                .values(TextUtils.join("\\\\", values))
                .build();
    }

    private Answer extractCheckboxFieldValue(Field field, View view) {

        ViewGroup group = ButterKnife.findById(view, R.id.field_checkbox);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < group.getChildCount(); i++) {
            View checkView = group.getChildAt(i);
            if (checkView instanceof CheckBox) {
                CheckBox check = (CheckBox) checkView;
                if (check.isChecked()) {
                    FieldOption option = (FieldOption) check.getTag();
                    values.add(option.getValue());
                }
            } else {
                CheckBox check = (CheckBox) ((ViewGroup) checkView).getChildAt(0);
                if (check.isChecked()) {
                    String value = ((TextView) ((ViewGroup) checkView).getChildAt(1)).getText().toString().trim();
                    if (!value.isEmpty()) values.add(value);
                }
            }
        }

        if (values.isEmpty()) return null;

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_ARRAY)
                .values(TextUtils.join("\\\\", values))
                .build();
    }

    private Answer extractSelectFieldValue(Field field, View view) {
        Spinner spinner = ButterKnife.findById(view, R.id.field_select);
        FieldOption option = (FieldOption) spinner.getSelectedItem();

        if (option == null) {
            return Answer.builder()
                    .fieldId(field.getId())
                    .type(Answer.TYPE_STRING)
                    .format(Answer.FORMAT_ARRAY)
                    .values("")
                    .build();
        }

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_ARRAY)
                .values(option.getValue())
                .build();
    }

    private Answer extractRadioFieldValue(Field field, View view) {
        RadioGroup group = ButterKnife.findById(view, R.id.field_radio);
        String selected = null;
        for (int i = 0; i < group.getChildCount(); i++) {
            View radioView = group.getChildAt(i);
            if (radioView instanceof RadioButton) {
                RadioButton radio = (RadioButton) radioView;
                if (radio.isChecked()) {
                    FieldOption option = (FieldOption) radio.getTag();
                    selected = option.getValue();
                    break;
                }
            } else {
                RadioButton radio = (RadioButton) ((ViewGroup) radioView).getChildAt(0);
                if (radio.isChecked()) {
                    String value = ((TextView) ((ViewGroup) radioView).getChildAt(1)).getText().toString().trim();
                    if (!value.isEmpty()) selected = value;
                }
            }
        }

        if (selected == null) return null;

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_ARRAY)
                .values(selected)
                .build();
    }

    private Answer extractDateTimeFieldValue(Field field, View view) {
        TextView textField = ButterKnife.findById(view, R.id.field);
        String value = textField.getText().toString().trim();
        if (value.isEmpty()) return null;

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(textField.getTag().toString())
                .build();
    }

    private Answer extractMoneyFieldValue(Field field, View view) {
        TextView textField = ButterKnife.findById(view, R.id.field);
        String value = textField.getText().toString().trim();
        if (value.isEmpty()) return null;

        DecimalFormatSymbols format = DecimalFormatSymbols.getInstance();
        String groupingSeparator = String.valueOf(format.getGroupingSeparator());
        if (groupingSeparator.equals(".")) groupingSeparator = "\\.";
        String decimalSeparator = String.valueOf(format.getDecimalSeparator());
        if (decimalSeparator.equals(".")) decimalSeparator = "\\.";

        value = value.replaceAll("[^0-9.,]", "").replaceAll(groupingSeparator, "")
                .replaceAll(decimalSeparator, ".");

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(value) // FIXME maybe return double?
                .build();
    }

    private Answer extractNumberFieldValue(Field field, View view) {
        TextView textField = ButterKnife.findById(view, R.id.field);
        String value = textField.getText().toString().trim().replaceAll("[^0-9]", "");
        if (value.isEmpty()) return null;

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_NUMBER)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(value)
                .build();
    }

    private Answer extractCpfFieldValue(Field field, View view) {
        TextView textField = ButterKnife.findById(view, R.id.field);
        String value = textField.getText().toString().trim();
        if (value.isEmpty()) return null;

        if (!mFieldValidator.isValidCpf(value)) return null;

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(value.replace(".", "").replace("-", ""))
                .build();
    }

    private Answer extractTextFieldValue(Field field, View view) {
        TextView textField = ButterKnife.findById(view, R.id.field);
        String value = textField.getText().toString().trim();
        if (value.isEmpty()) return null;

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(value)
                .build();
    }

    private Answer extractEmailFieldValue(Field field, View view) {
        TextView textField = ButterKnife.findById(view, R.id.field);
        String value = textField.getText().toString().trim();
        if (value.isEmpty()) return null;

        if (!mFieldValidator.isValidEmail(value)) return null;

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(value)
                .build();
    }

    private Answer extractUrlFieldValue(Field field, View view) {
        TextView textField = ButterKnife.findById(view, R.id.field);
        String value = textField.getText().toString().trim();
        if (value.isEmpty()) return null;

        if (!mFieldValidator.isValidUrl(value)) return null;

        if (!value.startsWith("http")) value = "http://" + value;

        return Answer.builder()
                .fieldId(field.getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(value)
                .build();
    }

    public View inflateFilledReadOnlyWithCorrection(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission) {
        View view = inflateFilled(context, container, field, fieldNumber, sectionNumber, submission, true);
        if (hasCorrection(field, submission)) highlightTitle(view);
        return view;
    }

    private void highlightTitle(View view) {
        TextView label = ButterKnife.findById(view, R.id.title);
        label.setTextColor(ContextCompat.getColor(view.getContext(), R.color.color_15));
        TextView number = ButterKnife.findById(view, R.id.number);
        number.setTextColor(ContextCompat.getColor(view.getContext(), R.color.color_15));
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

        for (Answer answer : submission.getAnswers()) {
            if (answer.getFieldId() == field.getId())
                return true;
        }

        return false;
    }

    public boolean hasCorrection(Field field, UserSubmission submission) {
        if (submission.getCorrections() == null) return false;

        for (SubmissionCorrection correction : submission.getCorrections()) {
            if (correction.getFieldId() == field.getId()) return true;
        }

        return false;
    }

    public String getFormattedAnswer(Context context, FieldType type, Object answer) {
        switch (type) {
            case TEXT:
            case EMAIL:
            case NUMBER:
            case URL:
                return answer.toString();
            case CPF:
                return answer.toString().replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
            case MONEY:
                return getMoneyFormattedValue(answer.toString());
            case DATETIME:
                return DateUtils.formatShortDate(context, new DateTime(answer.toString()).toDate());
            case RADIO:
            case SELECT:
            case CHECKBOX:
            case ORDERED_LIST:
                return answer.toString();
            case PRIVATE:
                UserData user = mPreferences.getUser();
                if (user != null && user.getRole().equals(UserRole.AGENT)) {
                    return context.getString(R.string.message_private_answer);
                } else {
                    return answer.toString();
                }
        }
        return null;
    }

    private String getMoneyFormattedValue(String value) {
        Editable s = new Editable.Factory().newEditable(value);

        String digits = s.toString().replaceAll("\\D", "");
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        try {
            String formatted = nf.format(Double.parseDouble(digits) / 100);
            s.replace(0, s.length(), formatted);
            return s.toString();
        } catch (NumberFormatException nfe) {
            return "";
        }
    }

    public View inflateFilledReadOnlyWithComment(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission) {
        View view = inflateFilled(context, container, field, fieldNumber, sectionNumber, submission, true);

        if (view != null && hasCorrection(field, submission)) {
            highlightTitle(view);
            showModeratorComment(view, field, submission);
        }

        return view;
    }

    public View inflateFilledForCorrection(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission) {
        return hasCorrection(field, submission) ? inflateFilledForEdit(context, container, field, fieldNumber, sectionNumber, submission) : inflateFilled(context, container, field, fieldNumber, sectionNumber, submission, true);
    }

    private View inflateFilledForEdit(Context context, ViewGroup container, Field field, int fieldNumber, int sectionNumber, UserSubmission submission) {
        View view = inflateFilled(context, container, field, fieldNumber, sectionNumber, submission, false);

        highlightTitle(view);
        showModeratorComment(view, field, submission);

        return view;
    }

    private void showModeratorComment(View view, Field field, UserSubmission submission) {
        SubmissionCorrection correction = getCorrection(field, submission);
        mModeratorHelper.addCommentReadOnly(view.getContext(), view, correction);
    }

    private SubmissionCorrection getCorrection(Field field, UserSubmission submission) {
        if (submission.getCorrections() == null) return null;

        for (SubmissionCorrection correction : submission.getCorrections()) {
            if (correction.getFieldId() == field.getId()) return correction;
        }

        return null;
    }
}
