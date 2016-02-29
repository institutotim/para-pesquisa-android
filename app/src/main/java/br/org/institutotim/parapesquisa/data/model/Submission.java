package br.org.institutotim.parapesquisa.data.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.joda.time.DateTime;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_Submission.Builder.class)
public abstract class Submission implements Parcelable {

    @Nullable
    @JsonProperty("submission_id")
    public abstract Long getId();

    @Nullable
    @JsonProperty("started_at")
    public abstract DateTime getStartedAt();

    @Nullable
    @JsonProperty("answers")
    public abstract List<Answer> getAnswers();

    @AutoParcel.Builder
    public static abstract class Builder {

        @JsonProperty("submission_id")
        public abstract Builder id(Long id);

        @JsonProperty("started_at")
        public abstract Builder startedAt(DateTime startedAt);

        @JsonProperty("answers")
        public abstract Builder answers(List<Answer> answers);

        public abstract Submission build();
    }

    public static Builder builder() {
        return new AutoParcel_Submission.Builder();
    }
}
