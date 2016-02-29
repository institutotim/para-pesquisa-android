package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_UserData.Builder.class)
public abstract class UserData implements Parcelable {

    public static final String TABLE = "users";

    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String AVATAR_URL = "avatar_url";
    public static final String ROLE = "role";
    public static final String CREATED_AT = "created_at";

    public static final String[] COLUMNS = {
        ID, NAME, USERNAME, EMAIL, AVATAR_URL, ROLE, CREATED_AT
    };

    @JsonProperty("id")
    public abstract long getId();

    @JsonProperty("name")
    public abstract String getName();

    @JsonProperty("username")
    public abstract String getUsername();

    @Nullable
    @JsonProperty("avatar_url")
    public abstract String getAvatarUrl();

    @Nullable
    @JsonProperty("email")
    public abstract String getEmail();

    @JsonProperty("created_at")
    public abstract DateTime getCreatedAt();

    @JsonProperty("role")
    public abstract UserRole getRole();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("id")
        public abstract Builder id(long id);

        @JsonProperty("name")
        public abstract Builder name(String name);

        @JsonProperty("username")
        public abstract Builder username(String username);

        @JsonProperty("avatar_url")
        public abstract Builder avatarUrl(String username);

        @JsonProperty("email")
        public abstract Builder email(String email);

        @JsonProperty("created_at")
        public abstract Builder createdAt(DateTime createdAt);

        @JsonProperty("role")
        public abstract Builder role(UserRole role);

        public abstract UserData build();
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

        public ContentBuilder username(String username) {
            values.put(USERNAME, username);
            return this;
        }

        public ContentBuilder avatarUrl(String avatarUrl) {
            values.put(AVATAR_URL, avatarUrl);
            return this;
        }

        public ContentBuilder email(String email) {
            values.put(EMAIL, email);
            return this;
        }

        public ContentBuilder createdAt(DateTime createdAt) {
            values.put(CREATED_AT, createdAt.toDate().getTime());
            return this;
        }

        public ContentBuilder role(UserRole role) {
            values.put(ROLE, role.toString());
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_UserData.Builder();
    }
}
