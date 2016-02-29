package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_AttributionTransfer.Builder.class)
public abstract class AttributionTransfer implements Parcelable {

    public static final String TABLE = "attribution_transfers";

    public static final String SOURCE = "attr_source";
    public static final String TARGET = "attr_target";
    public static final String STATUS = "status";

    public static final String[] COLUMNS = {
            SOURCE, TARGET, STATUS
    };

    @JsonProperty("source_assignment_id")
    public abstract long getSource();

    @JsonProperty("target_assignment_id")
    public abstract long getTarget();

    @JsonProperty("status")
    public abstract SubmissionStatus getStatus();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("source_assignment_id")
        public abstract Builder source(long source);

        @JsonProperty("target_assignment_id")
        public abstract Builder target(long target);

        @JsonProperty("status")
        public abstract Builder status(SubmissionStatus status);

        public abstract AttributionTransfer build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder source(long source) {
            values.put(SOURCE, source);
            return this;
        }

        public ContentBuilder target(long target) {
            values.put(TARGET, target);
            return this;
        }

        public ContentBuilder status(SubmissionStatus status) {
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
