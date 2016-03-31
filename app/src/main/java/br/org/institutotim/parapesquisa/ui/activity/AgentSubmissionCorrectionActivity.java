package br.org.institutotim.parapesquisa.ui.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.event.RefreshFieldEvent;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.StopReason;
import br.org.institutotim.parapesquisa.data.model.SubmissionLogAction;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.adapter.SectionPagerAdapter;
import br.org.institutotim.parapesquisa.ui.helper.FormHelper;
import br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper;
import br.org.institutotim.parapesquisa.ui.helper.SubmissionHelper;
import br.org.institutotim.parapesquisa.util.DateUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

public class AgentSubmissionCorrectionActivity extends BaseSubmissionViewActivity implements AdapterView.OnItemSelectedListener, ViewPager.OnPageChangeListener {

    public static final int CORRECTIONS_READ_ONLY = 0;
    public static final int CORRECTIONS = 1;

    @IntDef({CORRECTIONS_READ_ONLY, CORRECTIONS})
    public @interface Correction {
    }

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.mode)
    Spinner mode;
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

    @Inject
    ParaPesquisaOpenHelper mHelper;
    @Inject
    FormHelper mFormHelper;
    @Inject
    SubmissionHelper mSubmissionHelper;
    @Inject
    ParaPesquisaPreferences mPreferences;

    private SectionPagerAdapter mAdapter;

    int menu = R.menu.form;
    @Correction
    public static int correctionType;
    public static UserData user;

    private boolean rescheduling = false;
    private DateTime date;

    private DateTime mTimestamp = DateTime.now();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_submission_correction);
        ButterKnife.bind(this);
        getComponent().inject(this);

        user = mPreferences.getUser();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSubmission = getIntent().getParcelableExtra(SUBMISSION_EXTRA);
        mForm = mHelper.getForm(mSubmission.getFormId());

        mAdapter = SectionPagerAdapter.builderSectionForCorrection(mForm, mSubmission);
        mContainer.addOnPageChangeListener(this);
        mPrevious.setVisibility(View.INVISIBLE);
        mode.setOnItemSelectedListener(this);
        mode.setSelection(1);

        handleReadOnlyStatus();

        List<Pair<Integer, Field>> fieldList = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < mForm.getSections().size(); i++) {
            final Section section = mForm.getSections().get(i);
            List<Field> mFields = section.getFields();
            for (int y = 0; y < mFields.size(); y++) {
                final Field field = mFields.get(y);
                if (RecyclerViewHelper.hasCorrection(field, mSubmission) && !getReadOnlyStatus(field.getId())) {
                    fieldList.add(new Pair<>(i, field));
                    fields.add(field.getLabel());
                }
            }
        }

        runOnUiThread(() -> {
            if (!fieldList.isEmpty()) {
                String[] fieldsArray = new String[fields.size()];
                fields.toArray(fieldsArray);
                new MaterialDialog.Builder(this)
                        .title(R.string.title_fields_with_peding_correction)
                        .items(fieldsArray)
                        .itemsCallback((materialDialog, view, i, charSequence) -> {
                            final Pair<Integer, Field> integerFieldPair = fieldList.get(i);
                            runnable = () -> mAdapter.scrollToField(mContainer, integerFieldPair.second);

                            if (mContainer.getCurrentItem() == integerFieldPair.first) {
                                runnable.run();
                                runnable = null;
                            } else {
                                mContainer.setCurrentItem(integerFieldPair.first);
                            }
                        })
                        .negativeText(R.string.button_cancel)
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(this.menu, menu);
        if (!mForm.hasExtraData()) {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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
                    mHelper.addSubmissionForCancel(mForm.getId(), mSubmission.addLog(mTimestamp, SubmissionLogAction.CANCELLED, mPreferences.getUser().getId()), reason);
                    mHelper.removeSubmission(mForm.getId(), mSubmission.getId());
                    finish();
                })
                .show();
    }

    private void rescheduleSurvey(StopReason reason) {
        if (rescheduling) return;

        rescheduling = true;

        date = DateTime.now();
        TimePickerDialog timeDialog = new TimePickerDialog(this,
                (view1, hourOfDay, minute) -> {
                    date = date.withHourOfDay(hourOfDay).withMinuteOfHour(minute);
                    new MaterialDialog.Builder(this)
                            .title(R.string.title_reschedule_submission)
                            .content(getString(R.string.message_reschedule_submission, DateUtils.getFullDateTimeInstanceWithoutSeconds().print(date)))
                            .positiveText(R.string.button_yes)
                            .negativeText(R.string.button_no)
                            .autoDismiss(true)
                            .onPositive((materialDialog, dialogAction) -> {
                                if (date.isBeforeNow()) {
                                    showSnackBar(R.string.message_reschedule_date_is_before_now);
                                } else if (date.isAfter(new DateTime(mForm.getPubEnd()).plusDays(1).withTimeAtStartOfDay())) {
                                    showSnackBar(R.string.message_reschedule_date_is_after_pub_end);
                                } else {
                                    mHelper.addSubmissionForReschedule(mForm.getId(), mSubmission.addLog(date,
                                            SubmissionLogAction.RESCHEDULED, mPreferences.getUser().getId()), reason, date);
                                    mHelper.removeSubmission(mForm.getId(), mSubmission.getId());
                                    finish();
                                }
                            })
                            .show();
                }, 0, 0, false);
        timeDialog.setOnDismissListener(dialog1 -> rescheduling = false);
        timeDialog.setCancelable(true);

        final DatePickerDialog dialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            date = DateTime.now().withDate(year, monthOfYear + 1, dayOfMonth);

            timeDialog.show();

        }, date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
        dialog.setOnCancelListener(dialog1 -> rescheduling = false);
        dialog.setOnDismissListener(dialog1 -> {
            if (rescheduling) timeDialog.show();
        });
        dialog.show();
    }

    private void submitForm() {
        if (!mAdapter.isValidSections(mContainer)) {
            showSnackBar(getString(R.string.message_check_form_errors));
        } else {

            new MaterialDialog.Builder(this)
                    .title(R.string.title_send_submission)
                    .content(R.string.message_send_submission_disclaimer)
                    .autoDismiss(true)
                    .onPositive((materialDialog, dialogAction) -> {
                        mSubmission = mSubmission.removeStatus();
                        mHelper.removeSubmission(mSubmission.getFormId(), mSubmission.getId());
                        mHelper.addSubmission(mSubmission.getFormId(), mSubmissionHelper.updateAnswersBySections(mAdapter.getAnswers(mContainer), mSubmission.addLog(mTimestamp, SubmissionLogAction.REVISED, mPreferences.getUser().getId())));
                        finish();
                    })
                    .positiveText(R.string.button_yes)
                    .negativeText(R.string.button_no)
                    .show();
        }
    }

    private void openExtraDataPopup() {
        Intent intent = new Intent(this, ExtraDataActivity.class);
        intent.putExtra(ExtraDataActivity.FORM_EXTRA, mForm);
        intent.putExtra(ExtraDataActivity.SUBMISSION_EXTRA, mSubmission);
        intent.putExtra(ExtraDataActivity.STARTED_EXTRA, DateUtils.formatShortDate(this, mSubmission.getStartTime()));
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            correctionType = CORRECTIONS_READ_ONLY;
            menu = R.menu.form_extra_only;
        } else {
            correctionType = CORRECTIONS;
            menu = R.menu.form;
        }
        supportInvalidateOptionsMenu();

        if (mContainer.getAdapter() == null) {
            mContainer.setAdapter(mAdapter);
            onPageSelected(0);
        } else {
            EventBus.getDefault().post(new RefreshFieldEvent(correctionType));
        }
//        setUpForm();
//        selectSection(1);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @OnClick(R.id.next)
    public void nextSection() {
        if (!mAdapter.validateCurrentPage(mContainer)) {
            return;
        }

        mContainer.setCurrentItem(mContainer.getCurrentItem() + 1);
    }

    @OnClick(R.id.previous)
    public void previousSection() {
        mContainer.setCurrentItem(mContainer.getCurrentItem() - 1);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void onPageSelected(int position) {
        if (runnable != null) runnable.run();
        runnable = null;
        mSectionIndicator.setText(String.format("%02d/%02d",
                ++position, mContainer.getAdapter().getCount()));

        mPrevious.setVisibility(View.VISIBLE);
        mNext.setVisibility(View.VISIBLE);

        if (mContainer.getCurrentItem() == mContainer.getAdapter().getCount() - 1) {
            mNext.setVisibility(View.INVISIBLE);
        }

        if (mContainer.getCurrentItem() == 0) {
            mPrevious.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
