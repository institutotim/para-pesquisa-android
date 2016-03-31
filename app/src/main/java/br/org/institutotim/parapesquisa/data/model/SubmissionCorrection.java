package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_SubmissionCorrection.Builder.class)
public abstract class SubmissionCorrection implements Parcelable {

    public static final String TABLE = "submission_corrections";

    public static final String FIELD_ID = "field_id";
    public static final String MESSAGE = "message";
    public static final String USER_ID = "user_id";
    public static final String CREATED_AT = "created_at";
    public static final String USER_SUBMISSION_ID = "user_submission_id";

    public static final String[] COLUMNS = {
            FIELD_ID, MESSAGE, USER_ID, CREATED_AT
    };

    @JsonProperty("field_id")
    public abstract long getFieldId();

    @JsonProperty("message")
    public abstract String getMessage();

    @JsonProperty("user_id")
    public abstract long getUserId();

    @Nullable
    @JsonProperty("created_at")
    public abstract DateTime getCreatedAt();

    @Override
    @JsonIgnore
    public boolean equals(Object o) {
        if (!(o instanceof SubmissionCorrection))
            return false;

        SubmissionCorrection correction = (SubmissionCorrection) o;
        if (correction.getFieldId() == getFieldId() && correction.getUserId() == getUserId()) {
            if (TextUtils.equals(correction.getMessage(), getMessage())) {
                return true;
            }
        }
        return false;
    }


    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("field_id")
        public abstract Builder fieldId(long fieldId);

        @JsonProperty("message")
        public abstract Builder message(String message);

        @JsonProperty("user_id")
        public abstract Builder userId(long userId);

        @JsonProperty("created_at")
        public abstract Builder createdAt(DateTime createdAt);

        public abstract SubmissionCorrection build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder fieldId(long fieldId) {
            values.put(FIELD_ID, fieldId);
            return this;
        }

        public ContentBuilder message(String message) {
            values.put(MESSAGE, message);
            return this;
        }

        public ContentBuilder userId(long userId) {
            values.put(USER_ID, userId);
            return this;
        }

        public ContentBuilder createdAt(DateTime createdAt) {
            if (createdAt != null) {
                values.put(CREATED_AT, createdAt.toDate().getTime());
            }
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
        return new AutoParcel_SubmissionCorrection.Builder();
    }
}
