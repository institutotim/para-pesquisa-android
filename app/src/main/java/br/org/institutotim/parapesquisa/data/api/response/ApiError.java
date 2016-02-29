package br.org.institutotim.parapesquisa.data.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ApiError {

    private String error;
    @JsonProperty("error_description")
    private String errorDescription;
    private Map<String, List<String>> messages;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public Map<String, List<String>> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, List<String>> messages) {
        this.messages = messages;
    }
}