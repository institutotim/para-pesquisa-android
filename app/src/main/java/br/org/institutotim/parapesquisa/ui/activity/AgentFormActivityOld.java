package br.org.institutotim.parapesquisa.ui.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.event.FieldActionEvent;
import br.org.institutotim.parapesquisa.data.event.FormUpdatedEvent;
import br.org.institutotim.parapesquisa.data.event.ScrollEvent;
import br.org.institutotim.parapesquisa.data.event.SyncCompletedEvent;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.StopReason;
import br.org.institutotim.parapesquisa.data.model.SubmissionLogAction;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.helper.FieldActionHelper;
import br.org.institutotim.parapesquisa.ui.helper.FieldHelper;
import br.org.institutotim.parapesquisa.ui.helper.FormHelper;
import br.org.institutotim.parapesquisa.ui.helper.SectionHelper;
import br.org.institutotim.parapesquisa.ui.helper.SubmissionHelper;
import br.org.institutotim.parapesquisa.ui.validator.SectionValidator;
import br.org.institutotim.parapesquisa.util.DateUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class AgentFormActivityOld extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.container)
    ViewGroup mContainer;
    @Bind(R.id.section_indicator)
    TextView mSectionIndicator;

    @Bind(R.id.next)
    View mNext;
    @Bind(R.id.previous)
    View mPrevious;
    @Bind(R.id.navigation)
    View mNavigation;

    @Inject
    ParaPesquisaOpenHelper mHelper;
    @Inject
    SubmissionHelper mSubmissionHelper;
    @Inject
    SectionHelper mSectionHelper;
    @Inject
    FieldHelper mFieldHelper;
    @Inject
    FieldActionHelper mFieldActionHelper;
    @Inject
    FormHelper mFormHelper;
    @Inject
    SectionValidator mSectionValidator;

    @Inject
    ObjectMapper mObjectMapper;
    @Inject
    ParaPesquisaPreferences mPreferences;

    private FormData mForm;
    private UserSubmission mSubmission;

    private List<View> mSections = new ArrayList<>();
    private int mCurrentSection;
    private ScrollView mCurrentContainer;

    private Date mTimestamp = new Date();

    private DateTime mDate;
    private boolean mRescheduling = false;

    List<Answer> mEmptyAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_form);
        ButterKnife.bind(this);
        getComponent().inject(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mForm = getIntent().getParcelableExtra(FORM_EXTRA);

        showProgressDialog();
        new Thread(() -> {
            mHelper.removeSubmissionInProgress(mForm.getId());
            if (getIntent().hasExtra(SUBMISSION_EXTRA)) {
                mSubmission = getIntent().getParcelableExtra(SUBMISSION_EXTRA);
                if (mSubmission.getId() != null && mSubmission.getId() != 0) {
                    runOnUiThread(() -> getSupportActionBar().setSubtitle(mHelper.getIdentifier(mSubmission)));
                }
            }

            setUpForm();
            handleFieldActions();

            selectSection(mSubmission != null && mSubmission.getCurrentPage() != null ? mSubmission.getCurrentPage() : 1);

            if (getIntent().getBooleanExtra(RESCHEDULE_EXTRA, false)) {
                showReasonPopup();
            }
            dismissProgressDialog();
        }).start();
    }

    private List<Answer> getEmptyAnswers() {
        List<Answer> answers = mHelper.getEmptyAnswers(mForm.getId());
        if (answers == null) {
            answers = mSubmissionHelper.extractAnswers(mTimestamp, mSections, mSubmission).getAnswers();
            mHelper.saveEmptyAnswers(mForm.getId(), answers);
        }
        return answers;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.form, menu);

        if (!mForm.hasExtraData()) {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    public void backAction() {
        UserSubmission submission = mSubmissionHelper.extractAnswers(mTimestamp, mSections, mSubmission,
                mCurrentSection);

        try {
            String user = mObjectMapper.writeValueAsString(submission.getAnswers());
            String empty = mObjectMapper.writeValueAsString(mEmptyAnswers);

            if (!user.equals(empty)) {
                mHelper.setSubmissionInProgress(mForm.getId(), submission);
            }
        } catch (Exception e) {
            Timber.e(e, "Failed to parse");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backAction();
                finish();
                return true;
            case R.id.action_stop:
                showReasonPopup();
                return true;
            case R.id.action_send:
                submitForm();
                return true;
            case R.id.action_info:
                openExtraDataPopup();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReasonPopup() {
        runOnUiThread(() -> {
            new MaterialDialog.Builder(this)
                    .title(R.string.title_reason_to_stop)
                    .items(mFormHelper.getStopReasonsArray(mForm.getStopReasons()))
                    .itemsCallback((materialDialog, view, i, charSequence) -> cancelOrReschedule(mForm.getStopReasons().get(i)))
                    .negativeText(R.string.button_cancel)
                    .show();
        });
    }

    private void cancelOrReschedule(StopReason reason) {
        if (reason.isReschedule()) {
            rescheduleSurvey(reason);
        } else {
            cancelSurvey(reason);
        }
    }

    private void cancelSurvey(StopReason reason) {
        new MaterialDialog.Builder(this)
                .title(R.string.title_cancel_submission)
                .content(R.string.message_cancel_submission)
                .positiveText(R.string.button_yes)
                .negativeText(R.string.button_no)
                .autoDismiss(true)
                .onPositive((materialDialog, dialogAction) -> {
                    if (mSubmission != null) {
                        mHelper.removeSubmission(mForm.getId(), mSubmission.getId());
                    }
                    mHelper.addSubmissionForCancel(mForm.getId(),
                            mSubmissionHelper.extractAnswers(mTimestamp, mSections, mSubmission), reason);
                    finish();
                })
                .show();
    }

    private void rescheduleSurvey(StopReason reason) {
        if (mRescheduling) return;

        mRescheduling = true;

        mDate = DateTime.now();
        TimePickerDialog timeDialog = new TimePickerDialog(this,
                (view1, hourOfDay, minute) -> {
                    mDate = mDate.withHourOfDay(hourOfDay).withMinuteOfHour(minute);
                    new MaterialDialog.Builder(this)
                            .title(R.string.title_reschedule_submission)
                            .content(getString(R.string.message_reschedule_submission, DateUtils.getFullDateTimeInstanceWithoutSeconds().print(mDate)))
                            .positiveText(R.string.button_yes)
                            .negativeText(R.string.button_no)
                            .onPositive((materialDialog, dialogAction) -> {
                                if (mDate.isBeforeNow()) {
                                    showSnackBar(R.string.message_reschedule_date_is_before_now);
                                } else if (mDate.isAfter(new DateTime(mForm.getPubEnd()).plusDays(1).withTimeAtStartOfDay())) {
                                    showSnackBar(R.string.message_reschedule_date_is_after_pub_end);
                                } else {
                                    if (mSubmission != null) {
                                        mHelper.removeSubmission(mForm.getId(), mSubmission.getId());
                                    }
                                    mHelper.addSubmissionForReschedule(mForm.getId(),
                                            mSubmissionHelper.extractAnswers(mTimestamp, mSections, mSubmission)
                                                    .addLog(mDate, SubmissionLogAction.RESCHEDULED,
                                                            mPreferences.getUser().getId()), reason, mDate);
                                    finish();
                                }
                            })
                            .show();
                }, 0, 0, false);
        timeDialog.setOnDismissListener(dialog1 -> mRescheduling = false);
        timeDialog.setCancelable(true);

        final DatePickerDialog dialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            mDate = DateTime.now().withDate(year, monthOfYear + 1, dayOfMonth);

            timeDialog.show();

        }, mDate.getYear(), mDate.getMonthOfYear() - 1, mDate.getDayOfMonth());
        dialog.setOnCancelListener(dialog1 -> mRescheduling = false);
        dialog.setOnDismissListener(dialog1 -> {
            if (mRescheduling) timeDialog.show();
        });
        dialog.show();
    }

    private void openExtraDataPopup() {
        Intent intent = new Intent(this, ExtraDataActivity.class);
        intent.putExtra(BaseActivity.FORM_EXTRA, mForm);
        intent.putExtra(BaseActivity.SUBMISSION_EXTRA, mSubmission);
        intent.putExtra(BaseActivity.STARTED_EXTRA, DateUtils.formatShortDate(this, mTimestamp));
        startActivity(intent);
    }

    private void selectSection(int section) {
        if (section > mCurrentSection && mCurrentSection != 0 && !validateSection()) return;

        mCurrentSection = section;

        runOnUiThread(() -> {
            for (int i = 0; i < mSections.size(); i++) {
                mSections.get(i).setVisibility(View.INVISIBLE);
            }

            mSections.get(section - 1).setVisibility(View.VISIBLE);
        });

        mCurrentContainer = ButterKnife.findById(mSections.get(section - 1), R.id.scrollView);

        runOnUiThread(() -> {
            if (mSectionHelper.isFirst(mSections, section)) {
                mPrevious.setVisibility(View.INVISIBLE);
                mNext.setVisibility(View.VISIBLE);
            } else if (mSectionHelper.isLast(mSections, section)) {
                mPrevious.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.INVISIBLE);
            } else {
                mPrevious.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
            }
        });

        setSectionCounterText(mSectionHelper.getSectionNumber(mSections, section));
    }

    private boolean validateSection() {
        return mSectionValidator.validate(mSections.get(mCurrentSection - 1));
    }

    private void setUpForm() {
        Collections.sort(mForm.getSections(), (lhs, rhs) -> {
            Long l1 = lhs.getOrder() != null ? lhs.getOrder() : Long.MAX_VALUE;
            Long l2 = rhs.getOrder() != null ? rhs.getOrder() : Long.MAX_VALUE;
            return l1.compareTo(l2);
        });

        for (int i = 0; i < mForm.getSections().size(); i++) {
            setUpSection(mForm.getSections().get(i), i + 1);
        }
    }

    private void setUpSection(Section section, int sectionNumber) {
        if (section.getFields().isEmpty()) return;

        View sectionView = getLayoutInflater().inflate(R.layout.view_section_old, mContainer, false);
        sectionView.setTag(R.id.section_object, section);
        sectionView.setTag(R.id.section_visibility, Boolean.TRUE);
        TextView name = ButterKnife.findById(sectionView, R.id.section_name);
        TextView number = ButterKnife.findById(sectionView, R.id.section_number);
        ViewGroup container = ButterKnife.findById(sectionView, R.id.container);

        name.setText(section.getName());
        number.setText(String.valueOf(sectionNumber));

        Collections.sort(section.getFields(), (lhs, rhs) -> {
            Long l1 = lhs.getOrder() != null ? lhs.getOrder() : Long.MAX_VALUE;
            Long l2 = rhs.getOrder() != null ? rhs.getOrder() : Long.MAX_VALUE;
            return l1.compareTo(l2);
        });

        for (int i = 0, j = 1; i < section.getFields().size(); i++) {
            if (!section.getFields().get(i).isReadOnly()) {
                setUpField(container, section.getFields().get(i), j++, sectionNumber);
            }
        }

        mSections.add(sectionView);
        runOnUiThread(() -> mContainer.addView(sectionView));
    }

    private void setUpField(ViewGroup container, Field field, int fieldNumber, int sectionNumber) {
        View view;
        if (mSubmission == null) {
            view = mFieldHelper.inflate(this, container, field, fieldNumber, sectionNumber);
        } else {
            view = mFieldHelper.inflateFilled(this, container, field, fieldNumber, sectionNumber, mSubmission, false);
        }
        if (view != null) container.addView(view);
    }

    @OnClick(R.id.next)
    public void nextSection() {
        selectSection(mCurrentSection + 1);
    }

    @OnClick(R.id.previous)
    public void previousSection() {
        selectSection(mCurrentSection - 1);
    }

    public void onEvent(FieldActionEvent event) {
        switch (event.getType()) {
            case FieldActionEvent.DISABLE:
                changeFieldVisibility(event.getActions(), !event.isSelect());
                break;
            case FieldActionEvent.DISABLE_SECTION:
                changeSectionVisibility(event.getActions(), event.isSelect());
                break;
            case FieldActionEvent.ENABLE:
                changeFieldVisibility(event.getActions(), event.isSelect());
                break;
        }
    }

    public void onEventMainThread(FormUpdatedEvent event) {
        handleFieldActions();
    }

    private void handleFieldActions() {
        // Reset visibilities
        for (int i = 0; i < mSections.size(); i++) {
            View section = mSections.get(i);
            List<View> fields = mSectionHelper.getFieldsFromSection(section);
            for (View fieldView : fields) {
                fieldView.setVisibility(View.VISIBLE);
            }
        }

        List<Answer> answers = new ArrayList<>();
        for (int i = 0; i < mSections.size(); i++) {
            View section = mSections.get(i);
            List<View> fields = mSectionHelper.getFieldsFromSection(section);
            for (View fieldView : fields) {
                Answer value = mFieldHelper.extractValueForSubmission(fieldView);
                if (value != null) answers.add(value);
                Field field = (Field) fieldView.getTag();

                mFieldActionHelper.verifyAction(field, value);
            }
        }
        if (mEmptyAnswers == null) {
            if (mSubmission != null) {
                answers.addAll(0, mSubmission.getAnswers());
            }
            mEmptyAnswers = answers;
        }
    }

    private void changeSectionVisibility(List<Long> actions, boolean select) {
        for (View view : mSections) {
            Section section = (Section) view.getTag(R.id.section_object);
            if (actions.contains(section.getId())) {
                view.setTag(1, select);
            }
        }
        setSectionCounterText(mCurrentSection);
    }

    private void changeFieldVisibility(List<Long> actions, boolean select) {
        for (View view : mSections) {
            ViewGroup viewGroup = ButterKnife.findById(view, R.id.container);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                Field field = (Field) child.getTag();

                if (actions.contains(field.getId())) {
                    child.setVisibility(select ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    private void setSectionCounterText(int currentSection) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String current = String.valueOf(currentSection);
        String total = String.format("/%d", mSectionHelper.getTotalSections(mSections));

        runOnUiThread(() -> mNavigation.setVisibility(mSectionHelper.getTotalSections(mSections) == 1 ? View.GONE : View.VISIBLE));

        builder.append(current).append(total);
        runOnUiThread(() -> mSectionIndicator.setText(builder));
    }

    @Override
    public void onBackPressed() {
        backAction();
        super.onBackPressed();
    }

    public void onEventMainThread(ScrollEvent event) {
        mCurrentContainer.scrollTo(0, event.getPosition());
    }

    private void submitForm() {
        if (!mSectionHelper.validateSections(mSections)) {
            showSnackBar(getString(R.string.message_check_form_errors));
        } else {

            new MaterialDialog.Builder(this)
                    .title(R.string.title_send_submission)
                    .content(R.string.message_send_submission_disclaimer)
                    .onPositive((materialDialog, dialogAction) -> {
                        mHelper.addSubmission(mForm.getId(), mSubmissionHelper.extractAnswers(mTimestamp, mSections, mSubmission));
                        finish();
                    })
                    .positiveText(R.string.button_yes)
                    .negativeText(R.string.button_no)
                    .show();
        }
    }

    public void onEventMainThread(SyncCompletedEvent event) {
        dismissProgressDialog();
        finish();
    }
}
