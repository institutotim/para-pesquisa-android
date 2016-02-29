package br.org.institutotim.parapesquisa.data.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

public class RescheduleRequest {

    private DateTime date;
    @JsonProperty("reason_id")
    private long reasonId;

    public RescheduleRequest() {
    }

    public RescheduleRequest(DateTime newDate, long reasonId) {
        date = newDate;
        this.reasonId = reasonId;
    }

    public RescheduleRequest(long reasonId) {
        date = null;
        this.reasonId = reasonId;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public long getReasonId() {
        return reasonId;
    }

    public void setReasonId(long reasonId) {
        this.reasonId = reasonId;
    }

}
