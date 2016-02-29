package br.org.institutotim.parapesquisa.data.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SubmissionLogAction {

    CREATED("created"),
    STARTED("started"),
    REVISED("revised"),
    APPROVED("approved"),
    CANCELLED("canceled"),
    RESCHEDULED("rescheduled"),
    TRANSFERRED("transferred"),
    RESET("reset"),
    REPROVED("reproved");

    private String value;

    SubmissionLogAction(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    public static SubmissionLogAction get(String value) {
        for (SubmissionLogAction action : values()) {
            if (action.value.equals(value)) return action;
        }

        return null;
    }
}
