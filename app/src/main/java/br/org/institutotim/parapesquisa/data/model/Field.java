package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_Field.Builder.class)
public abstract class Field implements Parcelable {

    public static final String TABLE = "fields";

    public static final String ID = "_id";
    public static final String LABEL = "label";
    public static final String IDENTIFIER = "identifier";
    public static final String READ_ONLY = "read_only";
    public static final String DESCRIPTION = "description";
    public static final String LAYOUT = "layout";
    public static final String TYPE = "type";
    public static final String ORDER = "field_order";
    public static final String OPTIONS = "options";
    public static final String VALIDATIONS = "validations";
    public static final String ACTIONS = "actions";
    public static final String SECTION_ID = "section_id";

    public static final String[] COLUMNS = {
            ID, LABEL, IDENTIFIER, READ_ONLY, DESCRIPTION, LAYOUT, TYPE, ORDER, OPTIONS,
            VALIDATIONS, ACTIONS, SECTION_ID
    };

    @JsonProperty("id")
    public abstract long getId();

    @JsonProperty("label")
    public abstract String getLabel();

    @JsonProperty("identifier")
    public abstract boolean isIdentifier();

    @JsonProperty("read_only")
    public abstract boolean isReadOnly();

    @Nullable
    @JsonProperty("description")
    public abstract String getDescription();

    @Nullable
    @JsonProperty("layout")
    public abstract FieldLayout getLayout();

    @JsonProperty("type")
    public abstract FieldType getType();

    @Nullable
    @JsonProperty("order")
    public abstract Integer getOrder();

    @Nullable
    @JsonProperty("options")
    public abstract List<FieldOption> getOptions();

    @JsonProperty("validations")
    public abstract FieldValidation getValidations();

    @JsonProperty("actions")
    public abstract List<FieldAction> getActions();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("id")
        public abstract Builder id(long id);

        @JsonProperty("label")
        public abstract Builder label(String label);

        @JsonProperty("identifier")
        public abstract Builder identifier(boolean identifier);

        @JsonProperty("read_only")
        public abstract Builder readOnly(boolean readOnly);

        @JsonProperty("description")
        public abstract Builder description(String description);

        @JsonProperty("layout")
        public abstract Builder layout(FieldLayout layout);

        @JsonProperty("type")
        public abstract Builder type(FieldType type);

        @JsonProperty("order")
        public abstract Builder order(Integer order);

        @JsonProperty("options")
        public abstract Builder options(List<FieldOption> options);

        @JsonProperty("validations")
        public abstract Builder validations(FieldValidation validations);

        @JsonProperty("actions")
        public abstract Builder actions(List<FieldAction> actions);

        public abstract Field build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder id(long id) {
            values.put(ID, id);
            return this;
        }

        public ContentBuilder label(String label) {
            values.put(LABEL, label);
            return this;
        }

        public ContentBuilder identifier(boolean identifier) {
            values.put(IDENTIFIER, identifier);
            return this;
        }

        public ContentBuilder readOnly(boolean readOnly) {
            values.put(READ_ONLY, readOnly);
            return this;
        }

        public ContentBuilder description(String description) {
            values.put(DESCRIPTION, description);
            return this;
        }

        public ContentBuilder layout(FieldLayout layout) {
            if (layout != null) {
                values.put(LAYOUT, layout.toString());
            }
            return this;
        }

        public ContentBuilder type(FieldType type) {
            values.put(TYPE, type.toString());
            return this;
        }

        public ContentBuilder order(Integer order) {
            values.put(ORDER, order);
            return this;
        }

        public ContentBuilder sectionId(long sectionId) {
            values.put(SECTION_ID, sectionId);
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_Field.Builder();
    }
}
