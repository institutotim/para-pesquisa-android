package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
@JsonDeserialize(builder = AutoParcel_UserSubmission.Builder.class)
public abstract class UserSubmission implements Parcelable {

    public static final String TABLE = "user_submissions";

    public static final String ID = "_id";
    public static final String FORM_ID = "form_id";
    public static final String LAST_RESCHEDULE_DATE = "last_reschedule_date";
    public static final String STATUS = "status";
    public static final String OWNER = "owner";
    public static final String USER_SUBMISSION_ID = "user_submission_id";
    public static final String ANSWERS = "answers_raw";

    public static final String[] COLUMNS = {
            ID, FORM_ID, LAST_RESCHEDULE_DATE, STATUS, OWNER, USER_SUBMISSION_ID, ANSWERS
    };

    @Nullable
    @JsonProperty("id")
    public abstract Long getId();

    @Nullable
    @JsonProperty("form_id")
    public abstract Long getFormId();

    @Nullable
    @JsonProperty("last_reschedule_date")
    public abstract DateTime getLastRescheduleDate();

    @Nullable
    @JsonProperty("status")
    public abstract SubmissionStatus getStatus();

    @JsonProperty("log")
    public abstract List<SubmissionLog> getLog();

    @Nullable
    @JsonProperty("corrections")
    public abstract List<SubmissionCorrection> getCorrections();

    @JsonProperty("answers")
    public abstract List<Answer> getAnswers();

    @Nullable
    @JsonProperty("alternatives")
    public abstract List<UserSubmission> getAlternatives();

    @Nullable
    @JsonProperty("owner")
    public abstract UserData getOwner();

    @Nullable
    @JsonIgnore
    public abstract Boolean getInProgress();

    @Nullable
    @JsonIgnore
    public abstract Integer getCurrentPage();

    @Nullable
    @JsonIgnore
    public abstract String getIdentifier();

    @Nullable
    public Answer getAnswerForField(long fieldId) {
        if (getAnswers() == null) return null;

        for (int i = 0; i < getAnswers().size(); i++) {
            Answer answer = getAnswers().get(i);
            if (answer != null && answer.getFieldId() == fieldId) return answer;
        }

        return null;
    }

    public UserSubmission removeStatus() {
        return new AutoParcel_UserSubmission.Builder()
                .currentPage(getCurrentPage())
                .id(getId())
                .formId(getFormId())
                .lastRescheduleDate(getLastRescheduleDate())
                .status(null)
                .log(getLog())
                .corrections(getCorrections())
                .answers(getAnswers())
                .alternatives(getAlternatives())
                .owner(getOwner())
                .inProgress(getInProgress())
                .currentPage(getCurrentPage())
                .identifier(getIdentifier())
                .build();
    }

    public UserSubmission addLog(DateTime date, SubmissionLogAction action, long user) {
        List<SubmissionLog> logs = new ArrayList<>(getLog());
        logs.add(SubmissionLog.builder()
                .when(date)
                .action(action)
                .userId(user)
                .build());

        return new AutoParcel_UserSubmission.Builder()
                .currentPage(getCurrentPage())
                .id(getId())
                .formId(getFormId())
                .lastRescheduleDate(getLastRescheduleDate())
                .status(getStatus())
                .log(logs)
                .corrections(getCorrections())
                .answers(getAnswers())
                .alternatives(getAlternatives())
                .owner(getOwner())
                .inProgress(getInProgress())
                .currentPage(getCurrentPage())
                .identifier(getIdentifier())
                .build();
    }

    public UserSubmission updateStatus(SubmissionStatus status) {
        return new AutoParcel_UserSubmission.Builder()
                .currentPage(getCurrentPage())
                .id(getId())
                .formId(getFormId())
                .lastRescheduleDate(getLastRescheduleDate())
                .status(status)
                .log(getLog())
                .corrections(getCorrections())
                .answers(getAnswers())
                .alternatives(getAlternatives())
                .owner(getOwner())
                .inProgress(getInProgress())
                .currentPage(getCurrentPage())
                .identifier(getIdentifier())
                .build();
    }

    public DateTime getStartTime() {
        if (getLog() == null || getLog().isEmpty()) return null;

        for (int i = 0; i < getLog().size(); i++) {
            if (SubmissionLogAction.CREATED.equals(getLog().get(i).getAction()))
                return getLog().get(i).getWhen();
        }

        return DateTime.now();
    }

