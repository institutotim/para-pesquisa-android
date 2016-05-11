package br.org.institutotim.parapesquisa.ui.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FieldOption;
import br.org.institutotim.parapesquisa.data.model.FieldType;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.StopReason;
import br.org.institutotim.parapesquisa.data.model.SubmissionLogAction;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.adapter.SectionPagerAdapter;
import br.org.institutotim.parapesquisa.ui.helper.FormHelper;
import br.org.institutotim.parapesquisa.ui.helper.SubmissionHelper;
import br.org.institutotim.parapesquisa.util.DateUtils;
import br.org.institutotim.parapesquisa.util.StringUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AgentFormActivity extends BaseSubmissionViewActivity implements ViewPager.OnPageChangeListener {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.container)
    ViewPager mContainer;
    @Bind(R.id.section_indicator)
    TextView mSectionIndicator;

    @Bind(R.id.next)
    View mNext;
    @Bind(R.id.previous)
    View mPrevious;
    @Bind(R.id.navigation)
    View mNavigation;

    private SectionPagerAdapter mAdapter;

    @Inject
    ParaPesquisaOpenHelper mHelper;

    @Inject
    FormHelper mFormHelper;
    @Inject
    SubmissionHelper mSubmissionHelper;
    @Inject
    ObjectMapper mObjectMapper;
    @Inject
    ParaPesquisaPreferences mPreferences;

    private Date mTimestamp = new Date();

    private DateTime mDate;
    private boolean mRescheduling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_view);
        ButterKnife.bind(this);

        getComponent().inject(this);

        mSubmission = getIntent().getParcelableExtra(BaseActivity.SUBMISSION_EXTRA);
        mForm = getIntent().getParcelableExtra(BaseActivity.FORM_EXTRA);

		if (mForm != null) {
		    mHelper.removeSubmissionInProgress(mForm.getId());
	    }

        if (mSubmission != null) {
            mHelper.removePendingSubmission(mSubmission);
        }

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            String identifier = mHelper.getIdentifier(mSubmission);
            if (identifier != null) {
                actionBar.setTitle(identifier);
            } else if (mSubmission != null) {
                getSupportActionBar().setTitle(getString(R.string.text_submission) + String.format(" #%d",
                        mSubmission.getId()));
            }
        }

        mAdapter = SectionPagerAdapter.builderSectionPagerDefault(mForm, mSubmission);
        mContainer.setAdapter(mAdapter);
        mContainer.addOnPageChangeListener(this);
        mPrevious.setVisibility(View.INVISIBLE);
        mHelper.removeSubmissionInProgress(mForm.getId());

        handleReadOnlyStatus();

        onPageSelected(0);

        if (getIntent().getBooleanExtra(RESCHEDULE_EXTRA, false)) {
            showReasonPopup();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.form, menu);

        if (!mForm.hasExtraData()) {
            menu.getItem(0).setVisible(false);
        }
        return true;
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

    private void submitForm() {
        if (!mAdapter.isValidSections(mContainer)) {
            showSnackBar(getString(R.string.message_check_form_errors));
        } else {

            new MaterialDialog.Builder(this)
                    .title(R.string.title_send_submission)
                    .content(R.string.message_send_submission_disclaimer)
                    .onPositive((materialDialog, dialogAction) -> {
                        mHelper.addSubmission(mForm.getId(), mSubmissionHelper.extractAnswersBySections(mTimestamp, mAdapter.getAnswers(mContainer), mSubmission));
                        mHelper.removeSubmissionInProgress(mForm.getId());
                        finish();
                    })
                    .positiveText(R.string.button_yes)
                    .negativeText(R.string.button_no)
                    .show();
        }
    }

    private void openExtraDataPopup() {
        Intent intent = new Intent(this, ExtraDataActivity.class);
        intent.putExtra(BaseActivity.FORM_EXTRA, mForm);
        intent.putExtra(BaseActivity.SUBMISSION_EXTRA, mSubmission);
        intent.putExtra(BaseActivity.STARTED_EXTRA, DateUtils.formatShortDate(this, mTimestamp));
        startActivity(intent);
    }

    public void backAction() {
        final List<Answer> answers = mAdapter.getAnswers(mContainer);
        if (answers != null && !answers.isEmpty() && shouldPersist(answers)) {
            UserSubmission submission = mSubmissionHelper.extractAnswersBySections(mTimestamp, answers, mSubmission,
                    mContainer.getCurrentItem());
            if (mSubmission != null && mSubmission.getId() != null) {
                mHelper.removeSubmission(mForm.getId(), mSubmission.getId());
            }
            mHelper.setSubmissionInProgress(mForm.getId(), submission);
        }
    }

    private boolean shouldPersist(List<Answer> answers) {
        for (int i = 0; i < answers.size(); i++) {
            final Answer answer = answers.get(i);
            if (answer == null) {
                continue;
            }
            final String[] split = StringUtils.split(answer.getValues(), "\\\\");
            if (split.length > 1)
                return true;

            final Field field = getFieldByAnswer(answer);
            if (field != null) {
                if (!FieldType.SELECT.equals(field.getType())) {
                    return true;
                } else {
                    final List<FieldOption> options = field.getOptions();
                    if (options != null && !options.isEmpty()) {
                        final FieldOption fieldOption = options.get(0);
                        if (!split[0].equals(fieldOption.getValue()))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    private Field getFieldByAnswer(Answer answer) {
        for (int i = 0; i < mForm.getSections().size(); i++) {
            final Section section = mForm.getSections().get(i);
            for (int y = 0; y < section.getFields().size(); y++) {
                final Field field = section.getFields().get(y);
                if (answer.getFieldId() == field.getId())
                    return field;
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        backAction();
        super.onBackPressed();
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
                    mHelper.removeSubmissionInProgress(mForm.getId());
                    mHelper.addSubmissionForCancel(mForm.getId(),
                            mSubmissionHelper.extractAnswersBySections(mTimestamp, mAdapter.getAnswers(mContainer), mSubmission), reason);
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
                                    mHelper.removeSubmissionInProgress(mForm.getId());
                                    mHelper.addSubmissionForReschedule(mForm.getId(),
                                            mSubmissionHelper.extractAnswersBySections(mTimestamp, mAdapter.getAnswers(mContainer), mSubmission)
                                                    .addLog(mDate, SubmissionLogAction.RESCHEDULED,
                                                            mPreferences.getUser().getId()), reason, mDate);
                                    finish();
                                }
                            })
                            .show();
                }, mDate.getHourOfDay(), mDate.getMinuteOfHour(), false);
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

    @OnClick(R.id.next)
    public void nextSection() {
        if (!mAdapter.validateCurrentPage(mContainer)) {
            return;
        }
        backAction();
        mContainer.setCurrentItem(mContainer.getCurrentItem() + 1);
        if (mContainer.getCurrentItem() == mContainer.getAdapter().getCount() - 1) {
            mNext.setVisibility(View.INVISIBLE);
        }
        mPrevious.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.previous)
    public void previousSection() {
        mContainer.setCurrentItem(mContainer.getCurrentItem() - 1);
        if (mContainer.getCurrentItem() == 0) {
            mPrevious.setVisibility(View.INVISIBLE);
        }
        mNext.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mSectionIndicator.setText(String.format("%02d/%02d",
                ++position, mContainer.getAdapter().getCount()));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }



}