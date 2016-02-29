package br.org.institutotim.parapesquisa.ui.validator;

import android.view.View;
import android.view.ViewGroup;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.event.ScrollEvent;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class SectionValidator {

    private final FieldValidator mFieldValidator;

    public SectionValidator(FieldValidator fieldValidator) {
        this.mFieldValidator = fieldValidator;
    }

    public boolean validate(View sectionView) {
        ViewGroup container = ButterKnife.findById(sectionView, R.id.container);
        boolean valid = true;
        boolean eventSent = false;
        for (int i = 0; i < container.getChildCount(); i++) {
            valid = mFieldValidator.validate(container.getChildAt(i)) && valid;

            if (!valid && !eventSent) {
                EventBus.getDefault().post(new ScrollEvent(container.getChildAt(i).getTop()));
                eventSent = true;
            }
        }

        return valid;
    }
}
