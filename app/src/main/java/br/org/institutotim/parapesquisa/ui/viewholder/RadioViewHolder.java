package br.org.institutotim.parapesquisa.ui.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FieldOption;
import br.org.institutotim.parapesquisa.util.StringUtils;
import butterknife.Bind;

public class RadioViewHolder extends BaseViewHolder implements RadioGroup.OnCheckedChangeListener {

    @Bind(R.id.field_radio)
    RadioGroup container;

    EditText input;
    private boolean enableInput;

    public RadioViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void fillData(Field field, @Nullable Answer answer) {
        super.fillData(field, answer);
        fillingData = true;
        container.removeAllViews();
        container.setOnCheckedChangeListener(this);
        for (int i = 0; field.getOptions() != null && i < field.getOptions().size(); i++) {
            FieldOption option = field.getOptions().get(i);
            if (!option.getValue().equals("other")) {
                RadioButton radio = new AppCompatRadioButton(container.getContext());
                radio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        clearCheck(buttonView);
                        if (input != null) {
                            changeEnableForInput(false);
                            input.setText(null);
                        }
                    }
                });
                radio.setTag(option);
                radio.setText(option.getLabel());
                container.addView(radio, 0);
                checkAnswer(answer, radio);
            } else {
                ViewGroup radio = (ViewGroup) LayoutInflater.from(itemView.getContext()).inflate(R.layout.radio_other, container, false);
                radio.setTag(option);
                RadioButton other = (RadioButton) radio.getChildAt(0);
                input = (EditText) radio.getChildAt(1);
                changeEnableForInput(false);
                other.setTag(option);
                other.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        clearCheck(buttonView);
                        changeEnableForInput(true);
                        notifyListener(getField(), extractAnswer());
                    }
                });
                input.setHint(option.getLabel());
                container.addView(radio, 0);
                checkAnswer(answer, radio, field.getOptions());
                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (other.isChecked())
                            notifyListener(getField(), extractAnswer());
                    }
                });
            }
        }
        if (input != null) input.post(() -> input.setEnabled(enableInput));
        fillingData = false;
    }

    private void changeEnableForInput(final boolean enabled) {
        enableInput = enabled;
        if (input != null) input.setEnabled(enabled);
    }

    private void clearCheck(CompoundButton button) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);
            if (view instanceof RadioButton && !button.equals(view)) {
                ((RadioButton) view).setChecked(false);
            } else if (view instanceof ViewGroup) {
                RadioButton radio = (RadioButton) ((ViewGroup) view).getChildAt(0);
                if (!radio.equals(button)) {
                    radio.setChecked(false);
                }
            }
        }
    }

    private void checkAnswer(@Nullable Answer answer, ViewGroup group, @NonNull List<FieldOption> options) {
        if (answer == null) {
            return;
        }

        final String[] values = StringUtils.split(answer.getValues(), "\\\\");
        boolean found = false;
        String value = null;
        if (values != null && values.length > 0) {
            value = values[0];
            for (int i = 0; i < options.size(); i++) {
                found = found || options.get(i).getValue().equals(value);
            }
        }

        if (!found) {
            RadioButton radio = (RadioButton) group.getChildAt(0);
            radio.toggle();
            EditText text = (EditText) group.getChildAt(1);
            text.setText(value);
        }
    }

    private void checkAnswer(@Nullable Answer answer, RadioButton radio) {
        if (answer == null) {
            return;
        }

        FieldOption option = (FieldOption) radio.getTag();
        String[] values = StringUtils.split(answer.getValues(), "\\\\");
        if (Arrays.asList(values).contains(option.getValue())) {
            radio.toggle();
        }
    }

    @Override
    public Answer extractAnswer() {
        String value = getValue();

        return value != null ? Answer.builder()
                .fieldId(getField().getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_ARRAY)
                .values(getValue())
                .build() : null;
    }

    @Nullable
    private String getValue() {
        String selected = null;
        for (int i = 0; i < container.getChildCount(); i++) {
            View radioView = container.getChildAt(i);
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
                    String value = ((TextView) ((ViewGroup) radioView).getChildAt(1))
                            .getText().toString().trim();
                    selected = value;
                }
            }
        }

        return selected;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        notifyListener(getField(), extractAnswer());
    }

}
