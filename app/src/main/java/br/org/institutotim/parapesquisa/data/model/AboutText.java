package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_AboutText.Builder.class)
public abstract class AboutText implements Parcelable {

    public static final String TABLE = "about_texts";

    public static final String ID = "_id";
    public static final String TITLE = "title";
    public static final String SUBTITLE = "subtitle";
    public static final String CONTENT = "content";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";

    public static final String[] COLUMNS = {
            ID, TITLE, SUBTITLE, CONTENT, CREATED_AT, UPDATED_AT
    };

    @JsonProperty("id")
    public abstract long getId();

    @JsonProperty("title")
    public abstract String getTitle();

    @JsonProperty("subtitle")
    public abstract String getSubtitle();

    @JsonProperty("content")
    public abstract String getContent();

    @JsonProperty("created_at")
    public abstract DateTime getCreatedAt();

    @JsonProperty("updated_at")
    public abstract DateTime getUpdatedAt();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("id")
        public abstract Builder id(long id);

        @JsonProperty("title")
        public abstract Builder title(String title);

        @JsonProperty("subtitle")
        public abstract Builder subtitle(String subtitle);

        @JsonProperty("content")
        public abstract Builder content(String content);

        @JsonProperty("created_at")
        public abstract Builder createdAt(DateTime createdAt);

        @JsonProperty("updated_at")
        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract AboutText build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder id(long id) {
            values.put(ID, id);
            return this;
        }

        public ContentBuilder title(String title) {
            values.put(TITLE, title);
            return this;
        }

        public ContentBuilder subtitle(String subtitle) {
            values.put(SUBTITLE, subtitle);
            return this;
        }

        public ContentBuilder content(String content) {
            values.put(CONTENT, content);
            return this;
        }

        public ContentBuilder createdAt(DateTime createdAt) {
            values.put(CREATED_AT, createdAt.toDate().getTime());
            return this;
        }

        public ContentBuilder updatedAt(DateTime updatedAt) {
            values.put(UPDATED_AT, updatedAt.toDate().getTime());
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_AboutText.Builder();
    }
}
