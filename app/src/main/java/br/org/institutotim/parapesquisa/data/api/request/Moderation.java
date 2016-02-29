package br.org.institutotim.parapesquisa.data.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

import br.org.institutotim.parapesquisa.data.model.ModerationAction;

public class Moderation {

    private DateTime date;
    @JsonProperty("submission_action")
    private ModerationAction action;

    public Moderation() {
        this.date = DateTime.now();
    }

    public Moderation(ModerationAction action) {
        this.action = action;
        this.date = DateTime.now();
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public ModerationAction getAction() {
        return action;
    }

    public void setAction(ModerationAction action) {
        this.action = action;
    }
}
