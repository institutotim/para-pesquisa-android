package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.List;

import auto.parcel.AutoParcel;
import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.util.DateUtils;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_FormData.Builder.class)
public abstract class FormData implements Parcelable {
    public static final String NULL_DATE = "2099-12-31";

    public static final String TABLE = "forms";

    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String SUBTITLE = "subtitle";
    public static final String PUB_START = "pub_start";
    public static final String PUB_END = "pub_end";
    public static final String MAX_RESCHEDULES = "max_reschedules";
    public static final String ALLOW_TRANSFER = "allow_transfer";
    public static final String ALLOW_NEW_SUBMISSIONS = "allow_new_submissions";
    public static final String UNDEFINED_MODE = "undefined_mode";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String SECTIONS = "sections_raw";

    public static final String[] COLUMNS = {
            ID, NAME, SUBTITLE, PUB_START, PUB_END, MAX_RESCHEDULES, ALLOW_TRANSFER,
            ALLOW_NEW_SUBMISSIONS, UNDEFINED_MODE, CREATED_AT, UPDATED_AT, SECTIONS
    };

    @JsonProperty("id")
    public abstract long getId();

    @JsonProperty("name")
    public abstract String getName();

    @Nullable
    @JsonProperty("subtitle")
    public abstract String getSubtitle();

    @Nullable
    @JsonProperty("pub_start")
    public abstract DateTime getPubStart();

    @Nullable
    @JsonProperty("pub_end")
    public abstract DateTime getPubEnd();

    @JsonProperty("max_reschedules")
    public abstract int getMaxReschedules();

    @JsonProperty("allow_transfer")
    public abstract boolean isAllowTransfer();

    @JsonProperty("allow_new_submissions")
    public abstract boolean isAllowNewSubmissions();

    @JsonProperty("undefined_mode")
    public abstract boolean isUndefinedMode();

    @JsonProperty("created_at")
    public abstract DateTime getCreatedAt();

    @JsonProperty("updated_at")
    public abstract DateTime getUpdatedAt();

    @JsonProperty("stop_reasons")
    public abstract List<StopReason> getStopReasons();

    @JsonProperty("sections")
    public abstract List<Section> getSections();

    public String getSubtitleAndPubDate(Context context) {
        DateTimeFormatter format = DateUtils.getShortDateInstanceWithoutYears();

        DateTime startTime = getPubStart();
        String startDate = startTime == null ? context.getString(R.string.text_indefined_date) : format.print(startTime);

        DateTime endTime = getPubEnd();
        String endDate = endTime == null ? context.getString(R.string.text_indefined_date) : format.print(endTime);

        String subtitle = (getSubtitle() != null ? getSubtitle() + " | " : "");
        if (getPubEnd() != null) {
            subtitle += context.getString(R.string.text_from_dates, startDate,
                    endDate);
        } else {
            subtitle += context.getString(R.string.text_from, startDate);
        }
        return subtitle;
    }

    public boolean hasExtraData() {
        if (getSections() == null) return false;

        for (int i = 0; i < getSections().size(); i++) {
            Section section = getSections().get(i);
            if (section.getFields() != null) {
                for (int j = 0; j < section.getFields().size(); j++) {
                    if (section.getFields().get(j).isReadOnly()) return true;
                }
            }
        }
        return false;
    }

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("id")
        public abstract Builder id(long id);

        @JsonProperty("name")
        public abstract Builder name(String name);

        @JsonProperty("subtitle")
        public abstract Builder subtitle(String subtitle);

        @JsonProperty("pub_start")
        public abstract Builder pubStart(DateTime pubStart);

        @JsonProperty("pub_end")
        public abstract Builder pubEnd(DateTime pubEnd);

        @JsonProperty("max_reschedules")
        public abstract Builder maxReschedules(int maxReschedules);

        @JsonProperty("allow_transfer")
        public abstract Builder allowTransfer(boolean allowTransfer);

        @JsonProperty("allow_new_submissions")
        public abstract Builder allowNewSubmissions(boolean allowNewSubmissions);

        @JsonProperty("undefined_mode")
        public abstract Builder undefinedMode(boolean undefinedMode);

        @JsonProperty("created_at")
        public abstract Builder createdAt(DateTime createdAt);

        @JsonProperty("updated_at")
        public abstract Builder updatedAt(DateTime updatedAt);

        @JsonProperty("stop_reasons")
        public abstract Builder stopReasons(List<StopReason> stopReasons);

        @JsonProperty("sections")
        public abstract Builder sections(List<Section> sections);

        public abstract FormData build();
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

        public ContentBuilder subtitle(String subtitle) {
            values.put(SUBTITLE, subtitle);
            return this;
        }

        public ContentBuilder pubStart(DateTime pubStart) {
            if (pubStart == null) {
                values.putNull(PUB_START);
            } else{
                values.put(PUB_START, pubStart.toDate().getTime());
            }
            return this;
        }

        public ContentBuilder pubEnd(DateTime pubEnd) {
            if (pubEnd == null) {
                values.putNull(PUB_END);
            } else {
                values.put(PUB_END, pubEnd.toDate().getTime());
            }
            return this;
        }

        public ContentBuilder maxReschedules(int maxReschedules) {
            values.put(MAX_RESCHEDULES, maxReschedules);
            return this;
        }

        public ContentBuilder allowTransfer(boolean allowTransfer) {
            values.put(ALLOW_TRANSFER, allowTransfer);
            return this;
        }

        public ContentBuilder allowNewSubmissions(boolean allowNewSubmissions) {
            values.put(ALLOW_NEW_SUBMISSIONS, allowNewSubmissions);
            return this;
        }

        public ContentBuilder undefinedMode(boolean undefinedMode) {
            values.put(UNDEFINED_MODE, undefinedMode);
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
        return new AutoParcel_FormData.Builder();
    }
}