    public UserSubmission newInstanceWithId(long submissionId) {
        return UserSubmission.builder()
                .id(submissionId)
                .formId(getFormId())
                .lastRescheduleDate(getLastRescheduleDate())
                .status(getStatus())
                .log(getLog())
                .corrections(getCorrections())
                .answers(getAnswers())
                .alternatives(getAlternatives())
                .owner(getOwner())
                .inProgress(getInProgress())
                .currentPage(getCurrentPage())
                .identifier(getIdentifier())
                .build();
    }

    public UserSubmission setCorrections(List<SubmissionCorrection> corrections) {
        return new AutoParcel_UserSubmission.Builder()
                .currentPage(getCurrentPage())
                .id(getId())
                .formId(getFormId())
                .lastRescheduleDate(getLastRescheduleDate())
                .status(SubmissionStatus.WAITING_CORRECTION)
                .log(getLog())
                .corrections(corrections)
                .answers(getAnswers())
                .alternatives(getAlternatives())
                .owner(getOwner())
                .inProgress(getInProgress())
                .currentPage(getCurrentPage())
                .identifier(getIdentifier())
                .build();
    }

    public UserSubmission setIdentifier(@Nullable String identifier) {
        if (identifier == null) return this;

        return new AutoParcel_UserSubmission.Builder()
                .currentPage(getCurrentPage())
                .id(getId())
                .formId(getFormId())
                .lastRescheduleDate(getLastRescheduleDate())
                .status(getStatus())
                .log(getLog())
                .corrections(getCorrections())
                .answers(getAnswers())
                .alternatives(getAlternatives())
                .owner(getOwner())
                .inProgress(getInProgress())
                .currentPage(getCurrentPage())
                .identifier(identifier)
                .build();
    }

    public UserSubmission setIdentifier(Field field) {
        Answer answer = getAnswerForField(field.getId());
        if (answer != null) return setIdentifier(answer.getValues());
        return this;
    }

    @AutoParcel.Builder
    public static abstract class Builder {

        public abstract UserSubmission build();

        @JsonProperty("id")
        public abstract Builder id(Long id);

        @JsonProperty("form_id")
        public abstract Builder formId(Long id);

        @JsonProperty("last_reschedule_date")
        public abstract Builder lastRescheduleDate(DateTime lastRescheduleDate);

        @JsonProperty("status")
        public abstract Builder status(SubmissionStatus status);

        @JsonProperty("log")
        public abstract Builder log(List<SubmissionLog> log);

        @JsonProperty("corrections")
        public abstract Builder corrections(List<SubmissionCorrection> corrections);

        @JsonProperty("answers")
        public abstract Builder answers(List<Answer> answers);

        @JsonProperty("alternatives")
        public abstract Builder alternatives(List<UserSubmission> alternatives);

        @JsonProperty("owner")
        public abstract Builder owner(UserData owner);

        @JsonIgnore
        public abstract Builder inProgress(Boolean inProgress);

        @JsonIgnore
        public abstract Builder currentPage(Integer currentPage);

        @JsonIgnore
        public abstract Builder identifier(String identifier);
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

        public ContentBuilder lastRescheduleDate(DateTime lastRescheduleDate) {
            if (lastRescheduleDate != null) {
                values.put(LAST_RESCHEDULE_DATE, lastRescheduleDate.toDate().getTime());
            }
            return this;
        }

        public ContentBuilder status(SubmissionStatus status) {
            if (status != null) {
                values.put(STATUS, status.toString());
            }
            return this;
        }

        public ContentBuilder owner(UserData owner) {
            if (owner != null) {
                values.put(OWNER, owner.getId());
            }
            return this;
        }

        public ContentBuilder userSubmissionId(Long userSubmissionId) {
            values.put(USER_SUBMISSION_ID, userSubmissionId);
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_UserSubmission.Builder();
    }

    @Nullable
    public SubmissionLog getLatestLog() {
        if (getLog() == null || getLog().isEmpty()) return null;

        return getLog().get(getLog().size() - 1);
    }

    @Nullable
    public SubmissionLog getLatestLogByAction(@NonNull SubmissionLogAction action) {
        if (getLog() == null || getLog().isEmpty()) return null;

        for (int i = getLog().size() - 1; i >= 0; i--) {
            final SubmissionLog submissionLog = getLog().get(i);
            if (submissionLog.getAction() != null && submissionLog.getAction().equals(action)) {
                return submissionLog;
            }
        }

        return null;
    }
}
