package br.org.institutotim.parapesquisa.ui.viewholder;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import butterknife.Bind;

import static butterknife.ButterKnife.bind;

/**
 * Created by rafael on 11/12/15.
 */
public class EmailViewHolder extends BaseViewHolder {

    @Bind(R.id.field)
    public EditText field;

    public EmailViewHolder(View itemView) {
        super(itemView);
        bind(this, itemView);
    }

    @Override
    public void fillData(Field field, @Nullable Answer answer) {
        super.fillData(field, answer);
        this.field.setInputType(InputType.TYPE_CLASS_TEXT | this.field.getInputType());

        if (answer != null) {
            this.field.setText(answer.getValues());
        }

        this.field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                notifyListener(getField(), extractAnswer());
            }
        });
    }

    @Override
    public Answer extractAnswer() {
        String value = field.getText().toString().trim();
        if (value.isEmpty()) return null;

        return Answer.builder()
                .fieldId(getField().getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(value)
                .lastValues("")
                .build();
    }

}