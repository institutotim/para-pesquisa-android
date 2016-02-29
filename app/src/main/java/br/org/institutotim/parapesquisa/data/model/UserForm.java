package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_UserForm.Builder.class)
public abstract class UserForm implements Parcelable {

    public static final String TABLE = "user_forms";

    public static final String ID = "_id";
    public static final String FORM_ID = "form_id";
    public static final String QUOTA = "quota";
    public static final String USER = "user";
    public static final String MODERATOR = "moderator";

    public static final String[] COLUMNS = {
            ID, FORM_ID, QUOTA, USER, MODERATOR
    };

    @JsonProperty("id")
    public abstract long getId();

    @JsonProperty("form_id")
    public abstract long getFormId();

    @JsonProperty("quota")
    public abstract long getQuota();

    @JsonProperty("user")
    public abstract UserData getUser();

    @Nullable
    @JsonProperty("moderator")
    public abstract UserData getModerator();

    @JsonProperty("form")
    public abstract FormData getForm();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("id")
        public abstract Builder id(long id);

        @JsonProperty("form_id")
        public abstract Builder formId(long formId);

        @JsonProperty("quota")
        public abstract Builder quota(long quota);

        @JsonProperty("user")
        public abstract Builder user(UserData user);

        @JsonProperty("moderator")
        public abstract Builder moderator(UserData moderator);

        @JsonProperty("form")
        public abstract Builder form(FormData form);

        public abstract UserForm build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder id(long id) {
            values.put(ID, id);
            return this;
        }

        public ContentBuilder formId(long formId) {
            values.put(FORM_ID, formId);
            return this;
        }

        public ContentBuilder quota(long quota) {
            values.put(QUOTA, quota);
            return this;
        }

        public ContentBuilder user(UserData user) {
            values.put(USER, user.getId());
            return this;
        }

        public ContentBuilder moderator(UserData moderator) {
            if (moderator != null)
                values.put(MODERATOR, moderator.getId());
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_UserForm.Builder();
    }
}
