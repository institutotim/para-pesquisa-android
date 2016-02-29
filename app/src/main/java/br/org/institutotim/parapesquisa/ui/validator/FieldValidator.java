package br.org.institutotim.parapesquisa.ui.validator;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Arrays;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FieldType;
import br.org.institutotim.parapesquisa.data.model.FieldValidation;
import butterknife.ButterKnife;
import timber.log.Timber;

public class FieldValidator {

    @StringRes
    public int validate(@NonNull Field field, Answer answer) {
        if (Arrays.asList(FieldType.SELECT, FieldType.ORDERED_LIST, FieldType.LABEL)
                .contains(field.getType())) {
            return 0;
        }

        if (answer == null || answer.getValues().trim().isEmpty()) {
            return Boolean.TRUE.equals(field.getValidations().getRequired()) ?
                    R.string.message_validation_required : 0;
        }

        switch (field.getType()) {
            case CPF:
                return validateCpfField(answer, field.getValidations());
            case EMAIL:
                return validateEmailField(answer, field.getValidations());
            case URL:
                return validateUrlField(answer, field.getValidations());
            //case CHECKBOX:
            //    return validateCheckboxField(answer, field.getValidations());
            //case RADIO:
            //    return validateRadioField(answer, field.getValidations());
            case NUMBER:
                return validateNumberField(answer, field.getValidations());
            default:
                return 0;
        }
    }

    public boolean validate(View fieldView) {
        Field field = (Field) fieldView.getTag();
        if (fieldView.getVisibility() == View.GONE) {
            // Field hidden.
            return true;
        }

        resetView(fieldView);

        switch (field.getType()) {
            case TEXT:
            case PRIVATE:
                return validateTextField(fieldView, field.getValidations());
            case CPF:
                return validateCpfField(fieldView, field.getValidations());
            case MONEY:
                return validateMoneyField(fieldView, field.getValidations());
            case EMAIL:
                return validateEmailField(fieldView, field.getValidations());
            case URL:
                return validateUrlField(fieldView, field.getValidations());
            case DATETIME:
                return validateDateTimeField(fieldView, field.getValidations());
            case CHECKBOX:
                return validateCheckboxField(fieldView, field.getValidations());
            case RADIO:
                return validateRadioField(fieldView, field.getValidations());
            case NUMBER:
                return validateNumberField(fieldView, field.getValidations());
            case SELECT:
            case ORDERED_LIST:
            case LABEL:
                return true;
        }

        return false;
    }

    private boolean validateMoneyField(View view, FieldValidation validation) {
        TextView textView = ButterKnife.findById(view, R.id.field);

        if (validation.getRequired() != null && validation.getRequired() && textView.getText().toString().trim().isEmpty()) {
            showError(view, R.string.message_validation_required);
            return false;
        }

        return true;
    }

    private int validateUrlField(Answer answer, FieldValidation validation) {
        if (validation.getRequired() != null && validation.getRequired() && answer.getValues().trim().isEmpty()) {
            return R.string.message_validation_required;
        }

        String value = answer.getValues().trim();
        if (!value.startsWith("http")) value = "http://" + value;
        if (!Patterns.WEB_URL.matcher(value).matches()) {
            return R.string.message_invalid_value;
        }

        return 0;
    }

    private boolean validateUrlField(View view, FieldValidation validation) {
        TextView textView = ButterKnife.findById(view, R.id.field);

        if (validation.getRequired() != null && validation.getRequired() && textView.getText().toString().trim().isEmpty()) {
            showError(view);
            return false;
        }

        String value = textView.getText().toString().trim();
        if (!value.startsWith("http")) value = "http://" + value;
        if (!Patterns.WEB_URL.matcher(value).matches()) {
            showError(view);
            return false;
        }

        return true;
    }

    private boolean validateEmailField(View view, FieldValidation validation) {
        TextView textView = ButterKnife.findById(view, R.id.field);

        if (validation.getRequired() == null && textView.getText().toString().trim().isEmpty()) {
            return true;
        }

        if (validation.getRequired() != null && validation.getRequired() && textView.getText().toString().trim().isEmpty()) {
            showError(view);
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(textView.getText()).matches()) {
            showError(view);
            return false;
        }

        return true;

    }

    private int validateEmailField(Answer answer, FieldValidation validation) {
        if (!Patterns.EMAIL_ADDRESS.matcher(answer.getValues().trim()).matches()) {
            return R.string.message_invalid_value;
        }

        return 0;
    }

    private boolean validateDateTimeField(View view, FieldValidation validation) {
        if (validation.getRequired() != null && validation.getRequired() && ButterKnife.findById(view, R.id.field).getTag() == null) {
            showError(view);

            return false;
        }

        return true;
    }

