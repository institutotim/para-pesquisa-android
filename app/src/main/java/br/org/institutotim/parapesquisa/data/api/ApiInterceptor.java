package br.org.institutotim.parapesquisa.data.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import retrofit.RequestInterceptor;

@Singleton
public class ApiInterceptor implements RequestInterceptor {

    private final ParaPesquisaPreferences preferences;

    @Inject
    public ApiInterceptor(ParaPesquisaPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void intercept(RequestFacade request) {
        String sessionId = preferences.getSessionId();
        if (sessionId != null) {
            request.addHeader("X-Session-ID", sessionId);
        }
    }
}
