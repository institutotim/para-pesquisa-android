package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_SubmissionLog.Builder.class)
public abstract class SubmissionLog implements Parcelable {

    public static final String TABLE = "submission_logs";

    public static final String ACTION = "action";
    public static final String WHEN = "when_log";
    public static final String USER_ID = "user_id";
    public static final String USER_SUBMISSION_ID = "user_submission_id";

    public static final String[] COLUMNS = {
            ACTION, WHEN, USER_ID
    };

    @Nullable
    @JsonProperty("action")
    public abstract SubmissionLogAction getAction();

    @JsonProperty("when")
    public abstract DateTime getWhen();

    @Nullable
    @JsonProperty("user_id")
    public abstract Long getUserId();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("action")
        public abstract Builder action(SubmissionLogAction action);

        @JsonProperty("when")
        public abstract Builder when(DateTime when);

        @JsonProperty("user_id")
        public abstract Builder userId(Long userId);

        public abstract SubmissionLog build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder action(SubmissionLogAction action) {
            if (action != null) {
                values.put(ACTION, action.toString());
            }
            return this;
        }

        public ContentBuilder when(DateTime when) {
            values.put(WHEN, when.toDate().getTime());
            return this;
        }

        public ContentBuilder userId(Long userId) {
            values.put(USER_ID, userId);
            return this;
        }

        public ContentBuilder userSubmissionId(long userSubmissionId) {
            values.put(USER_SUBMISSION_ID, userSubmissionId);
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_SubmissionLog.Builder();
    }
}
