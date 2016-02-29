package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_StopReason.Builder.class)
public abstract class StopReason implements Parcelable {

    public static final String TABLE = "stop_reasons";

    public static final String ID = "_id";
    public static final String REASON = "reason";
    public static final String RESCHEDULE = "reschedule";
    public static final String FORM_ID = "form_id";

    public static final String[] COLUMNS = {
            ID, REASON, RESCHEDULE, FORM_ID
    };

    @JsonProperty("id")
    public abstract long getId();

    @JsonProperty("reason")
    public abstract String getReason();

    @JsonProperty("reschedule")
    public abstract boolean isReschedule();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("id")
        public abstract Builder id(long id);

        @JsonProperty("reason")
        public abstract Builder reason(String reason);

        @JsonProperty("reschedule")
        public abstract Builder reschedule(boolean reschedule);

        public abstract StopReason build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder id(long id) {
            values.put(ID, id);
            return this;
        }

        public ContentBuilder reason(String reason) {
            values.put(REASON, reason);
            return this;
        }

        public ContentBuilder reschedule(boolean reschedule) {
            values.put(RESCHEDULE, reschedule);
            return this;
        }

        public ContentBuilder formId(long formId) {
            values.put(FORM_ID, formId);
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_StopReason.Builder();
    }
}
