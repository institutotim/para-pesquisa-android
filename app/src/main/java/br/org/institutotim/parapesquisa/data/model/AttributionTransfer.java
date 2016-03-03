package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_AttributionTransfer.Builder.class)
public abstract class AttributionTransfer implements Parcelable {

    public static final String TABLE = "attribution_transfers";

    public static final String SOURCE = "user_id_from";
    public static final String TARGET = "user_id_to";
    public static final String FORM_ID = "form_id";
    public static final String STATUS = "status";

    public static final String[] COLUMNS = {
            FORM_ID, SOURCE, TARGET, STATUS
    };

    @JsonProperty("user_id_from")
    public abstract long getSource();

    @JsonProperty("user_id_to")
    public abstract long getTarget();

    @JsonProperty("status")
    public abstract SubmissionStatus getStatus();

    @Nullable
    @JsonProperty("form_id")
    public abstract Long getFormId();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("user_id_from")
        public abstract Builder source(long source);

        @JsonProperty("user_id_to")
        public abstract Builder target(long target);

        @JsonProperty("status")
        public abstract Builder status(SubmissionStatus status);

        @JsonProperty("form_id")
        public abstract Builder formId(Long id);

        public abstract AttributionTransfer build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder source(long source) {
            values.put(SOURCE, source);
            return this;
        }

        public ContentBuilder formId(long formId) {
            values.put(FORM_ID, formId);
            return this;
        }

        public ContentBuilder target(long target) {
            values.put(TARGET, target);
            return this;
        }

        public ContentBuilder status(SubmissionStatus status) {
            if (status == null) {
                return this;
            }
            values.put(STATUS, status.toString());
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_AttributionTransfer.Builder();
    }
}