    private boolean validateTextField(View view, FieldValidation validation) {
        if (validation.getRequired() != null && validation.getRequired() && ((TextView) ButterKnife.findById(view, R.id.field)).getText().toString().trim().isEmpty()) {
            showError(view);

            return false;
        }

        return true;
    }

    private int validateCpfField(Answer answer, FieldValidation validation) {
        if (!isValidCpf(answer.getValues().trim())) {
            return R.string.message_invalid_value;
        }

        return 0;
    }

    private boolean validateCpfField(View view, FieldValidation validation) {
        if (validation.getRequired() != null && validation.getRequired() && ((TextView) ButterKnife.findById(view, R.id.field)).getText().toString().trim().isEmpty()) {
            showError(view);
            return false;
        }

        if (!isValidCpf(((TextView) ButterKnife.findById(view, R.id.field)).getText().toString().trim())) {
            showError(view, R.string.message_invalid_value);
            return false;
        }

        return true;
    }

    private int validateNumberField(Answer answer, FieldValidation validation) {
        long value;
        try {
            value = Long.parseLong(answer.getValues().trim());
        } catch (NumberFormatException exception) {
            Timber.d(exception, "Error in the conversion of long");
            return R.string.message_invalid_value;
        }
        if (validation.getRange() != null && value >= validation.getRange().get(0) && value <= validation.getRange().get(1)) {
            return 0;
        }

        if (validation.getRange() == null) return 0;

        return R.string.message_validation_out_of_range;
    }

    private boolean validateNumberField(View view, FieldValidation validation) {
        if (!validateTextField(view, validation)) return false;

        String rawValue = ((TextView) ButterKnife.findById(view, R.id.field)).getText().toString().trim().replaceAll("[^0-9]", "");

        if (rawValue.equals("")) {
            boolean valid = validation.getRequired() == null;
            if (!valid) showError(view, R.string.message_validation_required);
            return valid;
        }

        long value;
        try {
            value = Long.parseLong(rawValue);
        } catch (NumberFormatException exception) {
            Timber.d(exception, "Error in the conversion of long");
            return false;
        }
        if (validation.getRange() != null && value >= validation.getRange().get(0) && value <= validation.getRange().get(1)) {
            return true;
        }

        if (validation.getRange() == null) return true;

        showError(view, R.string.message_validation_out_of_range);
        return false;
    }

    private boolean validateRadioField(View view, FieldValidation validation) {
        if (validation.getRequired() != null && validation.getRequired()) {
            RadioGroup group = ButterKnife.findById(view, R.id.field_radio);
            boolean checked = false;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);

                RadioButton radio;
                if (child instanceof RadioButton) {
                    radio = (RadioButton) child;
                } else {
                    radio = (RadioButton) ((ViewGroup) child).getChildAt(0);
                    TextView content = (TextView) ((ViewGroup) child).getChildAt(1);
                    if (content.getText().toString().trim().isEmpty()) radio.setChecked(false);
                }

                checked = radio.isChecked() || checked;
            }

            if (!checked) {
                showError(view);
                return false;
            }
        }

        resetView(view);
        return true;
    }

    private boolean validateCheckboxField(View view, FieldValidation validation) {
        if (validation.getRequired() != null && validation.getRequired()) {
            ViewGroup group = ButterKnife.findById(view, R.id.field_checkbox);
            boolean checked = false;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);

                CheckBox checkBox;
                if (child instanceof CheckBox) {
                    checkBox = (CheckBox) child;
                } else {
                    checkBox = (CheckBox) ((ViewGroup) child).getChildAt(0);
                    TextView content = (TextView) ((ViewGroup) child).getChildAt(1);
                    if (content.getText().toString().trim().isEmpty()) checkBox.setChecked(false);
                }

                checked = checkBox.isChecked() || checked;
            }

            if (!checked) {
                showError(view);
                return false;
            }
        }

        resetView(view);
        return true;
    }

    public boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidUrl(String url) {
        if (!url.startsWith("http")) url = "http://" + url;
        return Patterns.WEB_URL.matcher(url).matches();
    }

    public boolean isValidCpf(String cpf) {
        cpf = cpf.replace(".", "").replace("-", "").trim();
        return CpfValidator.isValid(cpf);
    }

    private void showError(View view) {
        showError(view, R.string.message_validation_required);
    }

    private void showError(View view, @StringRes int message) {
        TextView messageView = ButterKnife.findById(view, R.id.validation_message);
        TextView title = ButterKnife.findById(view, R.id.title);

        messageView.setVisibility(View.VISIBLE);
        messageView.setText(message);
        title.setTextColor(view.getContext().getResources().getColor(R.color.color_15));
    }

    private void resetView(View view) {
        TextView message = ButterKnife.findById(view, R.id.validation_message);
        TextView title = ButterKnife.findById(view, R.id.title);

        if (message != null) message.setVisibility(View.GONE);
        title.setTextColor(view.getContext().getResources().getColor(R.color.color_7));
    }
}
