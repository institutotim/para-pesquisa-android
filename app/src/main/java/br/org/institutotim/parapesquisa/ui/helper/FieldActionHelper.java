package br.org.institutotim.parapesquisa.ui.helper;

import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Arrays;

import br.org.institutotim.parapesquisa.data.event.FieldActionEvent;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FieldAction;
import br.org.institutotim.parapesquisa.data.model.FieldOption;
import br.org.institutotim.parapesquisa.util.StringUtils;
import de.greenrobot.event.EventBus;

public class FieldActionHelper {

    public void verifyAction(Field field, Answer value) {
        if (value == null || !value.getValues().isEmpty()) {
            return;
        }

        for (int i = 0; i < field.getActions().size(); i++) {
            FieldAction action = field.getActions().get(i);
            switch (field.getType()) {
                case RADIO:
                case SELECT:
                case CHECKBOX:
                    if(action.getWhen().containsAll(Arrays.asList(StringUtils.split(value.getValues(), "\\\\")))) {
                        sendEvent(action, true);
                    }
                    break;
            }
        }
    }

    public void verifyAction(Field field, FieldOption option, boolean isChecked) {
        for (int i = 0; i < field.getActions().size(); i++) {
            FieldAction action = field.getActions().get(i);
            if (action.getWhen().contains(option.getValue())) {
                sendEvent(action, isChecked);
            }
        }
    }

    public void verifyAction(Field field, RadioGroup group) {
        for (int j = 0; j < field.getActions().size(); j++) {
            FieldAction action = field.getActions().get(j);
            for (int i = 0; i < group.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) group.getChildAt(i);
                FieldOption option = (FieldOption) radioButton.getTag();
                if (action.getWhen() != null && action.getWhen().contains(option.getValue())) {
                    sendEvent(action, radioButton.isChecked());
                }
            }
        }
    }

    public void verifyAction(Field field, int position) {
        for (int j = 0; j < field.getActions().size(); j++) {
            FieldAction action = field.getActions().get(j);
            if (field.getOptions() != null) {
                for (int i = 0; i < field.getOptions().size(); i++) {
                    if (action.getWhen().contains(field.getOptions().get(i).getValue())) {
                        sendEvent(action, i == position);
                    }
                }
            }
        }
    }

    private void sendEvent(FieldAction action, boolean isChecked) {
        if (action.getDisable() != null) EventBus.getDefault().post(new FieldActionEvent(action.getDisable(), isChecked, FieldActionEvent.DISABLE));
        if (action.getEnable() != null) EventBus.getDefault().post(new FieldActionEvent(action.getEnable(), isChecked, FieldActionEvent.ENABLE));
        if (action.getDisableSections() != null) EventBus.getDefault().post(new FieldActionEvent(action.getDisableSections(), isChecked, FieldActionEvent.DISABLE_SECTION));
    }
}
