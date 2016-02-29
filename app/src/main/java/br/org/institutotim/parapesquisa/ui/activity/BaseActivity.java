package br.org.institutotim.parapesquisa.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;

import br.org.institutotim.parapesquisa.ParaPesquisaApp;
import br.org.institutotim.parapesquisa.ParaPesquisaComponent;
import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.api.response.ApiError;
import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String SUBMISSION_EXTRA = "br.org.institutotim.parapesquisa.submission_extra";
    public static final String FORM_EXTRA = "br.org.institutotim.parapesquisa.form_extra";
    public static final String RESCHEDULE_EXTRA = "br.org.institutotim.parapesquisa.reschedule_extra";
    public static final String STARTED_EXTRA = "br.org.institutotim.parapesquisa.started_extra";

    public static final int FORM_RESULT = 0xA;

    private ProgressDialog mProgressDialog;

    protected ParaPesquisaComponent getComponent() {
        return ParaPesquisaApp.get(this).getComponent();
    }

    protected void rebuildComponent() {
        ParaPesquisaApp.get(this).setupComponent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            Timber.d("This class doesn't have onEvent() methods");
        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    protected void hideKeyboard() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
    }

    protected void showSnackBar(@StringRes int message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    protected void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle(R.string.app_name);
            mProgressDialog.setMessage(getString(R.string.message_please_wait));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null) mProgressDialog.dismiss();
    }

    protected void showError(Throwable throwable) {
        if (throwable instanceof RetrofitError) {
            RetrofitError error = (RetrofitError) throwable;

            if (error.getKind().equals(RetrofitError.Kind.NETWORK)) {
                showSnackBar(getString(R.string.message_network_error));
            } else {
                try {
                    ApiError json = (ApiError) error.getBodyAs(ApiError.class);
                    showSnackBar(json.getErrorDescription());
                } catch (Exception e) {
                    Timber.w("ParaPesquisa", "It' not a server error", e);
                    showSnackBar(R.string.message_server_error);
                }
            }
        } else {
            showSnackBar(R.string.message_internal_error);
        }
    }
}
