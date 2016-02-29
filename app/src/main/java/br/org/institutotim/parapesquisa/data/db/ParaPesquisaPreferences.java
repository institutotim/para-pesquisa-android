package br.org.institutotim.parapesquisa.data.db;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import br.org.institutotim.parapesquisa.data.model.UserData;
import timber.log.Timber;

public class ParaPesquisaPreferences {

    private static final String SERVER_URL = "br.org.institutotim.parapesquisa.server_url";
    private static final String SESSION_ID = "br.org.institutotim.parapesquisa.session_id";
    private static final String USER_DATA = "br.org.institutotim.parapesquisa.user_data";

    private final SharedPreferences preferences;
    private final ObjectMapper objectMapper;

    public ParaPesquisaPreferences(SharedPreferences preferences, ObjectMapper objectMapper) {
        this.preferences = preferences;
        this.objectMapper = objectMapper;
    }

    public
    @Nullable
    UserData getUser() {
        try {
            String rawUser = preferences.getString(USER_DATA, null);
            if (rawUser != null) {
                return objectMapper.readValue(rawUser, UserData.class);
            }
        } catch (IOException e) {
            Timber.e(e, "Failed to save user");

        }

        return null;
    }

    public void setServerUrl(CharSequence url) {
        preferences.edit().putString(SERVER_URL, url.toString()).apply();
    }

    public String getServerUrl() {
        return preferences.getString(SERVER_URL, null);
    }

    public void setSessionId(String sessionId) {
        preferences.edit().putString(SESSION_ID, sessionId).apply();
    }

    public String getSessionId() {
        return preferences.getString(SESSION_ID, null);
    }

    public void setUser(UserData user) {
        try {
            preferences.edit().putString(USER_DATA, objectMapper.writeValueAsString(user)).apply();
        } catch (JsonProcessingException e) {
            Timber.e(e, "Failed to save user");
        }
    }

    public void clearSession() {
        preferences.edit().remove(SESSION_ID).remove(USER_DATA).apply();
    }
}
