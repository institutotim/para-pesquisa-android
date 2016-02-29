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
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.StopReason;
import br.org.institutotim.parapesquisa.data.model.SubmissionLogAction;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.helper.FieldHelper;
import br.org.institutotim.parapesquisa.ui.helper.FormHelper;
import br.org.institutotim.parapesquisa.ui.helper.SectionHelper;
import br.org.institutotim.parapesquisa.ui.helper.SubmissionHelper;
import br.org.institutotim.parapesquisa.util.DateUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AgentSubmissionCorrectionActivityOld extends BaseActivity implements AdapterView.OnItemSelectedListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.mode)
    Spinner mode;
    @Bind(R.id.container)
    ViewGroup container;
    @Bind(R.id.navigation)
    View navigation;
    @Bind(R.id.next)
    View next;
    @Bind(R.id.previous)
    View previous;
    @Bind(R.id.section_indicator)
    TextView sectionIndicator;

    boolean readOnly = true;
    int menu = R.menu.form_extra_only;

    private UserSubmission submission;
    private FormData form;

    private int currentSection;
    private DateTime date;
    private boolean rescheduling = false;

    private List<View> sections = new ArrayList<>();

    @Inject
    ParaPesquisaOpenHelper mHelper;
    @Inject
    SectionHelper mSectionHelper;
    @Inject
    FieldHelper mFieldHelper;
    @Inject
    FormHelper mFormHelper;
    @Inject
    SubmissionHelper mSubmissionHelper;
    @Inject
    ParaPesquisaPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_submission_correction_old);
        ButterKnife.bind(this);
        getComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        submission = getIntent().getParcelableExtra(SUBMISSION_EXTRA);
        form = mHelper.getForm(submission.getFormId());

        showProgressDialog();
        new Thread(() -> {
            setUpForm();

            selectSection(1);

            mode.setOnItemSelectedListener(this);
            runOnUiThread(() -> mode.setSelection(1));
            dismissProgressDialog();
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(this.menu, menu);
        if (!form.hasExtraData()) {
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

    private void submitForm() {
        if (!mSectionHelper.validateSections(sections)) {
            showSnackBar(getString(R.string.message_check_form_errors));
        } else {

            new MaterialDialog.Builder(this)
                    .title(R.string.title_send_submission)
                    .content(R.string.message_send_submission_disclaimer)
                    .autoDismiss(true)
                    .onPositive((materialDialog, dialogAction) -> {
                        submission = submission.removeStatus();
                        mHelper.removeSubmission(submission.getFormId(), submission.getId());
                        mHelper.addSubmission(submission.getFormId(), mSubmissionHelper.updateAnswers(sections, submission));
                        finish();
                    })
                    .positiveText(R.string.button_yes)
                    .negativeText(R.string.button_no)
                    .show();
        }
    }

    private void openExtraDataPopup() {
        Intent intent = new Intent(this, ExtraDataActivity.class);
        intent.putExtra(ExtraDataActivity.FORM_EXTRA, form);
        intent.putExtra(ExtraDataActivity.SUBMISSION_EXTRA, submission);
        intent.putExtra(ExtraDataActivity.STARTED_EXTRA, DateUtils.formatShortDate(this, submission.getStartTime()));
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            readOnly = true;
            menu = R.menu.form_extra_only;
        } else {
            readOnly = false;
            menu = R.menu.form;
        }
        supportInvalidateOptionsMenu();
        setUpForm();
        selectSection(1);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void setUpForm() {
        container.removeAllViews();
        sections.clear();
        for (int i = 0; i < form.getSections().size(); i++) {
            setUpSection(form.getSections().get(i), i + 1);
        }
    }

    private void setUpSection(Section section, int sectionNumber) {
        View sectionView = getLayoutInflater().inflate(R.layout.view_section_old, container, false);
        sectionView.setTag(R.id.section_object, section);
        sectionView.setTag(R.id.section_visibility, Boolean.TRUE);
        TextView name = ButterKnife.findById(sectionView, R.id.section_name);
        TextView number = ButterKnife.findById(sectionView, R.id.section_number);
        ViewGroup container = ButterKnife.findById(sectionView, R.id.container);

        name.setText(section.getName());
        number.setText(String.valueOf(sectionNumber));

        Collections.sort(section.getFields(), (lhs, rhs) -> {
            if (lhs.getOrder() == null || rhs.getOrder() == null) return 0;
            if (lhs.getOrder() < rhs.getOrder()) return -1;
            if (lhs.getOrder() > rhs.getOrder()) return 1;
            return 0;
        });

        for (int i = 0; i < section.getFields().size(); i++) {
            setUpField(container, section.getFields().get(i), i + 1, sectionNumber);
        }

        if (container.getChildCount() == 0) return;

        sections.add(sectionView);
        runOnUiThread(() -> this.container.addView(sectionView));
    }

    private void setUpField(ViewGroup container, Field field, int fieldNumber, int sectionNumber) {
        View view;
        if (readOnly) {
            view = mFieldHelper.inflateFilledReadOnlyWithCorrection(this, container, field, fieldNumber, sectionNumber, submission);
        } else {
            view = mFieldHelper.inflateFilledForCorrection(this, container, field, fieldNumber, sectionNumber, submission);
        }
        if (view != null) container.addView(view);
    }

    private void selectSection(int section) {
        currentSection = section;

        runOnUiThread(() -> {
            for (View view : sections) {
                view.setVisibility(View.INVISIBLE);
            }
        });

        if (sections.size() == 0) return;

        runOnUiThread(() -> {
            sections.get(section - 1).setVisibility(View.VISIBLE);

            if (mSectionHelper.isFirst(sections, section)) {
                previous.setVisibility(View.INVISIBLE);
                next.setVisibility(View.VISIBLE);
            } else if (mSectionHelper.isLast(sections, section)) {
                previous.setVisibility(View.VISIBLE);
                next.setVisibility(View.INVISIBLE);
            } else {
                previous.setVisibility(View.VISIBLE);
                next.setVisibility(View.VISIBLE);
            }
        });

        setSectionCounterText(mSectionHelper.getSectionNumber(sections, section));
    }

    private void setSectionCounterText(int currentSection) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String current = String.valueOf(currentSection);
        String total = String.format("/%d", mSectionHelper.getTotalSections(sections));

        navigation.setVisibility(mSectionHelper.getTotalSections(sections) == 1 ? View.GONE : View.VISIBLE);

        builder.append(current).append(total);
        runOnUiThread(() -> sectionIndicator.setText(builder));
    }

    @OnClick(R.id.next)
    public void nextSection() {
        selectSection(currentSection + 1);
    }

    @OnClick(R.id.previous)
    public void previousSection() {
        selectSection(currentSection - 1);
    }

    private void showReasonPopup() {
        new MaterialDialog.Builder(this)
                .title(R.string.title_reason_to_stop)
                .items(mFormHelper.getStopReasonsArray(form.getStopReasons()))
                .itemsCallback((materialDialog, view, i, charSequence) -> cancelOrReschedule(form.getStopReasons().get(i)))
                .negativeText(R.string.button_cancel)
                .show();
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
                    mHelper.addSubmissionForCancel(form.getId(), submission, reason);
                    mHelper.removeSubmission(form.getId(), submission.getId());
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
                                } else if (date.isAfter(new DateTime(form.getPubEnd()).plusDays(1).withTimeAtStartOfDay())) {
                                    showSnackBar(R.string.message_reschedule_date_is_after_pub_end);
                                } else {
                                    mHelper.addSubmissionForReschedule(form.getId(), submission.addLog(date,
                                            SubmissionLogAction.RESCHEDULED, mPreferences.getUser().getId()), reason, date);
                                    mHelper.removeSubmission(form.getId(), submission.getId());
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
}
