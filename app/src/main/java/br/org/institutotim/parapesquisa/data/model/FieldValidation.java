package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_FieldValidation.Builder.class)
public abstract class FieldValidation implements Parcelable {

    public static final String TABLE = "field_validations";

    public static final String REQUIRED = "required";
    public static final String RANGE = "range";
    public static final String FIELD_ID = "field_id";

    public static final String[] COLUMNS = {
            REQUIRED, RANGE
    };

    @Nullable
    @JsonProperty("required")
    public abstract Boolean getRequired();

    @Nullable
    @JsonProperty("range")
    public abstract List<Long> getRange();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("required")
        public abstract Builder required(Boolean required);

        @JsonProperty("range")
        public abstract Builder range(List<Long> range);

        public abstract FieldValidation build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder required(Boolean required) {
            values.put(REQUIRED, required);
            return this;
        }

        public ContentBuilder range(List<Long> range) {
            if (range != null) {
                values.put(RANGE, TextUtils.join("\\\\", range));
            }
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
        return new AutoParcel_FieldValidation.Builder();
    }
}
