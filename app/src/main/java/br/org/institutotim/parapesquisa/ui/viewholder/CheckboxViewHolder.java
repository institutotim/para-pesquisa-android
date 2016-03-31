package br.org.institutotim.parapesquisa.ui.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FieldOption;
import br.org.institutotim.parapesquisa.util.StringUtils;
import butterknife.Bind;

public class CheckboxViewHolder extends BaseViewHolder implements CompoundButton.OnCheckedChangeListener {

    @Bind(R.id.field_checkbox)
    LinearLayout container;

    private String lastValue;

    public CheckboxViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void fillData(Field field, @Nullable Answer answer) {
        super.fillData(field, answer);
        fillingData = true;
        container.removeAllViews();

        for (int i = 0; field.getOptions() != null && i < field.getOptions().size(); i++) {
            FieldOption option = field.getOptions().get(i);
            if (!option.getValue().equals("other")) {
                CheckBox check = new AppCompatCheckBox(container.getContext());
                check.setTag(option);
                check.setText(option.getLabel());
                container.addView(check, 0);
                check.setOnCheckedChangeListener(this);
                checkAnswer(answer, check);
            } else {
                ViewGroup radio = (ViewGroup) LayoutInflater.from(itemView.getContext()).inflate(R.layout.checkbox_other, container, false);
                radio.setTag(option);
                CheckBox other = (CheckBox) radio.getChildAt(0);
                other.setTag(option);
                other.setOnCheckedChangeListener(this);
                EditText input = (EditText) radio.getChildAt(1);
                input.setHint(option.getLabel());
                container.addView(radio, 0);
                checkAnswer(answer, radio, field.getOptions());
            }
        }
        fillingData = false;
    }

    private void checkAnswer(@Nullable Answer answer, ViewGroup group, @NonNull List<FieldOption> options) {
        if (answer == null) {
            return;
        }

        final List<String> values = Arrays.asList(StringUtils.split(answer.getValues(), "\\\\"));
        boolean found = false;
        for (int i = 0; i < options.size(); i++) {
            found = found || values.contains(options.get(i).getValue());
        }

        if (!found) {
            CheckBox checkBox = (CheckBox) group.getChildAt(0);
            checkBox.toggle();
            EditText text = (EditText) group.getChildAt(1);
            text.setText(values.get(0));
        }
    }

    private void checkAnswer(@Nullable Answer answer, CheckBox checkBox) {
        if (answer == null) {
            return;
        }

        FieldOption option = (FieldOption) checkBox.getTag();
        String[] values = StringUtils.split(answer.getValues(), "\\\\");
        if (Arrays.asList(values).contains(option.getValue())) {
            checkBox.toggle();
        }
    }

    @Override
    public Answer extractAnswer() {
        List<String> values = getValues();
        String value = TextUtils.join("\\\\", values);

        Answer answer = !values.isEmpty() ? Answer.builder()
                .fieldId(getField().getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_ARRAY)
                .values(value)
                .lastValues(lastValue == null ? "" : lastValue)
                .build() : null;
        lastValue = value;
        return answer;
    }

    @NonNull
    private List<String> getValues() {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < container.getChildCount(); i++) {
            View checkView = container.getChildAt(i);
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

        return values;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        notifyListener(getField(), extractAnswer());
    }

}
