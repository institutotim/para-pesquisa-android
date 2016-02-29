package br.org.institutotim.parapesquisa.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.api.ParaPesquisaApi;
import br.org.institutotim.parapesquisa.data.api.request.SignInRequest;
import br.org.institutotim.parapesquisa.data.api.response.SignInData;
import br.org.institutotim.parapesquisa.data.api.response.SingleResponse;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.event.SyncCompletedEvent;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserRole;
import br.org.institutotim.parapesquisa.service.AgentUpdateService;
import br.org.institutotim.parapesquisa.service.ModeratorUpdateService;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

public class SignInActivity extends BaseActivity implements Observer<SingleResponse<UserData>> {

    @Bind(R.id.server_location)
    EditText serverLocation;
    @Bind(R.id.username)
    EditText username;
    @Bind(R.id.password)
    EditText password;

    @Bind(R.id.sign_in)
    Button signIn;
    @Bind(R.id.progress)
    ProgressBar progress;

    @Inject
    ParaPesquisaPreferences preferences;
    @Inject
    ParaPesquisaApi api;
    @Inject
    ParaPesquisaOpenHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
        getComponent().inject(this);

        serverLocation.setText(preferences.getServerUrl());
    }

    @OnClick(R.id.sign_in)
    public void performSignIn() {
        if (isDataFilled()) {
            hideKeyboard();
            preferences.setServerUrl(serverLocation.getText());
            rebuildComponent();
            getComponent().inject(this);

            progress.setVisibility(View.VISIBLE);
            signIn.setVisibility(View.INVISIBLE);

            api.signIn(new SignInRequest(
                    username.getText().toString(),
                    password.getText().toString()))
                    .flatMap(new Func1<SingleResponse<SignInData>, Observable<SingleResponse<UserData>>>() {
                        @Override
                        public Observable<SingleResponse<UserData>> call(SingleResponse<SignInData> response) {
                            preferences.setSessionId(response.getResponse().getSessionId());
                            return api.getUser(response.getResponse().getUserId());
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this);

        }
    }

    private boolean isDataFilled() {
        return !isEmpty(serverLocation.getText()) &&
                !isEmpty(username.getText()) &&
                !isEmpty(password.getText());
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        progress.setVisibility(View.GONE);
        signIn.setVisibility(View.VISIBLE);

        password.setText("");

        Snackbar.make(findViewById(android.R.id.content), R.string.message_sign_in_failed,
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onNext(SingleResponse<UserData> response) {
        preferences.setUser(response.getResponse());

        startService(new Intent(this, response.getResponse().getRole().equals(UserRole.AGENT) ?
                AgentUpdateService.class : ModeratorUpdateService.class));
    }

    public void onEventMainThread(SyncCompletedEvent event) {
        progress.setVisibility(View.GONE);
        signIn.setVisibility(View.VISIBLE);

        switch (event.getResult()) {
            case SUCCESS:
                UserData user = preferences.getUser();
                if (user != null) {
                    Intent intent = new Intent(this, user.getRole().equals(UserRole.AGENT) ?
                            AgentMainActivity.class : ModeratorMainActivity.class);
                    intent.putExtra("sign_in", true);
                    startActivity(intent);
                    finish();
                }
                break;
            case ERROR:
                signIn.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                mHelper.clearAll();
                showSnackBar(R.string.message_sign_in_failed);
                break;
        }
    }
}
