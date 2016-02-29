package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_Attribution.Builder.class)
public abstract class Attribution {

    public static final String TABLE = "attributions";

    public static final String ID = "_id";
    public static final String USER = "user";
    public static final String QUOTA = "quota";
    public static final String FORM_ID = "form_id";

    public static final String[] COLUMNS = {
            ID, USER, QUOTA, FORM_ID
    };

    @JsonProperty("id")
    public abstract long getId();

    @JsonProperty("user")
    public abstract UserData getUser();

    @JsonProperty("quota")
    public abstract int getQuota();

    @JsonProperty("form_id")
    public abstract long getFormId();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("id")
        public abstract Builder id(long id);

        @JsonProperty("user")
        public abstract Builder user(UserData user);

        @JsonProperty("quota")
        public abstract Builder quota(int quota);

        @JsonProperty("form_id")
        public abstract Builder formId(long formId);

        public abstract Attribution build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder id(long id) {
            values.put(ID, id);
            return this;
        }

        public ContentBuilder user(UserData user) {
            values.put(USER, user.getId());
            return this;
        }

        public ContentBuilder quota(int quota) {
            values.put(QUOTA, quota);
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
        return new AutoParcel_Attribution.Builder();
    }
}
