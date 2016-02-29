package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_FieldOption.Builder.class)
public abstract class FieldOption implements Parcelable {

    public static final String TABLE = "field_options";

    public static final String ID = "id";
    public static final String LABEL = "label";
    public static final String VALUE = "value";
    public static final String FIELD_ID = "field_id";

    public static final String[] COLUMNS = {
            ID, LABEL, VALUE
    };

    @Override
    public String toString() {
        return getValue();
    }

    @Nullable
    @JsonProperty("id")
    public abstract Long getId();

    @JsonProperty("label")
    public abstract String getLabel();

    @JsonProperty("value")
    public abstract String getValue();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("id")
        public abstract Builder id(Long id);

        @JsonProperty("label")
        public abstract Builder label(String label);

        @JsonProperty("value")
        public abstract Builder value(String value);

        public abstract FieldOption build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder id(Long id) {
            values.put(ID, id);
            return this;
        }

        public ContentBuilder label(String label) {
            values.put(LABEL, label);
            return this;
        }

        public ContentBuilder value(String value) {
            values.put(VALUE, value);
            return this;
        }

        public ContentBuilder fieldId(long fieldId) {
            values.put(FIELD_ID, fieldId);
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_FieldOption.Builder();
    }
}
