package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_Notification.Builder.class)
public abstract class Notification implements Parcelable {

    public static final String TABLE = "notifications";

    public static final String DATE = "date_notification";
    public static final String MESSAGE = "messagem";
    public static final String ICON = "icon";

    public static final String[] COLUMNS = {
            DATE, MESSAGE, ICON
    };

    public abstract DateTime getDate();

    public abstract String getMessage();

    @Nullable
    public abstract Integer getIcon();

    @AutoParcel.Builder
    public static abstract class Builder {

        public abstract Builder date(DateTime date);

        public abstract Builder message(String message);

        public abstract Builder icon(Integer icon);

        public abstract Notification build();
    }

    public static class ContentBuilder {

        private final ContentValues values = new ContentValues();

        public ContentBuilder date(DateTime date) {
            values.put(DATE, date.toDate().getTime());
            return this;
        }

        public ContentBuilder message(String message) {
            values.put(MESSAGE, message);
            return this;
        }

        public ContentBuilder icon(Integer icon) {
            if (icon != null) {
                values.put(ICON, icon);
            }
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_Notification.Builder();
    }
}
