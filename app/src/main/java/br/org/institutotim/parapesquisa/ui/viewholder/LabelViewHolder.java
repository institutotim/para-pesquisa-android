package br.org.institutotim.parapesquisa.ui.viewholder;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import butterknife.Bind;

import static butterknife.ButterKnife.bind;

public class LabelViewHolder extends BaseViewHolder {

    @Bind(R.id.title)
    public TextView title;

    public LabelViewHolder(View itemView) {
        super(itemView);
        bind(this, itemView);
    }

    @Override
    public void fillData(Field field, @Nullable Answer answer) {
        super.fillData(field, answer);
        title.setText(field.getLabel());
    }

    @Override
    public Answer extractAnswer() {
        return null;
    }
}
