package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_StopReasonSubmission.Builder.class)
public abstract class StopReasonSubmission {

    public static final String TABLE = "stop_reasons_submission";

    public static final String REASON = "reason";
    public static final String DATE = "date_rescheduling";
    public static final String SUBMISSION_ID = "submission_id";

    public static final String[] COLUMNS = {
            REASON, DATE, SUBMISSION_ID
    };

    public abstract StopReason getReason();

    public abstract DateTime getDate();

    public abstract long getSubmissionId();

    @AutoParcel.Builder
    public static abstract class Builder {

        public abstract Builder reason(StopReason reason);

        public abstract Builder date(DateTime date);

        public abstract Builder submissionId(long submissionId);

        public abstract StopReasonSubmission build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder reason(StopReason reason) {
            values.put(REASON, reason.getId());
            return this;
        }

        public ContentBuilder date(DateTime date) {
            values.put(DATE, date.toDate().getTime());
            return this;
        }

        public ContentBuilder submissionId(long submissionId) {
            values.put(SUBMISSION_ID, submissionId);
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }
}
