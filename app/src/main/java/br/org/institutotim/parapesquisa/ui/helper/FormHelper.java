package br.org.institutotim.parapesquisa.ui.helper;

import java.util.ArrayList;
import java.util.List;

import br.org.institutotim.parapesquisa.data.model.StopReason;

public class FormHelper {

    public String[] getStopReasonsArray(List<StopReason> reasons) {
        List<String> array = new ArrayList<>(reasons.size());
        for (int i = 0; i < reasons.size(); i++) {
            array.add(reasons.get(i).getReason());
        }
        return array.toArray(new String[array.size()]);
    }
}
