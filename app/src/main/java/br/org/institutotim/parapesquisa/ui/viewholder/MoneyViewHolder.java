package br.org.institutotim.parapesquisa.ui.viewholder;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import java.text.DecimalFormatSymbols;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.ui.watcher.CurrencyTextWatcher;
import butterknife.Bind;

import static butterknife.ButterKnife.bind;

/**
 * Created by rafael on 11/12/15.
 */
public class MoneyViewHolder extends BaseViewHolder {

    @Bind(R.id.field)
    public EditText field;

    public MoneyViewHolder(View itemView) {
        super(itemView);
        bind(this, itemView);
    }

    @Override
    public void fillData(Field field, @Nullable Answer answer) {
        super.fillData(field, answer);
        this.field.setInputType(InputType.TYPE_CLASS_TEXT | this.field.getInputType());
        this.field.addTextChangedListener(new CurrencyTextWatcher(this.field));

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

        DecimalFormatSymbols format = DecimalFormatSymbols.getInstance();
        String groupingSeparator = String.valueOf(format.getGroupingSeparator());
        if (groupingSeparator.equals(".")) groupingSeparator = "\\.";
        String decimalSeparator = String.valueOf(format.getDecimalSeparator());
        if (decimalSeparator.equals(".")) decimalSeparator = "\\.";

        value = value.replaceAll("[^0-9.,]", "").replaceAll(groupingSeparator, "")
                .replaceAll(decimalSeparator, ".");

        return Answer.builder()
                .fieldId(getField().getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(value) // FIXME maybe return double?
                .lastValues("")
                .build();
    }

}