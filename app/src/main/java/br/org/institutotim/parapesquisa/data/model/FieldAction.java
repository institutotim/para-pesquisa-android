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
@JsonDeserialize(builder = AutoParcel_FieldAction.Builder.class)
public abstract class FieldAction implements Parcelable {

    public static final String TABLE = "field_actions";

    public static final String WHEN = "when_action";
    public static final String DISABLE = "disable";
    public static final String ENABLE = "enable";
    public static final String DISABLE_SECTIONS = "disable_sections";
    public static final String FIELD_ID = "field_id";

    public static final String[] COLUMNS = {
            WHEN, DISABLE, ENABLE, DISABLE_SECTIONS, FIELD_ID
    };

    @JsonProperty("when")
    public abstract List<String> getWhen();

    @Nullable
    @JsonProperty("disable")
    public abstract List<Long> getDisable();

    @Nullable
    @JsonProperty("enable")
    public abstract List<Long> getEnable();

    @Nullable
    @JsonProperty("disable_sections")
    public abstract List<Long> getDisableSections();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("when")
        public abstract Builder when(List<String> when);

        @JsonProperty("disable")
        public abstract Builder disable(List<Long> disable);

        @JsonProperty("enable")
        public abstract Builder enable(List<Long> enable);

        @JsonProperty("disable_sections")
        public abstract Builder disableSections(List<Long> disableSections);

        public abstract FieldAction build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder when(List<String> when) {
            values.put(WHEN, TextUtils.join("\\\\", when));
            return this;
        }

        public ContentBuilder disable(List<Long> disable) {
            values.put(WHEN, TextUtils.join("\\\\", disable));
            return this;
        }

        public ContentBuilder enable(List<Long> enable) {
            if (enable != null) {
                values.put(WHEN, TextUtils.join("\\\\", enable));
            }
            return this;
        }

        public ContentBuilder disableSections(List<Long> disableSections) {
            if (disableSections != null) {
                values.put(WHEN, TextUtils.join("\\\\", disableSections));
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
        return new AutoParcel_FieldAction.Builder();
    }
}
