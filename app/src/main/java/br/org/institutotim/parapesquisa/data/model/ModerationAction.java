package br.org.institutotim.parapesquisa.data.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ModerationAction {

    APPROVED("approve"),
    REJECTED("reprove");

    private String value;

    ModerationAction(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() { return value; }
}
