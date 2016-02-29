package br.org.institutotim.parapesquisa.ui.helper;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.ui.validator.SectionValidator;
import butterknife.ButterKnife;

public class SectionHelper {

    private final SectionValidator mValidator;

    public SectionHelper(SectionValidator validator) {
        this.mValidator = validator;
    }

    public boolean isLast(List<View> sections, int section) {
        for (int i = section; i < sections.size(); i++) {
            Boolean isActivated = (Boolean) sections.get(i).getTag(R.id.section_visibility);
            if (isActivated) return false;
        }

        return true;
    }

    public boolean isFirst(List<View> sections, int section) {
        for (int i = section - 2; i >= 0; i--) {
            Boolean isActivated = (Boolean) sections.get(i).getTag(R.id.section_visibility);
            if (isActivated) return false;
        }

        return true;
    }

    public int getSectionNumber(List<View> sections, int position) {
        position--;
        int number = 0;
        for (int i = 0; i < position; i++) {
            Boolean isActivated = (Boolean) sections.get(i).getTag(R.id.section_visibility);
            if (isActivated) number++;
        }
        return number + 1;
    }

    public int getTotalSections(List<View> sections) {
        int total = 0;
        for (View view : sections) {
            Boolean isActivated = (Boolean) view.getTag(R.id.section_visibility);
            if (isActivated) total++;
        }
        return total;
    }

    public boolean validateSections(List<View> sections) {
        boolean valid = true;
        for (View view : sections) {
            Boolean isActivated = (Boolean) view.getTag(R.id.section_visibility);
            if (isActivated) {
                valid = mValidator.validate(view) && valid;
            }
        }

        return valid;
    }

    public List<View> getFieldsFromSection(View section) {
        Boolean isActivated = (Boolean) section.getTag(R.id.section_visibility);
        if (!isActivated) return Collections.emptyList();

        ViewGroup container = ButterKnife.findById(section, R.id.container);
        List<View> views = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            views.add(container.getChildAt(i));
        }

        return views;
    }
}
