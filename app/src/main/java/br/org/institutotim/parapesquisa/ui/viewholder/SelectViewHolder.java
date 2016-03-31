package br.org.institutotim.parapesquisa.ui.viewholder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.Collections;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FieldOption;
import br.org.institutotim.parapesquisa.util.StringUtils;
import butterknife.Bind;

import static butterknife.ButterKnife.bind;

/**
 * Created by rafael on 11/12/15.
 */
public class SelectViewHolder extends BaseViewHolder{

    @Bind(R.id.field_select)
    Spinner spinner;

    private String lastValue;

    public SelectViewHolder(View itemView) {
        super(itemView);
        bind(this, itemView);
    }

    @Override
    public void fillData(Field field, @Nullable Answer answer) {
        super.fillData(field, answer);
        fillingData = true;

        if (field.getOptions() == null) return;

        final Context context = spinner.getContext();
        ArrayAdapter<FieldOption> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                android.R.id.text1, field.getOptions() != null ? field.getOptions() : Collections.emptyList());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                notifyListener(field, extractAnswer());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        checkAnswer(answer, spinner);
        fillingData = false;
    }

    private void checkAnswer(@Nullable Answer answer, Spinner spinner) {
        if (answer == null) {
            return;
        }

        final String value = StringUtils.split(answer.getValues(), "\\\\")[0];
        final SpinnerAdapter spinnerAdapter = spinner.getAdapter();
        for (int index = 0; index < spinnerAdapter.getCount(); index++) {
            final FieldOption fieldOption = (FieldOption) spinnerAdapter.getItem(index);
            if (value.equals(fieldOption.getValue())) {
                spinner.setSelection(index);
                break;
            }
        }
    }

    @Override
    public Answer extractAnswer() {
        FieldOption option = (FieldOption) spinner.getSelectedItem();
        Answer answer;
        if (option == null) {
            answer = Answer.builder()
                    .fieldId(getField().getId())
                    .type(Answer.TYPE_STRING)
                    .format(Answer.FORMAT_ARRAY)
                    .values("")
                    .lastValues(lastValue == null ? "" : lastValue)
                    .build();
            lastValue = null;
        } else {
            answer = Answer.builder()
                    .fieldId(getField().getId())
                    .type(Answer.TYPE_STRING)
                    .format(Answer.FORMAT_ARRAY)
                    .values(option.getValue())
                    .lastValues(lastValue == null ? "" : lastValue)
                    .build();
            lastValue = option.getValue();
        }
        return answer;
    }

}
