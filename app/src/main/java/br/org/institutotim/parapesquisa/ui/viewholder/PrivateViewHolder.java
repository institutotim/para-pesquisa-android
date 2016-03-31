package br.org.institutotim.parapesquisa.ui.viewholder;

import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.ParaPesquisaApp;
import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserRole;
import butterknife.Bind;

import static butterknife.ButterKnife.bind;

/**
 * Created by rafael on 11/12/15.
 */
public class PrivateViewHolder extends BaseViewHolder {

    @Bind(R.id.field)
    EditText field;
    @Bind(R.id.confirm)
    Button confirm;

    @Inject
    ParaPesquisaPreferences preferences;

    public PrivateViewHolder(View itemView) {
        super(itemView);
        bind(this, itemView);
        ParaPesquisaApp.get(field.getContext()).getComponent().inject(this);
    }

    @Override
    public void fillData(Field field, @Nullable Answer answer) {
        super.fillData(field, answer);
        this.field.setInputType(InputType.TYPE_CLASS_TEXT);

        if (answer != null) {
            this.field.setEnabled(Boolean.FALSE);
            this.field.setText(answer.getValues());

            final UserData user = preferences.getUser();

            if (user != null && UserRole.AGENT.equals(user.getRole())) {
                if (field.isReadOnly()) {
                    this.field.setText(this.field.getContext().getString(R.string.message_private_answer));
                } else {
                    this.field.setInputType(this.field.getInputType() | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }

            confirm.setVisibility(View.GONE);
        } else {
            confirm.setOnClickListener(v -> {
                if (!this.field.getText().toString().trim().isEmpty()) {
                    notifyListener(field, extractAnswer());
                    this.field.setInputType(this.field.getInputType() | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    this.field.setEnabled(Boolean.FALSE);
                    confirm.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public Answer extractAnswer() {
        return Answer.builder()
                .fieldId(getField().getId())
                .type(Answer.TYPE_STRING)
                .format(Answer.FORMAT_SINGLE_VALUE)
                .values(this.field.getText().toString())
                .lastValues("")
                .build();
    }

}
