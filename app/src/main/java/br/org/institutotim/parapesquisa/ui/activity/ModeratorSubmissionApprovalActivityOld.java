package br.org.institutotim.parapesquisa.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import br.org.institutotim.parapesquisa.data.event.AddCommentEvent;
import br.org.institutotim.parapesquisa.data.event.RemoveCommentEvent;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.helper.ModeratorFieldHelper;
import br.org.institutotim.parapesquisa.ui.helper.SectionHelper;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ModeratorSubmissionApprovalActivityOld extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.container)
    ViewGroup container;
    @Bind(R.id.section_indicator)
    TextView sectionIndicator;

    @Bind(R.id.next)
    View next;
    @Bind(R.id.previous)
    View previous;
    @Bind(R.id.navigation)
    View navigation;

    private List<SubmissionCorrection> corrections = new ArrayList<>();

    private UserSubmission submission;
    private FormData form;

    private List<View> sections = new ArrayList<>();

    private int currentSection;

    @Inject
    ParaPesquisaOpenHelper mHelper;
    @Inject
    SectionHelper mSectionHelper;
    @Inject
    ModeratorFieldHelper mModeratorFieldHelper;
    @Inject
    ParaPesquisaPreferences mPreferences;

    private UserData user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_submission_approval_old);
        ButterKnife.bind(this);
        getComponent().inject(this);

        user = mPreferences.getUser();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        submission = getIntent().getParcelableExtra(SUBMISSION_EXTRA);
        form = mHelper.getForm(getIntent().getLongExtra(FORM_EXTRA, 0));

        if (submission.getIdentifier() != null) {
            runOnUiThread(() -> getSupportActionBar().setTitle(submission.getIdentifier()));
        } else {
            runOnUiThread(() -> getSupportActionBar().setTitle(getString(R.string.text_submission) + String.format(" #%d", submission.getId())));
        }

        if (submission.getCorrections() != null) corrections = submission.getCorrections();

        showProgressDialog();
        new Thread(() -> {
            setUpForm();
            selectSection(1);
            dismissProgressDialog();
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpForm() {
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
        View view = mModeratorFieldHelper.inflateFilled(this, container, field, fieldNumber, sectionNumber, submission, true);
        if (view != null) container.addView(view);
    }

    private void selectSection(int section) {
        currentSection = section;

        runOnUiThread(() -> {
            for (int i = 0 ; i < sections.size(); i++) {
                sections.get(i).setVisibility(View.INVISIBLE);
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

        runOnUiThread(() -> navigation.setVisibility(mSectionHelper.getTotalSections(sections) == 1 ? View.GONE : View.VISIBLE));

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

    @OnClick(R.id.approve)
    public void approve() {
        new MaterialDialog.Builder(this)
                .title(R.string.title_approve_submission)
                .content(R.string.message_approve_submission_confirmation)
                .negativeText(R.string.button_no)
                .positiveText(R.string.button_yes)
                .autoDismiss(true)
                .onPositive((materialDialog, dialogAction) -> {
                    mHelper.addApprovedSubmission(submission);
                    mHelper.removeSubmission(submission.getFormId(), submission.getId());
                    finish();
                })
                .show();
    }

    @OnClick(R.id.reject)
    public void reject() {
        new MaterialDialog.Builder(this)
                .title(R.string.title_reject_submission)
                .content(R.string.message_reject_submission_confirmation)
                .negativeText(R.string.button_no)
                .positiveText(R.string.button_yes)
                .autoDismiss(true)
                .onPositive((materialDialog, dialogAction) -> {
                    mHelper.addRejectedSubmission(submission);
                    mHelper.removeSubmission(submission.getFormId(), submission.getId());
                    finish();
                })
                .show();
    }

    @OnClick(R.id.request_correction)
    public void requestCorrection() {
        new MaterialDialog.Builder(this)
                .title(corrections.isEmpty() ? R.string.title_warning : R.string.title_correction_requested)
                .content(corrections.isEmpty() ? getString(R.string.message_no_comments) : getString(R.string.message_correction_requested, submission.getOwner().getName()))
                .neutralText(R.string.button_ok)
                .autoDismiss(true)
                .onNeutral((materialDialog, dialogAction) -> {
                    if (!corrections.isEmpty()) {
                        submission = submission.setCorrections(corrections);
                        mHelper.removeSubmission(submission.getFormId(), submission.getId());
                        mHelper.addSubmission(submission.getFormId(), submission);
                        finish();
                    }
                })
                .show();
    }

    public void onEvent(AddCommentEvent event) {
        List<SubmissionCorrection> corrections = new ArrayList<>();
        for (SubmissionCorrection correction : this.corrections) {
            if (correction.getFieldId() != event.getField().getId()) corrections.add(correction);
        }
        corrections.add(SubmissionCorrection.builder()
                .createdAt(DateTime.now())
                .fieldId(event.getField().getId())
                .message(event.getComment())
                .userId(user.getId())
                .build());
        this.corrections = corrections;
    }

    public void onEvent(RemoveCommentEvent event) {
        List<SubmissionCorrection> corrections = new ArrayList<>();
        for (SubmissionCorrection correction : this.corrections) {
            if (correction.getFieldId() != event.getField().getId()) corrections.add(correction);
        }
        this.corrections = corrections;
    }
}
