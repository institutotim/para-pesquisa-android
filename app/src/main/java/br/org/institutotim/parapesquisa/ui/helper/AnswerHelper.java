package br.org.institutotim.parapesquisa.ui.helper;

import android.support.annotation.Nullable;

import java.util.List;

import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.FieldOption;
import br.org.institutotim.parapesquisa.util.StringUtils;

public class AnswerHelper {

    public boolean hasOtherValue(@Nullable Answer answer, @Nullable List<FieldOption> options) {
        if (answer == null || options == null) return false;

        String[] values = StringUtils.split(answer.getValues(), "\\\\");
        for (String value : values) {
            if (!containsValue(value, options)) return true;
        }

        return false;
    }

    @Nullable
    public String getOtherValue(@Nullable Answer answer, List<FieldOption> options) {
        if (answer == null) return null;

        String[] values = StringUtils.split(answer.getValues(), "\\\\");
        for (String value : values) {
            if (!containsValue(value, options)) return value;
        }

        return null;
    }

    private boolean containsValue(String value, List<FieldOption> options) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getValue().equals(value)) return true;
        }
        return false;
    }
}
