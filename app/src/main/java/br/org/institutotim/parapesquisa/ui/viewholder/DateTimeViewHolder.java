package br.org.institutotim.parapesquisa.ui.viewholder;

import android.app.DatePickerDialog;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.util.DateUtils;
import butterknife.Bind;

public class DateTimeViewHolder extends BaseViewHolder {

    @Bind(R.id.field)
    public EditText field;

    public DateTimeViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void fillData(Field field, @Nullable Answer answer) {
        super.fillData(field, answer);

        this.field.setOnClickListener(v -> {
            DateTime date = (DateTime) v.getTag();
            if (date == null) date = DateTime.now();

            final DatePickerDialog dialog = new DatePickerDialog(v.getContext(), (view1, year, monthOfYear, dayOfMonth) -> {
                DateTime selectedDate = new DateTime().withTime(0, 0, 0, 0).withYear(year).withMonthOfYear(monthOfYear + 1).withDayOfMonth(dayOfMonth);
                DateTimeViewHolder.this.field.setTag(selectedDate);
                DateTimeViewHolder.this.field.setText(DateUtils.formatShortDate(v.getContext(), selectedDate.toDate()));
            }, date.getYear(), date.getMonthOfYear() - 1 /* damn Java Calendar... */, date.getDayOfMonth());

            dialog.show();
        });

        if (answer != null) {
            this.field.setText(DateUtils.formatShortDate(itemView.getContext(), answer.getValues()));

            DateTime date;
            if (answer.getValues().length() == 10) {
                try {
                    date = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(answer.getValues());
                } catch (Exception e) {
                    date = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(answer.getValues());
                }
            } else {
                date = new DateTime(answer.getValues());
            }

            this.field.setTag(date);
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
        String value = field.getText().toString().trim();
        if (value.isEmpty()) return null;

        return Answer.builder()
                .fieldId(getField().getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(getValue())
                .build();
    }

    private String getValue() {
        if (field.getTag() != null) {
            return field.getTag().toString();
        }

        return DateUtils.parseShortDate(itemView.getContext(), field.getText().toString().trim())
                .toString();
    }
}
