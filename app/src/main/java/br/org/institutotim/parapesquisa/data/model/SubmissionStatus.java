package br.org.institutotim.parapesquisa.data.model;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonValue;

import br.org.institutotim.parapesquisa.R;

public enum SubmissionStatus {

    NEW("new"),
    WAITING_APPROVAL("waiting_approval"),
    WAITING_CORRECTION("waiting_correction"),
    APPROVED("approved"),
    CANCELLED("canceled"),
    RESCHEDULED("rescheduled"),
    WAITING_SYNC("waiting_sync"),
    IN_PROGRESS("in_progress");

    private String value;

    SubmissionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    public String toString(Context context) {
        switch (this) {
            case WAITING_APPROVAL:
                return context.getString(R.string.text_pending_approve);
            case WAITING_CORRECTION:
                return context.getString(R.string.text_pending_correction);
            case APPROVED:
                return context.getString(R.string.text_approved);
            case CANCELLED:
                return context.getString(R.string.text_cancelled);
            case RESCHEDULED:
                return context.getString(R.string.text_rescheduled);
            case IN_PROGRESS:
                return context.getString(R.string.text_in_progress);
            case WAITING_SYNC:
                return context.getString(R.string.tab_waiting_sync);
            default:
                return "";
        }
    }

    public static SubmissionStatus get(String value) {
        for (SubmissionStatus status : values()) {
            if (status.value.equals(value)) return status;
        }
        return null;
    }
}