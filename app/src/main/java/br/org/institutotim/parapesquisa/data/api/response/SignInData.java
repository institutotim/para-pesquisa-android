package br.org.institutotim.parapesquisa.data.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignInData {

    @JsonProperty("user_id")
    private int userId;
    @JsonProperty("session_id")
    private String sessionId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
