package br.org.institutotim.parapesquisa.ui.viewholder;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import butterknife.Bind;

import static butterknife.ButterKnife.bind;

public class NumberViewHolder extends BaseViewHolder {

    @Bind(R.id.field)
    public EditText field;

    public NumberViewHolder(View itemView) {
        super(itemView);
        bind(this, itemView);
    }

    @Override
    public void fillData(Field field, @Nullable Answer answer) {
        super.fillData(field, answer);
        this.field.setInputType(InputType.TYPE_CLASS_NUMBER);

        if (answer != null) {
            this.field.setText(answer.getValues());
        } else {
            this.field.setText(null);
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
        return Answer.builder()
                .fieldId(getField().getId())
                .type(Answer.TYPE_NUMBER)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(this.field.getText().toString())
                .build();
    }
}
