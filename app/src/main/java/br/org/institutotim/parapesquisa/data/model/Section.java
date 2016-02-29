package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_Section.Builder.class)
public abstract class Section implements Parcelable {

    public static final String TABLE = "sections";

    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String ORDER = "section_order";
    public static final String FORM_ID = "form_id";

    public static final String[] COLUMNS = {
            ID, NAME, ORDER, FORM_ID
    };

    @JsonProperty("id")
    public abstract long getId();

    @JsonProperty("name")
    public abstract String getName();

    @Nullable
    @JsonProperty("order")
    public abstract Integer getOrder();

    @JsonProperty("fields")
    public abstract List<Field> getFields();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("id")
        public abstract Builder id(long id);

        @JsonProperty("name")
        public abstract Builder name(String name);

        @JsonProperty("order")
        public abstract Builder order(Integer order);

        @JsonProperty("fields")
        public abstract Builder fields(List<Field> fields);

        public abstract Section build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder id(long id) {
            values.put(ID, id);
            return this;
        }

        public ContentBuilder name(String name) {
            values.put(NAME, name);
            return this;
        }

        public ContentBuilder order(Integer order) {
            values.put(ORDER, order);
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
        return new AutoParcel_Section.Builder();
    }
}
