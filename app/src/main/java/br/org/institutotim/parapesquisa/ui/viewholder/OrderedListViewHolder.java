package br.org.institutotim.parapesquisa.ui.viewholder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jmedeisis.draglinearlayout.DragLinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FieldOption;
import br.org.institutotim.parapesquisa.util.StringUtils;
import butterknife.Bind;
import butterknife.ButterKnife;

import static butterknife.ButterKnife.bind;

/**
 * Created by rafael on 11/12/15.
 */
public class OrderedListViewHolder extends BaseViewHolder {

    @Bind(R.id.field_ordered_list)
    DragLinearLayout field;

    public OrderedListViewHolder(View itemView) {
        super(itemView);
        bind(this, itemView);
    }

    @Override
    public void fillData(Field field, @Nullable Answer answer) {
        super.fillData(field, answer);

        if (this.field.getChildCount() > 0) {
            for (int i = 0; i < this.field.getChildCount(); i++) {
                this.field.removeDragView(this.field.getChildAt(i));
            }
        }
        this.field.removeAllViews();

        final Context context = this.field.getContext();
        final List<FieldOption> fieldOptions = field.getOptions();
        if (fieldOptions != null) {
            if (answer == null) {
                for (int i = 0; i < fieldOptions.size(); i++) {
                    FieldOption fieldOption = fieldOptions.get(i);
                    addItem(context, fieldOption);
                }
            } else {
                String[] values = StringUtils.split(answer.getValues(), "\\\\");
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    FieldOption fieldOption = findFieldOptionByValue(fieldOptions, value);
                    addItem(context, fieldOption);
                }
            }
        }

        this.field.setOnViewSwapListener((firstView, firstPosition, secondView, secondPosition) -> {
            notifyListener(field, extractAnswer(firstPosition, secondPosition));
        });
    }

    private FieldOption findFieldOptionByValue(final List<FieldOption> fieldOptions, final String value) {
        for (int i = 0; i < fieldOptions.size(); i++) {
            FieldOption fieldOption = fieldOptions.get(i);
            if (fieldOption.getValue().equals(value)) {
                return fieldOption;
            }
        }
        return null;
    }

    private void addItem(Context context, FieldOption option) {
        View item = LayoutInflater.from(context).inflate(R.layout.item_list_draggable, this.field, false);
        TextView textView = ButterKnife.findById(item, R.id.title);
        textView.setText(option.getLabel());
        item.setTag(option);
        this.field.addView(item);
        this.field.setViewDraggable(item, item);
    }

    private Answer extractAnswer(int firstPosition, int secondPosition) {
        if (field == null) return null;

        List<String> values = new ArrayList<>();
        for (int i = 0; i < field.getChildCount(); i++) {
            View child = field.getChildAt(i);
            FieldOption option = (FieldOption) child.getTag();
            values.add(option.getValue());
        }

        Collections.swap(values, firstPosition, secondPosition);

        return Answer.builder()
                .fieldId(getField().getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_ARRAY)
                .values(TextUtils.join("\\\\", values))
                .build();
    }

    @Override
    public Answer extractAnswer() {
        return extractAnswer(0, 0);
    }

}