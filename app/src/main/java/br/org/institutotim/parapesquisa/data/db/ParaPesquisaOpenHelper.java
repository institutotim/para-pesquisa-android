package br.org.institutotim.parapesquisa.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.org.institutotim.parapesquisa.data.model.AboutText;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Attribution;
import br.org.institutotim.parapesquisa.data.model.AttributionTransfer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Notification;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.StopReason;
import br.org.institutotim.parapesquisa.data.model.StopReasonSubmission;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.SubmissionLog;
import br.org.institutotim.parapesquisa.data.model.SubmissionLogAction;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserForm;
import br.org.institutotim.parapesquisa.data.model.UserRole;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import timber.log.Timber;

import static br.org.institutotim.parapesquisa.util.DbUtils.getBoolean;
import static br.org.institutotim.parapesquisa.util.DbUtils.getDateTime;
import static br.org.institutotim.parapesquisa.util.DbUtils.getInt;
import static br.org.institutotim.parapesquisa.util.DbUtils.getLong;
import static br.org.institutotim.parapesquisa.util.DbUtils.getString;

public class ParaPesquisaOpenHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String INTEGER_PRIMARY_KEY = " INTEGER NOT NULL PRIMARY KEY,";
    private static final String TEXT = " TEXT,";
    private static final String LONG = " LONG,";
    private static final String INTEGER = " INTEGER,";
    private static final String LONG_END = " LONG";

    private static final String DB_NAME = "parapesquisa.db";
    private static final int DB_VERSION = 2;

    private static final String CREATE_TEMP = ""
            + CREATE_TABLE + "TEMPORARY" + "("
            + "_id INTEGER NOT NULL PRIMARY KEY)";

    private static final String CREATE_USER = ""
            + CREATE_TABLE + UserData.TABLE + "("
            + UserData.ID + INTEGER_PRIMARY_KEY
            + UserData.NAME + TEXT
            + UserData.USERNAME + TEXT
            + UserData.AVATAR_URL + TEXT
            + UserData.EMAIL + TEXT
            + UserData.CREATED_AT + LONG
            + UserData.ROLE + " TEXT NOT NULL"
            + ")";
    private static final String CREATE_USER_FORM = ""
            + CREATE_TABLE + UserForm.TABLE + "("
            + UserForm.ID + INTEGER_PRIMARY_KEY
            + UserForm.FORM_ID + INTEGER
            + UserForm.QUOTA + INTEGER
            + UserForm.USER + INTEGER
            + UserForm.MODERATOR + " INTEGER"
            + ")";
    private static final String CREATE_FORM = ""
            + CREATE_TABLE + FormData.TABLE + "("
            + FormData.ID + INTEGER_PRIMARY_KEY
            + FormData.NAME + TEXT
            + FormData.SUBTITLE + TEXT
            + FormData.PUB_START + LONG
            + FormData.PUB_END + LONG
            + FormData.MAX_RESCHEDULES + INTEGER
            + FormData.ALLOW_TRANSFER + INTEGER
            + FormData.ALLOW_NEW_SUBMISSIONS + INTEGER
            + FormData.UNDEFINED_MODE + INTEGER
            + FormData.CREATED_AT + LONG
            + FormData.SECTIONS + TEXT
            + FormData.UPDATED_AT + LONG_END
            + ")";
    private static final String CREATE_STOP_REASON = ""
            + CREATE_TABLE + StopReason.TABLE + "("
            + StopReason.ID + INTEGER_PRIMARY_KEY
            + StopReason.REASON + TEXT
            + StopReason.RESCHEDULE + INTEGER
            + StopReason.FORM_ID + LONG_END
            + ")";
    private static final String CREATE_ATTRIBUTION = ""
            + CREATE_TABLE + Attribution.TABLE + "("
            + Attribution.ID + INTEGER_PRIMARY_KEY
            + Attribution.USER + LONG
            + Attribution.QUOTA + INTEGER
            + Attribution.FORM_ID + LONG_END
            + ")";
    private static final String CREATE_USER_SUBMISSION = ""
            + CREATE_TABLE + UserSubmission.TABLE + "("
            + UserSubmission.ID + INTEGER_PRIMARY_KEY
            + UserSubmission.FORM_ID + LONG
            + UserSubmission.LAST_RESCHEDULE_DATE + LONG
            + UserSubmission.STATUS + TEXT
            + UserSubmission.OWNER + LONG
            + UserSubmission.ANSWERS + TEXT
            + UserSubmission.USER_SUBMISSION_ID + LONG_END
            + ")";
    private static final String CREATE_USER_SUBMISSION_IN_PROGRESS = ""
            + CREATE_TABLE + UserSubmission.TABLE + "_in_progress" + "("
            + UserSubmission.ID + LONG
            + UserSubmission.FORM_ID + LONG
            + UserSubmission.LAST_RESCHEDULE_DATE + LONG
            + UserSubmission.STATUS + TEXT
            + UserSubmission.OWNER + LONG
            + UserSubmission.ANSWERS + TEXT
            + UserSubmission.USER_SUBMISSION_ID + LONG_END
            + ")";
    private static final String CREATE_USER_SUBMISSION_PENDING = ""
            + CREATE_TABLE + UserSubmission.TABLE + "_pending" + "("
            + UserSubmission.ID + LONG
            + UserSubmission.FORM_ID + LONG
            + UserSubmission.LAST_RESCHEDULE_DATE + LONG
            + UserSubmission.STATUS + TEXT
            + UserSubmission.OWNER + LONG
            + UserSubmission.ANSWERS + TEXT
            + UserSubmission.USER_SUBMISSION_ID + LONG_END
            + ")";
    private static final String CREATE_SUBMISSION_LOG = ""
            + CREATE_TABLE + SubmissionLog.TABLE + "("
            + SubmissionLog.ACTION + TEXT
            + SubmissionLog.WHEN + LONG
            + SubmissionLog.USER_ID + LONG
            + SubmissionLog.USER_SUBMISSION_ID + LONG_END
            + ")";
    private static final String CREATE_SUBMISSION_CORRECTION = ""
            + CREATE_TABLE + SubmissionCorrection.TABLE + "("
            + SubmissionCorrection.FIELD_ID + LONG
            + SubmissionCorrection.MESSAGE + TEXT
            + SubmissionCorrection.USER_ID + LONG
            + SubmissionCorrection.CREATED_AT + LONG
            + SubmissionCorrection.USER_SUBMISSION_ID + LONG_END
            + ")";
    private static final String CREATE_ABOUT_TEXT = ""
            + CREATE_TABLE + AboutText.TABLE + "("
            + AboutText.ID + INTEGER_PRIMARY_KEY
            + AboutText.TITLE + TEXT
            + AboutText.SUBTITLE + TEXT
            + AboutText.CONTENT + TEXT
            + AboutText.CREATED_AT + LONG
            + AboutText.UPDATED_AT + LONG_END
            + ")";
    private static final String CREATE_NOTIFICATION = ""
            + CREATE_TABLE + Notification.TABLE + "("
            + Notification.DATE + LONG
            + Notification.MESSAGE + TEXT
            + Notification.ICON + LONG_END
            + ")";
    private static final String CREATE_REASON_SUBMISSION = ""
            + CREATE_TABLE + StopReasonSubmission.TABLE + "("
            + StopReasonSubmission.REASON + LONG
            + StopReasonSubmission.DATE + LONG
            + StopReasonSubmission.SUBMISSION_ID + LONG_END
            + ")";
    private static final String CREATE_ANSWER_EMPTY = ""
            + CREATE_TABLE + Answer.TABLE + "_empty("
            + Answer.FIELD_ID + LONG
            + Answer.FORMAT + INTEGER
            + Answer.TYPE + INTEGER
            + Answer.VALUES + TEXT
            + "form_id" + LONG_END
            + ")";
    private static final String CREATE_ATTRIBUTION_TRANSFER = ""
            + CREATE_TABLE + AttributionTransfer.TABLE + "("
            + AttributionTransfer.SOURCE + LONG
            + AttributionTransfer.TARGET + LONG
            + AttributionTransfer.FORM_ID + LONG
            + AttributionTransfer.STATUS + " TEXT"
            + ")";
    private static final String CREATE_USER_SUBMISSION_APPROVED = ""
            + CREATE_TABLE + UserSubmission.TABLE + "_approved("
            + UserSubmission.ID + LONG
            + UserSubmission.FORM_ID + LONG
            + UserSubmission.LAST_RESCHEDULE_DATE + LONG
            + UserSubmission.STATUS + TEXT
            + UserSubmission.OWNER + LONG
            + UserSubmission.ANSWERS + TEXT
            + UserSubmission.USER_SUBMISSION_ID + LONG_END
            + ")";
    private static final String CREATE_USER_SUBMISSION_REPPROVED = ""
            + CREATE_TABLE + UserSubmission.TABLE + "_repproved("
            + UserSubmission.ID + LONG
            + UserSubmission.FORM_ID + LONG
            + UserSubmission.LAST_RESCHEDULE_DATE + LONG
            + UserSubmission.STATUS + TEXT
            + UserSubmission.OWNER + LONG
            + UserSubmission.ANSWERS + TEXT
            + UserSubmission.USER_SUBMISSION_ID + LONG_END
            + ")";

    private final ObjectMapper mObjectMapper;
    private final ParaPesquisaPreferences mPreferences;

    public ParaPesquisaOpenHelper(Context context, ObjectMapper objectMapper, ParaPesquisaPreferences preferences) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mObjectMapper = objectMapper;
        this.mObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.mPreferences = preferences;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TEMP);
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_USER_FORM);
        db.execSQL(CREATE_FORM);
        db.execSQL(CREATE_STOP_REASON);
        db.execSQL(CREATE_ATTRIBUTION);
        db.execSQL(CREATE_USER_SUBMISSION);
        db.execSQL(CREATE_SUBMISSION_LOG);
        db.execSQL(CREATE_SUBMISSION_CORRECTION);
        db.execSQL(CREATE_ABOUT_TEXT);
        db.execSQL(CREATE_USER_SUBMISSION_IN_PROGRESS);
        db.execSQL(CREATE_NOTIFICATION);
        db.execSQL(CREATE_USER_SUBMISSION_PENDING);
        db.execSQL(CREATE_REASON_SUBMISSION);
        db.execSQL(CREATE_ANSWER_EMPTY);
        db.execSQL(CREATE_ATTRIBUTION_TRANSFER);
        db.execSQL(CREATE_USER_SUBMISSION_REPPROVED);
        db.execSQL(CREATE_USER_SUBMISSION_APPROVED);

        db.execSQL("create index pp_forms_index on " + FormData.TABLE + "(" + FormData.ID + ")");
        db.execSQL("create index pp_submissions_form_index on " + UserSubmission.TABLE + "(" +
                UserSubmission.USER_SUBMISSION_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void deleteUserForms() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(UserForm.TABLE, null, null);
        db.delete(FormData.TABLE, null, null);
        db.delete(StopReason.TABLE, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void saveUserForms(List<UserForm> forms) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        for (int i = 0; i < forms.size(); i++) {
            saveUserForm(db, forms.get(i));
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void saveUserForm(SQLiteDatabase db, UserForm form) {
        db.insert(UserForm.TABLE, null,
                new UserForm.ContentBuilder()
                        .id(form.getId())
                        .formId(form.getFormId())
                        .quota(form.getQuota())
                        .user(form.getUser())
                        .moderator(form.getModerator())
                        .build());

        saveUser(db, form.getUser());
        if (form.getModerator() != null) saveUser(db, form.getModerator());
        saveForm(db, form.getForm());
    }

    private void saveForm(SQLiteDatabase db, FormData form) {
        ContentValues values = new FormData.ContentBuilder()
                .id(form.getId())
                .name(form.getName())
                .subtitle(form.getSubtitle())
                .pubStart(form.getPubStart())
                .pubEnd(form.getPubEnd())
                .maxReschedules(form.getMaxReschedules())
                .allowTransfer(form.isAllowTransfer())
                .allowNewSubmissions(form.isAllowNewSubmissions())
                .undefinedMode(form.isUndefinedMode())
                .createdAt(form.getCreatedAt())
                .updatedAt(form.getUpdatedAt())
                .build();
        try {
            values.put(FormData.SECTIONS, mObjectMapper.writeValueAsString(form.getSections()));
        } catch (JsonProcessingException e) {
            Timber.w(e, "Não deu...");
        }

        db.insert(FormData.TABLE, null, values);

        saveStopReasons(db, form.getStopReasons(), form.getId());
    }

    private void saveStopReasons(SQLiteDatabase db, List<StopReason> stopReasons, long formId) {
        for (int i = 0; i < stopReasons.size(); i++) {
            saveStopReason(db, stopReasons.get(i), formId);
        }
    }

    private void saveStopReason(SQLiteDatabase db, StopReason stopReason, long formId) {
        db.insert(StopReason.TABLE, null, new StopReason.ContentBuilder()
                .id(stopReason.getId())
                .reason(stopReason.getReason())
                .reschedule(stopReason.isReschedule())
                .formId(formId)
                .build());
    }

    private void saveUser(SQLiteDatabase db, UserData user) {
        if (getUser(user.getId()) == null) {
            db.insert(UserData.TABLE, null, new UserData.ContentBuilder()
                    .id(user.getId())
                    .name(user.getName())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .email(user.getEmail())
                    .createdAt(user.getCreatedAt())
                    .role(user.getRole())
                    .build());
        } else {
            db.update(UserData.TABLE, new UserData.ContentBuilder()
                    .id(user.getId())
                    .name(user.getName())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .email(user.getEmail())
                    .createdAt(user.getCreatedAt())
                    .role(user.getRole())
                    .build(), UserData.ID + "=" + user.getId(), null);
        }
    }

    public void deleteAttributions() {
        getWritableDatabase().delete(Attribution.TABLE, null, null);
    }

    public void saveAttributions(List<Attribution> attributions) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        for (int i = 0; i < attributions.size(); i++) {
            saveAttribution(db, attributions.get(i));
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<Attribution> getAttributions() {
        Cursor cursor = getReadableDatabase().query(Attribution.TABLE, Attribution.COLUMNS,
                null, null, null, null, null);
        cursor.moveToFirst();
        List<Attribution> attributions = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            attributions.add(Attribution.builder()
                    .id(getLong(cursor, Attribution.ID))
                    .user(getUser(getLong(cursor, Attribution.USER)))
                    .quota(getInt(cursor, Attribution.QUOTA))
                    .formId(getLong(cursor, Attribution.FORM_ID))
                    .build());
            cursor.moveToNext();
        }
        cursor.close();
        return attributions;
    }

    public List<Attribution> getAttributions(long formId) {
        Cursor cursor = getReadableDatabase().query(Attribution.TABLE, Attribution.COLUMNS,
                Attribution.FORM_ID + "=?", new String[]{String.valueOf(formId)}, null, null, null);
        cursor.moveToFirst();
        List<Attribution> attributions = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            attributions.add(Attribution.builder()
                    .id(getLong(cursor, Attribution.ID))
                    .user(getUser(getLong(cursor, Attribution.USER)))
                    .quota(getInt(cursor, Attribution.QUOTA))
                    .formId(getLong(cursor, Attribution.FORM_ID))
                    .build());
            cursor.moveToNext();
        }
        cursor.close();
        return attributions;
    }

    private void saveAttribution(SQLiteDatabase db, Attribution attribution) {
        db.insert(Attribution.TABLE, null, new Attribution.ContentBuilder()
                .id(attribution.getId())
                .user(attribution.getUser())
                .quota(attribution.getQuota())
                .formId(attribution.getFormId())
                .build());

        saveUser(db, attribution.getUser());
    }

    public void saveSubmissions(List<UserSubmission> submissions) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        for (int i = 0; i < submissions.size(); i++) {
            saveSubmission(db, submissions.get(i), null);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void deleteSubmissions() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(UserSubmission.TABLE, null, null);
        db.delete(SubmissionLog.TABLE, null, null);
        db.delete(SubmissionCorrection.TABLE, null, null);
        db.delete("TEMPORARY", null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void saveSubmission(SQLiteDatabase db, UserSubmission submission, @Nullable Long submissionId) {
        ContentValues values = new UserSubmission.ContentBuilder()
                .id(submission.getId() != null ? submission.getId() : -1l)
                .formId(submission.getFormId())
                .lastRescheduleDate(submission.getLastRescheduleDate())
                .status(submission.getStatus())
                .owner(submission.getOwner())
                .userSubmissionId(submissionId)
                .build();

        try {
            values.put(UserSubmission.ANSWERS, mObjectMapper.writeValueAsString(submission.getAnswers()));
        } catch (JsonProcessingException e) {
            Timber.w(e, "Falhou... :(");
        }

        db.insert(UserSubmission.TABLE, null, values);

        saveLog(db, submission.getLog(), submission.getId());
        saveCorrections(db, submission.getCorrections(), submission.getId());
        saveAlternatives(db, submission.getAlternatives(), submission.getId());
    }

    private void saveAlternatives(SQLiteDatabase db, @Nullable List<UserSubmission> alternatives, long submissionId) {
        if (alternatives == null) return;

        for (int i = 0; i < alternatives.size(); i++) {
            saveSubmission(db, alternatives.get(i), submissionId);
        }
    }

    private void saveCorrections(SQLiteDatabase db, @Nullable List<SubmissionCorrection> corrections, long submissionId) {
        if (corrections == null) return;

        for (int i = 0; i < corrections.size(); i++) {
            saveCorrection(db, corrections.get(i), submissionId);
        }
    }

    private void saveCorrection(SQLiteDatabase db, SubmissionCorrection correction, long submissionId) {
        db.insert(SubmissionCorrection.TABLE, null,
                new SubmissionCorrection.ContentBuilder()
                        .fieldId(correction.getFieldId())
                        .message(correction.getMessage())
                        .userId(correction.getUserId())
                        .createdAt(correction.getCreatedAt())
                        .userSubmissionId(submissionId)
                        .build());
    }

    private void saveLog(SQLiteDatabase db, List<SubmissionLog> log, long submissionId) {
        for (int i = 0; i < log.size(); i++) {
            saveLog(db, log.get(i), submissionId);
        }
    }

    private void saveLog(SQLiteDatabase db, SubmissionLog log, long submissionId) {
        db.insert(SubmissionLog.TABLE, null,
                new SubmissionLog.ContentBuilder()
                        .action(log.getAction())
                        .when(log.getWhen())
                        .userId(log.getUserId())
                        .userSubmissionId(submissionId)
                        .build());
    }

    public void deleteAboutText() {
        getWritableDatabase().delete(AboutText.TABLE, null, null);
    }

    public void saveAboutText(List<AboutText> texts) {
        for (int i = 0; i < texts.size(); i++) {
            saveAboutText(texts.get(i));
        }
    }

    private void saveAboutText(AboutText text) {
        getWritableDatabase().insert(AboutText.TABLE, null,
                new AboutText.ContentBuilder()
                        .id(text.getId())
                        .title(text.getTitle())
                        .subtitle(text.getSubtitle())
                        .content(text.getContent())
                        .createdAt(text.getCreatedAt())
                        .updatedAt(text.getUpdatedAt())
                        .build());
    }

    public List<UserForm> getUserForms() {
        Cursor cursor = getReadableDatabase()
                .query(UserForm.TABLE, UserForm.COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();
        List<UserForm> forms = new ArrayList<>(cursor.getCount());
        while (!cursor.isAfterLast()) {
            forms.add(UserForm.builder()
                    .id(getLong(cursor, UserForm.ID))
                    .formId(getLong(cursor, UserForm.FORM_ID))
                    .quota(getLong(cursor, UserForm.QUOTA))
                    .user(getUser(getLong(cursor, UserForm.USER)))
                    .form(getForm(getLong(cursor, UserForm.FORM_ID)))
                    .build());
            cursor.moveToNext();
        }
        cursor.close();
        return forms;
    }

    public FormData getForm(long formId) {
        Cursor cursor = getReadableDatabase()
                .query(FormData.TABLE, FormData.COLUMNS, FormData.ID + "=?",
                        new String[]{String.valueOf(formId)}, null, null, null);
        cursor.moveToFirst();
        FormData form = null;
        if (!cursor.isAfterLast()) {
            try {
                form = FormData.builder()
                        .id(getLong(cursor, FormData.ID))
                        .name(getString(cursor, FormData.NAME))
                        .subtitle(getString(cursor, FormData.SUBTITLE))
                        .pubStart(getDateTime(cursor, FormData.PUB_START))
                        .pubEnd(getDateTime(cursor, FormData.PUB_END))
                        .maxReschedules(getInt(cursor, FormData.MAX_RESCHEDULES))
                        .allowTransfer(getBoolean(cursor, FormData.ALLOW_TRANSFER))
                        .allowNewSubmissions(getBoolean(cursor, FormData.ALLOW_NEW_SUBMISSIONS))
                        .undefinedMode(getBoolean(cursor, FormData.UNDEFINED_MODE))
                        .createdAt(getDateTime(cursor, FormData.CREATED_AT))
                        .updatedAt(getDateTime(cursor, FormData.UPDATED_AT))
                        .stopReasons(getStopReasons(getLong(cursor, FormData.ID)))
                        .sections(mObjectMapper.readValue(getString(cursor, FormData.SECTIONS),
                                new TypeReference<List<Section>>() {
                                }))
                        .build();
            } catch (IOException e) {
                Timber.w(e, "Não deu mesmo... :(");
            }
        }
        cursor.close();
        return form;
    }

    private List<StopReason> getStopReasons(long formId) {
        Cursor cursor = getReadableDatabase()
                .query(StopReason.TABLE, StopReason.COLUMNS, StopReason.FORM_ID + "=?",
                        new String[]{String.valueOf(formId)}, null, null, null);
        cursor.moveToFirst();
        List<StopReason> reasons = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            reasons.add(StopReason.builder()
                    .id(getInt(cursor, StopReason.ID))
                    .reason(getString(cursor, StopReason.REASON))
                    .reschedule(getBoolean(cursor, StopReason.RESCHEDULE))
                    .build());
            cursor.moveToNext();
        }
        cursor.close();
        return reasons;
    }

    private UserData getUser(long userId) {
        Cursor cursor = getReadableDatabase()
                .query(UserData.TABLE, UserData.COLUMNS, UserData.ID + "=?",
                        new String[]{String.valueOf(userId)}, null, null, null);
        cursor.moveToFirst();
        UserData user = null;
        if (!cursor.isAfterLast()) {
            user = UserData.builder()
                    .id(getLong(cursor, UserData.ID))
                    .name(getString(cursor, UserData.NAME))
                    .username(getString(cursor, UserData.USERNAME))
                    .email(getString(cursor, UserData.EMAIL))
                    .avatarUrl(getString(cursor, UserData.AVATAR_URL))
                    .role(UserRole.get(getString(cursor, UserData.ROLE)))
                    .createdAt(getDateTime(cursor, UserData.CREATED_AT))
                    .build();
        }
        cursor.close();
        return user;
    }

    public long getRemainingSurveys(long formId) {
        UserForm form = getUserForm(formId);
        if (form != null) {
            return form.getQuota() - getSubmissionsNotNewCounter(formId);
        }
        return 0;
    }

    public long getRemainingSurveys(long formId, long quota) {
        return quota - getSubmissionsCount(formId) +
                getPendingSubmissionsCount(formId) -
                getRepprovedSubmissionsCount(formId) +
                getApprovedSubmissionsCount(formId);
    }

    private UserForm getUserForm(long formId) {
        Cursor cursor = getReadableDatabase().query(UserForm.TABLE, UserForm.COLUMNS,
                UserForm.FORM_ID + "=?", new String[]{String.valueOf(formId)}, null, null, null);
        cursor.moveToFirst();
        UserForm form = null;
        if (!cursor.isAfterLast()) {
            form = UserForm.builder()
                    .id(getLong(cursor, UserForm.ID))
                    .formId(getLong(cursor, UserForm.FORM_ID))
                    .quota(getLong(cursor, UserForm.QUOTA))
                    .user(getUser(getLong(cursor, UserForm.USER)))
                    .form(getForm(getLong(cursor, UserForm.FORM_ID)))
                    .build();
        }
        cursor.close();
        return form;
    }

    private long getSubmissionsNotNewCounter(long formId) {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), UserSubmission.TABLE,
                UserSubmission.FORM_ID + "=? AND " + UserSubmission.STATUS + " != ?",
                new String[]{String.valueOf(formId), SubmissionStatus.NEW.toString()});
    }

    public long getSubmissionsCount(long formId) {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), UserSubmission.TABLE,
                UserSubmission.FORM_ID + "=?", new String[]{String.valueOf(formId)});
    }

    public long getSubmissionsCount(long formId, SubmissionStatus status) {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), UserSubmission.TABLE,
                UserSubmission.FORM_ID + "=? AND " + UserSubmission.STATUS + "=?",
                new String[]{String.valueOf(formId), status.toString()});
    }

    public long getAllSubmissionsCount(long formId) {
        long submissions = getSubmissionsCount(formId);
        long pending = getPendingSubmissionsCount(formId);
        long approved = getApprovedSubmissionsCount(formId);
        long reproved = getRepprovedSubmissionsCount(formId);
        return submissions + pending + approved - reproved;
    }

    public List<UserSubmission> getSubmissions(long formId) {
        Cursor cursor = getReadableDatabase().query(UserSubmission.TABLE, UserSubmission.COLUMNS,
                UserSubmission.FORM_ID + "=?", new String[]{String.valueOf(formId)}, null, null, null);
        cursor.moveToFirst();
        List<UserSubmission> submissions = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            try {
                submissions.add(UserSubmission.builder()
                        .id(getLong(cursor, UserSubmission.ID))
                        .formId(getLong(cursor, UserSubmission.FORM_ID))
                        .lastRescheduleDate(getDateTime(cursor, UserSubmission.LAST_RESCHEDULE_DATE))
                        .status(SubmissionStatus.get(getString(cursor, UserSubmission.STATUS)))
                        .log(getSubmissionLog(getLong(cursor, UserSubmission.ID)))
                        .corrections(getSubmissionCorrections(getLong(cursor, UserSubmission.ID)))
                        .answers(mObjectMapper.readValue(getString(cursor, UserSubmission.ANSWERS),
                                new TypeReference<List<Answer>>() {
                                }))
                        .alternatives(getAlternatives(getLong(cursor, UserSubmission.ID)))
                        .owner(getUser(getLong(cursor, UserSubmission.OWNER)))
                        .build());
            } catch (IOException e) {
                Timber.e(e, "Falhou...");
            }
            cursor.moveToNext();
        }
        cursor.close();
        return submissions;
    }

    public List<UserSubmission> getSubmissions(long formId, SubmissionStatus status) {
        Cursor cursor = getReadableDatabase().query(UserSubmission.TABLE, UserSubmission.COLUMNS,
                UserSubmission.FORM_ID + "=? AND " + UserSubmission.STATUS + "=?",
                new String[]{String.valueOf(formId), status.toString()}, null, null, null);
        cursor.moveToFirst();
        List<UserSubmission> submissions = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            try {
                submissions.add(UserSubmission.builder()
                        .id(getLong(cursor, UserSubmission.ID))
                        .formId(getLong(cursor, UserSubmission.FORM_ID))
                        .lastRescheduleDate(getDateTime(cursor, UserSubmission.LAST_RESCHEDULE_DATE))
                        .status(SubmissionStatus.get(getString(cursor, UserSubmission.STATUS)))
                        .log(getSubmissionLog(getLong(cursor, UserSubmission.ID)))
                        .corrections(getSubmissionCorrections(getLong(cursor, UserSubmission.ID)))
                        .answers(mObjectMapper.readValue(getString(cursor, UserSubmission.ANSWERS),
                                new TypeReference<List<Answer>>() {
                                }))
                        .alternatives(getAlternatives(getLong(cursor, UserSubmission.ID)))
                        .owner(getUser(getLong(cursor, UserSubmission.OWNER)))
                        .build());
            } catch (IOException e) {
                Timber.e(e, "Falhou...");
            }
            cursor.moveToNext();
        }
        cursor.close();
        return submissions;
    }

    private List<UserSubmission> getSubmissionsNotNew(long formId) {
        Cursor cursor = getReadableDatabase().query(UserSubmission.TABLE, UserSubmission.COLUMNS,
                UserSubmission.FORM_ID + "=? AND " + UserSubmission.STATUS + " != ?",
                new String[]{String.valueOf(formId), SubmissionStatus.NEW.toString()}, null, null, null);
        cursor.moveToFirst();
        List<UserSubmission> submissions = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            try {
                submissions.add(UserSubmission.builder()
                        .id(getLong(cursor, UserSubmission.ID))
                        .formId(getLong(cursor, UserSubmission.FORM_ID))
                        .lastRescheduleDate(getDateTime(cursor, UserSubmission.LAST_RESCHEDULE_DATE))
                        .status(SubmissionStatus.get(getString(cursor, UserSubmission.STATUS)))
                        .log(getSubmissionLog(getLong(cursor, UserSubmission.ID)))
                        .corrections(getSubmissionCorrections(getLong(cursor, UserSubmission.ID)))
                        .answers(mObjectMapper.readValue(getString(cursor, UserSubmission.ANSWERS),
                                new TypeReference<List<Answer>>() {
                                }))
                        .alternatives(getAlternatives(getLong(cursor, UserSubmission.ID)))
                        .owner(getUser(getLong(cursor, UserSubmission.OWNER)))
                        .build());
            } catch (IOException e) {
                Timber.e(e, "Falhou...");
            }
            cursor.moveToNext();
        }
        cursor.close();
        return submissions;
    }

    private List<UserSubmission> getAlternatives(long submissionId) {
        Cursor cursor = getReadableDatabase().query(UserSubmission.TABLE, UserSubmission.COLUMNS,
                UserSubmission.USER_SUBMISSION_ID + "=?", new String[]{String.valueOf(submissionId)},
                null, null, null);
        cursor.moveToFirst();
        List<UserSubmission> submissions = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            try {
                submissions.add(UserSubmission.builder()
                        .id(getLong(cursor, UserSubmission.ID))
                        .formId(getLong(cursor, UserSubmission.FORM_ID))
                        .lastRescheduleDate(getDateTime(cursor, UserSubmission.LAST_RESCHEDULE_DATE))
                        .status(SubmissionStatus.get(getString(cursor, UserSubmission.STATUS)))
                        .log(getSubmissionLog(getLong(cursor, UserSubmission.ID)))
                        .corrections(getSubmissionCorrections(getLong(cursor, UserSubmission.ID)))
                        .answers(mObjectMapper.readValue(getString(cursor, UserSubmission.ANSWERS),
                                new TypeReference<List<Answer>>() {
                                }))
                        .alternatives(getAlternatives(getLong(cursor, UserSubmission.ID)))
                        .owner(getUser(getLong(cursor, UserSubmission.OWNER)))
                        .build());
            } catch (IOException e) {
                Timber.e(e, "Falhou...");
            }
            cursor.moveToNext();
        }
        cursor.close();
        return submissions;
    }

    private List<SubmissionCorrection> getSubmissionCorrections(long submissionId) {
        Cursor cursor = getReadableDatabase().query(SubmissionCorrection.TABLE, SubmissionCorrection.COLUMNS,
                SubmissionCorrection.USER_SUBMISSION_ID + "=?", new String[]{String.valueOf(submissionId)},
                null, null, null);
        cursor.moveToFirst();
        List<SubmissionCorrection> corrections = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            corrections.add(SubmissionCorrection.builder()
                    .fieldId(getLong(cursor, SubmissionCorrection.FIELD_ID))
                    .message(getString(cursor, SubmissionCorrection.MESSAGE))
                    .userId(getLong(cursor, SubmissionCorrection.USER_ID))
                    .createdAt(getDateTime(cursor, SubmissionCorrection.CREATED_AT))
                    .build());
            cursor.moveToNext();
        }
        cursor.close();
        return corrections;
    }

    private List<SubmissionLog> getSubmissionLog(long submissionId) {
        Cursor cursor = getReadableDatabase().query(SubmissionLog.TABLE, SubmissionLog.COLUMNS,
                SubmissionLog.USER_SUBMISSION_ID + "=?", new String[]{String.valueOf(submissionId)},
                null, null, null);
        cursor.moveToFirst();
        List<SubmissionLog> logs = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            logs.add(SubmissionLog.builder()
                    .action(SubmissionLogAction.get(getString(cursor, SubmissionLog.ACTION)))
                    .when(getDateTime(cursor, SubmissionLog.WHEN))
                    .userId(getLong(cursor, SubmissionLog.USER_ID))
                    .build());
            cursor.moveToNext();
        }
        cursor.close();
        return logs;
    }

    public List<UserSubmission> getFilteredSubmissions(long formId, @Nullable SubmissionStatus status,
                                                       @Nullable UserData userData) {
        return getFilteredSubmissions(formId, status, userData, 0);
    }

    public List<UserSubmission> getFilteredSubmissions(long formId, @Nullable SubmissionStatus status,
                                                       @Nullable UserData userData, int offset) {

        StringBuilder condition = new StringBuilder();
        condition.append(UserSubmission.FORM_ID).append("=? AND ").append(UserSubmission.STATUS)
                .append(" != ?");
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(formId));
        values.add(SubmissionStatus.NEW.toString());

        if (status != null) {
            condition.append(" AND ").append(UserSubmission.STATUS).append("=?");
            values.add(status.toString());
        }
        if (userData != null) {
            condition.append(" AND ").append(UserSubmission.OWNER).append("=?");
            values.add(String.valueOf(userData.getId()));
        }

        condition.append(" limit ").append(offset).append(",25");

        Cursor cursor = getReadableDatabase().query(UserSubmission.TABLE, UserSubmission.COLUMNS,
                condition.toString(), values.toArray(new String[values.size()]), null, null, null);
        cursor.moveToFirst();
        List<UserSubmission> submissions = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            try {
                submissions.add(UserSubmission.builder()
                        .id(getLong(cursor, UserSubmission.ID))
                        .formId(getLong(cursor, UserSubmission.FORM_ID))
                        .lastRescheduleDate(getDateTime(cursor, UserSubmission.LAST_RESCHEDULE_DATE))
                        .status(SubmissionStatus.get(getString(cursor, UserSubmission.STATUS)))
                        .log(getSubmissionLog(getLong(cursor, UserSubmission.ID)))
                        .corrections(getSubmissionCorrections(getLong(cursor, UserSubmission.ID)))
                        .answers(mObjectMapper.readValue(getString(cursor, UserSubmission.ANSWERS),
                                new TypeReference<List<Answer>>() {
                                }))
                        .alternatives(getAlternatives(getLong(cursor, UserSubmission.ID)))
                        .owner(getUser(getLong(cursor, UserSubmission.OWNER)))
                        .build());
            } catch (IOException e) {
                Timber.e(e, "Falhou...");
            }
            cursor.moveToNext();
        }
        cursor.close();
        return submissions;
    }

    @Nullable
    public String getIdentifier(UserSubmission submission) {
        if (submission == null) return null;
        Field field = getIdentifierField(submission.getFormId());
        if (field == null) return null;

        return getIdentifier(submission, field);
    }

    @Nullable
    private String getIdentifier(UserSubmission submission, Field field) {
        for (Answer answer : submission.getAnswers()) {
            if (answer != null && answer.getFieldId() == field.getId()) {
                return answer.getValues();
            }
        }
        return null;
    }

    @Nullable
    public Field getIdentifierField(long formId) {
        FormData formData = getForm(formId);
        if (formData.getSections() != null) {
            for (int i = 0; i < formData.getSections().size(); i++) {
                Section section = formData.getSections().get(i);
                if (section.getFields() != null) {
                    for (int j = 0; j < section.getFields().size(); j++) {
                        Field field = section.getFields().get(j);
                        if (field.isIdentifier()) return field;
                    }
                }
            }
        }
        return null;
    }

    public boolean hasSubmissionInProgress(long formId) {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), UserSubmission.TABLE + "_in_progress",
                UserSubmission.FORM_ID + "=?", new String[]{String.valueOf(formId)}) > 0;
    }

    @Nullable
    public UserSubmission getSubmissionInProgress(long formId) {
        Cursor cursor = getReadableDatabase().query(UserSubmission.TABLE + "_in_progress", UserSubmission.COLUMNS,
                UserSubmission.FORM_ID + "=?", new String[]{String.valueOf(formId)}, null, null, null);
        cursor.moveToFirst();
        UserSubmission submission = null;
        if (!cursor.isAfterLast()) {
            try {
                submission = UserSubmission.builder()
                        .id(getLong(cursor, UserSubmission.ID))
                        .formId(getLong(cursor, UserSubmission.FORM_ID))
                        .lastRescheduleDate(getDateTime(cursor, UserSubmission.LAST_RESCHEDULE_DATE))
                        .status(SubmissionStatus.IN_PROGRESS)
                        .inProgress(Boolean.TRUE)
                        .log(getSubmissionLog(getLong(cursor, UserSubmission.ID)))
                        .corrections(getSubmissionCorrections(getLong(cursor, UserSubmission.ID)))
                        .answers(mObjectMapper.readValue(getString(cursor, UserSubmission.ANSWERS),
                                new TypeReference<List<Answer>>() {
                                }))
                        .alternatives(getAlternatives(getLong(cursor, UserSubmission.ID)))
                        .owner(getUser(getLong(cursor, UserSubmission.OWNER)))
                        .build();
            } catch (IOException e) {
                Timber.e(e, "Falhou...");
            }
        }
        cursor.close();
        return submission;
    }

    public void clearNotifications() {
        getWritableDatabase().delete(Notification.TABLE, null, null);
    }

    @Nullable
    public String getFormName(long formId) {
        Cursor cursor = getReadableDatabase()
                .query(FormData.TABLE, new String[]{FormData.NAME}, FormData.ID + "=?",
                        new String[]{String.valueOf(formId)}, null, null, null);
        cursor.moveToFirst();
        String name = null;
        if (!cursor.isAfterLast()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public List<Notification> getNotifications() {
        Cursor cursor = getReadableDatabase().query(Notification.TABLE, Notification.COLUMNS,
                null, null, null, null, null);
        cursor.moveToFirst();
        List<Notification> notifications = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            notifications.add(Notification.builder()
                    .message(getString(cursor, Notification.MESSAGE))
                    .date(getDateTime(cursor, Notification.DATE))
                    .icon(getInt(cursor, Notification.ICON))
                    .build());
            cursor.moveToNext();
        }
        cursor.close();
        return notifications;
    }

    public UserForm getUserFormById(long id) {
        Cursor cursor = getReadableDatabase().query(UserForm.TABLE, UserForm.COLUMNS,
                UserForm.ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        cursor.moveToFirst();
        UserForm form = null;
        if (!cursor.isAfterLast()) {
            form = UserForm.builder()
                    .id(getLong(cursor, UserForm.ID))
                    .formId(getLong(cursor, UserForm.FORM_ID))
                    .quota(getLong(cursor, UserForm.QUOTA))
                    .user(getUser(getLong(cursor, UserForm.USER)))
                    .form(getForm(getLong(cursor, UserForm.FORM_ID)))
                    .build();
        }
        cursor.close();
        return form;
    }

    public boolean hasExtraData(long formId) {
        return getSubmissionsCount(formId, SubmissionStatus.NEW) > 0;
    }

    public void saveNotifications(List<Notification> notifications) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        for (int i = 0; i < notifications.size(); i++) {
            saveNotification(db, notifications.get(i));
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void saveNotification(SQLiteDatabase db, Notification notification) {
        db.insert(Notification.TABLE, null, new Notification.ContentBuilder()
                .date(notification.getDate())
                .icon(notification.getIcon())
                .message(notification.getMessage())
                .build());
    }

    public void removeSubmissionInProgress(long formId) {
        getWritableDatabase().delete(UserSubmission.TABLE + "_in_progress", UserSubmission.FORM_ID + "=?",
                new String[]{String.valueOf(formId)});
    }

    public void addSubmission(long formId, UserSubmission submission) {
        long submissionId = getSubmissionId(submission);

        ContentValues values = new UserSubmission.ContentBuilder()
                .id(submissionId)
                .formId(formId)
                .lastRescheduleDate(submission.getLastRescheduleDate())
                .status(submission.getStatus())
                .owner(submission.getOwner())
                .build();

        try {
            values.put(UserSubmission.ANSWERS, mObjectMapper.writeValueAsString(submission.getAnswers()));
        } catch (JsonProcessingException e) {
            Timber.w(e, "Falhou... :(");
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.insert(UserSubmission.TABLE + "_pending", null, values);

        saveLog(db, submission.getLog(), submissionId);
        saveCorrections(db, submission.getCorrections(), submissionId);
        saveAlternatives(db, submission.getAlternatives(), submissionId);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addSubmissionForReschedule(long formId, UserSubmission submission, StopReason reason, DateTime date) {
        long submissionId = getSubmissionId(submission);

        ContentValues values = new UserSubmission.ContentBuilder()
                .id(submissionId)
                .formId(formId)
                .lastRescheduleDate(submission.getLastRescheduleDate())
                .status(SubmissionStatus.RESCHEDULED)
                .owner(submission.getOwner())
                .build();

        try {
            values.put(UserSubmission.ANSWERS, mObjectMapper.writeValueAsString(submission.getAnswers()));
        } catch (JsonProcessingException e) {
            Timber.w(e, "Falhou... :(");
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.insert(UserSubmission.TABLE + "_pending", null, values);

        saveLog(db, submission.getLog(), submissionId);
        saveCorrections(db, submission.getCorrections(), submissionId);
        saveAlternatives(db, submission.getAlternatives(), submissionId);

        addReasonSubmission(db, reason, date.toDate(), submissionId);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void addReasonSubmission(SQLiteDatabase db, StopReason reason, Date date, long submissionId) {
        db.insert(StopReasonSubmission.TABLE, null, new StopReasonSubmission.ContentBuilder()
                .date(new DateTime(date))
                .reason(reason)
                .submissionId(submissionId)
                .build());
    }

    public void addSubmissionForCancel(long formId, UserSubmission submission, StopReason reason) {
        long submissionId = getSubmissionId(submission);

        ContentValues values = new UserSubmission.ContentBuilder()
                .id(submissionId)
                .formId(formId)
                .lastRescheduleDate(submission.getLastRescheduleDate())
                .status(SubmissionStatus.CANCELLED)
                .owner(submission.getOwner())
                .build();

        try {
            values.put(UserSubmission.ANSWERS, mObjectMapper.writeValueAsString(submission.getAnswers()));
        } catch (JsonProcessingException e) {
            Timber.w(e, "Falhou... :(");
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.insert(UserSubmission.TABLE + "_pending", null, values);

        saveLog(db, submission.getLog(), submissionId);
        saveCorrections(db, submission.getCorrections(), submissionId);
        saveAlternatives(db, submission.getAlternatives(), submissionId);

        addReasonSubmission(db, reason, submissionId);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void addReasonSubmission(SQLiteDatabase db, StopReason reason, long submissionId) {
        addReasonSubmission(db, reason, null, submissionId);
    }

    public List<Answer> getEmptyAnswers(long formId) {
        Cursor cursor = getReadableDatabase().query(Answer.TABLE + "_empty", Answer.COLUMNS,
                "form_id" + "=?", new String[]{String.valueOf(formId)},
                null, null, null);
        cursor.moveToFirst();
        List<Answer> answers = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            answers.add(Answer.builder()
                    .type(getInt(cursor, Answer.TYPE))
                    .format(getInt(cursor, Answer.FORMAT))
                    .values(getString(cursor, Answer.VALUES))
                    .fieldId(getLong(cursor, Answer.FIELD_ID))
                    .build());
            cursor.moveToNext();
        }
        cursor.close();
        return answers;
    }

    public void saveEmptyAnswers(long formId, List<Answer> answers) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        saveEmptyAnswers(db, answers, formId);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void setSubmissionInProgress(long formId, UserSubmission submission) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        db.delete(UserSubmission.TABLE + "_in_progress", UserSubmission.FORM_ID + "=?",
                new String[]{String.valueOf(formId)});

        long submissionId = getSubmissionId(submission);

        ContentValues values = new UserSubmission.ContentBuilder()
                .id(submissionId)
                .formId(formId)
                .lastRescheduleDate(submission.getLastRescheduleDate())
                .status(submission.getStatus())
                .owner(submission.getOwner())
                .build();

        try {
            values.put(UserSubmission.ANSWERS, mObjectMapper.writeValueAsString(submission.getAnswers()));
        } catch (JsonProcessingException e) {
            Timber.w(e, "Falhou... :(");
        }

        db.insert(UserSubmission.TABLE + "_in_progress", null, values);

        saveLog(db, submission.getLog(), submissionId);
        saveCorrections(db, submission.getCorrections(), submissionId);
        saveAlternatives(db, submission.getAlternatives(), submissionId);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void saveEmptyAnswers(SQLiteDatabase db, List<Answer> answers, long formId) {
        for (int i = 0; i < answers.size(); i++) {
            saveEmptyAnswer(db, answers.get(i), formId);
        }
    }

    private void saveEmptyAnswer(SQLiteDatabase db, Answer answer, long formId) {
        ContentValues values = new Answer.ContentBuilder()
                .fieldId(answer.getFieldId())
                .format(answer.getFormat())
                .type(answer.getType())
                .values(answer.getValues())
                .build();

        values.put("form_id", formId);

        db.insert(Answer.TABLE + "_empty", null, values);
    }

    public void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        db.delete(UserData.TABLE, null, null);
        db.delete(UserForm.TABLE, null, null);
        db.delete(FormData.TABLE, null, null);
        db.delete(StopReason.TABLE, null, null);
        db.delete(Attribution.TABLE, null, null);
        db.delete(UserSubmission.TABLE, null, null);
        db.delete(UserSubmission.TABLE + "_in_progress", null, null);
        db.delete(UserSubmission.TABLE + "_pending", null, null);
        db.delete(UserSubmission.TABLE + "_approved", null, null);
        db.delete(UserSubmission.TABLE + "_repproved", null, null);
        db.delete(SubmissionLog.TABLE, null, null);
        db.delete(SubmissionCorrection.TABLE, null, null);
        db.delete(AboutText.TABLE, null, null);
        db.delete(Notification.TABLE, null, null);
        db.delete(StopReasonSubmission.TABLE, null, null);
        db.delete(Answer.TABLE + "_empty", null, null);
        db.delete(AttributionTransfer.TABLE, null, null);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<AboutText> getAboutText() {
        Cursor cursor = getReadableDatabase()
                .query(AboutText.TABLE, AboutText.COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();
        List<AboutText> texts = new ArrayList<>(cursor.getCount());
        while (!cursor.isAfterLast()) {
            texts.add(AboutText.builder()
                    .id(getLong(cursor, AboutText.ID))
                    .title(getString(cursor, AboutText.TITLE))
                    .subtitle(getString(cursor, AboutText.SUBTITLE))
                    .content(getString(cursor, AboutText.CONTENT))
                    .createdAt(getDateTime(cursor, AboutText.CREATED_AT))
                    .updatedAt(getDateTime(cursor, AboutText.UPDATED_AT))
                    .build());
            cursor.moveToNext();
        }
        cursor.close();
        return texts;
    }

    public void removeSubmission(long formId, Long submissionId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(UserSubmission.TABLE,
                UserSubmission.FORM_ID + "=? AND " + UserSubmission.ID + "=?",
                new String[]{String.valueOf(formId), String.valueOf(submissionId)});
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<UserSubmission> getPendingSubmissions() {
        Cursor cursor = getReadableDatabase().query(UserSubmission.TABLE + "_pending", UserSubmission.COLUMNS,
                null, null, null, null, null);
        cursor.moveToFirst();
        List<UserSubmission> submissions = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            try {
                submissions.add(UserSubmission.builder()
                        .id(getLong(cursor, UserSubmission.ID))
                        .formId(getLong(cursor, UserSubmission.FORM_ID))
                        .lastRescheduleDate(getDateTime(cursor, UserSubmission.LAST_RESCHEDULE_DATE))
                        .status(SubmissionStatus.get(getString(cursor, UserSubmission.STATUS)))
                        .log(getSubmissionLog(getLong(cursor, UserSubmission.ID)))
                        .corrections(getSubmissionCorrections(getLong(cursor, UserSubmission.ID)))
                        .answers(mObjectMapper.readValue(getString(cursor, UserSubmission.ANSWERS),
                                new TypeReference<List<Answer>>() {
                                }))
                        .alternatives(getAlternatives(getLong(cursor, UserSubmission.ID)))
                        .owner(getUser(getLong(cursor, UserSubmission.OWNER)))
                        .build());
            } catch (IOException e) {
                Timber.e(e, "Falhou... :(");
            }
            cursor.moveToNext();
        }
        cursor.close();
        return submissions;
    }

    public void updatePendingSubmission(UserSubmission submission) {
        if (submission.getLatestLog() == null) return;

        long id = getSubmissionIdByLog(submission.getLatestLog().getWhen());
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        removePendingSubmission(db, id);
        addPendingSubmission(db, submission);
    }

    private void addPendingSubmission(SQLiteDatabase db, UserSubmission submission) {
        long submissionId = getSubmissionId(submission);

        ContentValues values = new UserSubmission.ContentBuilder()
                .id(submissionId)
                .formId(submission.getFormId())
                .lastRescheduleDate(submission.getLastRescheduleDate())
                .status(submission.getStatus())
                .owner(submission.getOwner())
                .build();

        try {
            values.put(UserSubmission.ANSWERS, mObjectMapper.writeValueAsString(submission.getAnswers()));
        } catch (JsonProcessingException e) {
            Timber.w(e, "Falhou... :(");
        }

        db.insert(UserSubmission.TABLE + "_pending", null, values);

        saveLog(db, submission.getLog(), submissionId);
        saveCorrections(db, submission.getCorrections(), submissionId);
        saveAlternatives(db, submission.getAlternatives(), submissionId);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void removePendingSubmission(SQLiteDatabase db, long submissionId) {
        db.delete(UserSubmission.TABLE + "_pending", UserSubmission.ID + "=?",
                new String[]{String.valueOf(submissionId)});
    }

    public void removePendingSubmission(UserSubmission submission) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        removePendingSubmission(db, submission.getId());
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void removePendingSubmission(long submissionId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        removePendingSubmission(db, submissionId);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private long getSubmissionIdByLog(DateTime when) {
        Cursor cursor = getReadableDatabase().query(SubmissionLog.TABLE, new String[]{SubmissionLog.USER_SUBMISSION_ID},
                SubmissionLog.WHEN + "=?", new String[]{String.valueOf(when.toDate().getTime())}, null, null, null);
        cursor.moveToFirst();
        long value = -1l;
        if (!cursor.isAfterLast()) {
            value = getLong(cursor, SubmissionLog.USER_SUBMISSION_ID);
        }
        cursor.close();
        return value;
    }

    public void clearPendingSubmissions() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(UserSubmission.TABLE + "_pending", null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public Pair<DateTime, Long> getRescheduleReason(UserSubmission submission) {
        return getRescheduleReasonBySubmissionId(submission.getId());
    }

    public Pair<DateTime, Long> getRescheduleReasonBySubmissionId(long submissionId) {
        Cursor cursor = getReadableDatabase().query(StopReasonSubmission.TABLE, new String[]{StopReasonSubmission.REASON,
                        StopReasonSubmission.DATE},
                StopReasonSubmission.SUBMISSION_ID + "=?", new String[]{String.valueOf(submissionId)},
                null, null, null);
        cursor.moveToFirst();
        Pair<DateTime, Long> data = null;
        if (!cursor.isAfterLast()) {
            data = new Pair<>(getDateTime(cursor, StopReasonSubmission.DATE),
                    getLong(cursor, StopReasonSubmission.REASON));
        }
        cursor.close();
        return data;
    }

    public long getCancelReasonId(UserSubmission submission) {
        return getCancelReasonId(submission.getId());
    }

    public long getCancelReasonId(long submissionId) {
        Cursor cursor = getReadableDatabase().query(StopReasonSubmission.TABLE, new String[]{StopReasonSubmission.REASON},
                StopReasonSubmission.SUBMISSION_ID + "=?", new String[]{String.valueOf(submissionId)},
                null, null, null);
        cursor.moveToFirst();
        long reason = -1;
        if (!cursor.isAfterLast()) {
            reason = cursor.getLong(0);
        }
        cursor.close();
        return reason;
    }

    public List<UserData> getUsers(long formId) {
        Cursor cursor = getReadableDatabase().query(Attribution.TABLE, new String[]{Attribution.USER},
                Attribution.FORM_ID + "=?", new String[]{String.valueOf(formId)}, null, null, null);
        cursor.moveToFirst();
        List<UserData> users = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            users.add(getUser(cursor.getLong(0)));
            cursor.moveToNext();
        }
        cursor.close();
        return users;
    }

    public void addTransfer(long source, long formId, SubmissionStatus status, long destination) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.insert(AttributionTransfer.TABLE, null, new AttributionTransfer.ContentBuilder()
                .source(source)
                .target(destination)
                .formId(formId)
                .status(status)
                .build());
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<AttributionTransfer> getTransfers() {
        Cursor cursor = getReadableDatabase().query(AttributionTransfer.TABLE, AttributionTransfer.COLUMNS,
                null, null, null, null, null);
        cursor.moveToFirst();
        List<AttributionTransfer> transfers = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            transfers.add(AttributionTransfer.builder()
                    .source(getLong(cursor, AttributionTransfer.SOURCE))
                    .target(getLong(cursor, AttributionTransfer.TARGET))
                    .formId(getLong(cursor, AttributionTransfer.FORM_ID))
                    .status(SubmissionStatus.get(getString(cursor, AttributionTransfer.STATUS)))
                    .build());
            cursor.moveToNext();
        }
        cursor.close();
        return transfers;
    }

    public void addApprovedSubmission(UserSubmission submission) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        ContentValues values = new UserSubmission.ContentBuilder()
                .id(submission.getId())
                .formId(submission.getFormId())
                .lastRescheduleDate(submission.getLastRescheduleDate())
                .status(SubmissionStatus.APPROVED)
                .owner(submission.getOwner())
                .build();

        try {
            values.put(UserSubmission.ANSWERS, mObjectMapper.writeValueAsString(submission.getAnswers()));
        } catch (JsonProcessingException e) {
            Timber.w(e, "Falhou... :(");
        }

        db.insert(UserSubmission.TABLE + "_approved", null, values);

        saveLog(db, submission.getLog(), submission.getId());
        saveCorrections(db, submission.getCorrections(), submission.getId());
        saveAlternatives(db, submission.getAlternatives(), submission.getId());
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<UserSubmission> getRepprovedSubmissions() {
        Cursor cursor = getReadableDatabase().query(UserSubmission.TABLE + "_repproved", UserSubmission.COLUMNS,
                null, null, null, null, null);
        cursor.moveToFirst();
        List<UserSubmission> submissions = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            try {
                submissions.add(UserSubmission.builder()
                        .id(getLong(cursor, UserSubmission.ID))
                        .formId(getLong(cursor, UserSubmission.FORM_ID))
                        .lastRescheduleDate(getDateTime(cursor, UserSubmission.LAST_RESCHEDULE_DATE))
                        .status(SubmissionStatus.get(getString(cursor, UserSubmission.STATUS)))
                        .log(getSubmissionLog(getLong(cursor, UserSubmission.ID)))
                        .corrections(getSubmissionCorrections(getLong(cursor, UserSubmission.ID)))
                        .answers(mObjectMapper.readValue(getString(cursor, UserSubmission.ANSWERS),
                                new TypeReference<List<Answer>>() {
                                }))
                        .alternatives(getAlternatives(getLong(cursor, UserSubmission.ID)))
                        .owner(getUser(getLong(cursor, UserSubmission.OWNER)))
                        .build());
            } catch (IOException e) {
                Timber.e(e, "Falhou...");
            }
            cursor.moveToNext();
        }
        cursor.close();
        return submissions;
    }

    public List<UserSubmission> getApprovedSubmissions() {
        Cursor cursor = getReadableDatabase().query(UserSubmission.TABLE + "_approved", UserSubmission.COLUMNS,
                null, null, null, null, null);
        cursor.moveToFirst();
        List<UserSubmission> submissions = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            try {
                submissions.add(UserSubmission.builder()
                        .id(getLong(cursor, UserSubmission.ID))
                        .formId(getLong(cursor, UserSubmission.FORM_ID))
                        .lastRescheduleDate(getDateTime(cursor, UserSubmission.LAST_RESCHEDULE_DATE))
                        .status(SubmissionStatus.get(getString(cursor, UserSubmission.STATUS)))
                        .log(getSubmissionLog(getLong(cursor, UserSubmission.ID)))
                        .corrections(getSubmissionCorrections(getLong(cursor, UserSubmission.ID)))
                        .answers(mObjectMapper.readValue(getString(cursor, UserSubmission.ANSWERS),
                                new TypeReference<List<Answer>>() {
                                }))
                        .alternatives(getAlternatives(getLong(cursor, UserSubmission.ID)))
                        .owner(getUser(getLong(cursor, UserSubmission.OWNER)))
                        .build());
            } catch (IOException e) {
                Timber.e(e, "Falhou...");
            }
            cursor.moveToNext();
        }
        cursor.close();
        return submissions;
    }

    public void addRejectedSubmission(UserSubmission submission) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        ContentValues values = new UserSubmission.ContentBuilder()
                .id(submission.getId())
                .formId(submission.getFormId())
                .lastRescheduleDate(submission.getLastRescheduleDate())
                .status(null)
                .owner(submission.getOwner())
                .build();

        try {
            values.put(UserSubmission.ANSWERS, mObjectMapper.writeValueAsString(submission.getAnswers()));
        } catch (JsonProcessingException e) {
            Timber.w(e, "Falhou... :(");
        }

        db.insert(UserSubmission.TABLE + "_repproved", null, values);

        saveLog(db, submission.getLog(), submission.getId());
        saveCorrections(db, submission.getCorrections(), submission.getId());
        saveAlternatives(db, submission.getAlternatives(), submission.getId());
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void clearTransfers() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(AttributionTransfer.TABLE, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<UserSubmission> getPendingSubmissions(long formId) {
        return getPendingSubmissions(formId, null);
    }

    public List<UserSubmission> getPendingSubmissions(long formId, @Nullable SubmissionStatus status) {
        Cursor cursor;
        if (status == null) {
            cursor = getReadableDatabase().query(UserSubmission.TABLE + "_pending", UserSubmission.COLUMNS,
                    UserSubmission.FORM_ID + "=?", new String[]{String.valueOf(formId)}, null, null, null);
        } else {
            cursor = getReadableDatabase().query(UserSubmission.TABLE + "_pending", UserSubmission.COLUMNS,
                    UserSubmission.FORM_ID + "=? AND " + UserSubmission.STATUS + "=?",
                    new String[]{String.valueOf(formId), status.toString()}, null, null, null);
        }

        cursor.moveToFirst();
        List<UserSubmission> submissions = new ArrayList<>();
        mObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        while (!cursor.isAfterLast()) {
            try {
                submissions.add(UserSubmission.builder()
                        .id(getLong(cursor, UserSubmission.ID))
                        .formId(getLong(cursor, UserSubmission.FORM_ID))
                        .lastRescheduleDate(getDateTime(cursor, UserSubmission.LAST_RESCHEDULE_DATE))
                        .status(SubmissionStatus.get(getString(cursor, UserSubmission.STATUS)))
                        .log(getSubmissionLog(getLong(cursor, UserSubmission.ID)))
                        .corrections(getSubmissionCorrections(getLong(cursor, UserSubmission.ID)))
                        .answers(mObjectMapper.readValue(getString(cursor, UserSubmission.ANSWERS),
                                new TypeReference<List<Answer>>() {
                                }))
                        .alternatives(getAlternatives(getLong(cursor, UserSubmission.ID)))
                        .owner(getUser(getLong(cursor, UserSubmission.OWNER)))
                        .build());
            } catch (IOException e) {
                Timber.e(e, "Falhou...");
            }
            cursor.moveToNext();
        }
        cursor.close();
        return submissions;
    }

    public void removeApprovedSubmissions() {
        getWritableDatabase().delete(UserSubmission.TABLE + "_approved", null, null);
    }

    public void removeRepprovedSubmissions() {
        getWritableDatabase().delete(UserSubmission.TABLE + "_repproved", null, null);
    }

    public void removePendingSubmissions() {
        getWritableDatabase().delete(UserSubmission.TABLE + "_pending", null, null);
    }

    public long getApprovedSubmissionsCount(long formId) {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), UserSubmission.TABLE + "_approved",
                UserSubmission.FORM_ID + "=?", new String[]{String.valueOf(formId)});
    }

    public long getRepprovedSubmissionsCount(long formId) {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), UserSubmission.TABLE + "_repproved",
                UserSubmission.FORM_ID + "=?", new String[]{String.valueOf(formId)});
    }

    public long getPendingSubmissionsCount(long formId) {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), UserSubmission.TABLE + "_pending",
                UserSubmission.FORM_ID + "=?", new String[]{String.valueOf(formId)});
    }

    private long getSubmissionId(final UserSubmission submission) {
        final long submissionId;

        if (submission.getId() != null) {
           submissionId = submission.getId();
            Timber.i("There is: %s", hasTemp(submissionId));
        } else {
            submissionId = System.currentTimeMillis();
            saveTemp(submissionId);
        }

        return submissionId;
    }

    private void saveTemp(Long value) {
        final ContentValues values = new ContentValues();
        values.put("_id", value);
        getWritableDatabase().insert("TEMPORARY", null, values);
    }

    public boolean hasTemp(long formId) {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), "TEMPORARY",
                "_id=?", new String[]{String.valueOf(formId)}) > 0;
    }

}
