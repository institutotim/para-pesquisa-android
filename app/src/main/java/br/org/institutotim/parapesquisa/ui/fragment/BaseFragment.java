package br.org.institutotim.parapesquisa.ui.fragment;

import android.support.v4.app.Fragment;

import br.org.institutotim.parapesquisa.ParaPesquisaApp;
import br.org.institutotim.parapesquisa.ParaPesquisaComponent;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

public abstract class BaseFragment extends Fragment {

    public static final String FILTER_EXTRA = "br.org.institutotim.parapesquisa.filter_extra";
    public static final String FORM_EXTRA = "br.org.institutotim.parapesquisa.form_extra";
    public static final String TEXT_EXTRA = "br.org.institutotim.parapesquisa.text_extra";
    public static final String USER_EXTRA = "br.org.institutotim.parapesquisa.user_extra";

    protected ParaPesquisaComponent getComponent() {
        return ParaPesquisaApp.get(getContext()).getComponent();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            Timber.d(e, "No onEvent() methods found on " + getClass().getName());
        }
    }
}
