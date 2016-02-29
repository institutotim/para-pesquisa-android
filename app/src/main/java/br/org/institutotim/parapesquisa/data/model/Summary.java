package br.org.institutotim.parapesquisa.data.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_Summary.Builder.class)
public abstract class Summary implements Parcelable {

    public abstract long getQuota();

    public abstract DateTime getDate();

    public abstract int getApproved();

    @Nullable
    public abstract Integer getRepproved();

    public abstract int getWaitingCorrection();

    public abstract int getWaitingApproval();

    public abstract int getRemaining();

    @AutoParcel.Builder
    public static abstract class Builder {

        public abstract Builder quota(long quota);

        public abstract Builder date(DateTime date);

        public abstract Builder approved(int approved);

        public abstract Builder repproved(Integer repproved);

        public abstract Builder waitingCorrection(int waitingCorrection);

        public abstract Builder waitingApproval(int waitingApproval);

        public abstract Builder remaining(int remaining);

        public abstract Summary build();
    }

    public static Builder builder() {
        return new AutoParcel_Summary.Builder();
    }
}
