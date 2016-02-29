package br.org.institutotim.parapesquisa.ui.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.joda.time.DateTime;

import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.SubmissionLogAction;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.adapter.SectionPagerAdapter;
import br.org.institutotim.parapesquisa.ui.helper.ModeratorFieldHelper;
import br.org.institutotim.parapesquisa.ui.helper.SectionHelper;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ModeratorSubmissionApprovalActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.container)
    ViewPager mContainer;
    @Bind(R.id.section_indicator)
    TextView sectionIndicator;

    @Bind(R.id.next)
    View next;
    @Bind(R.id.previous)
    View previous;
    @Bind(R.id.navigation)
    View navigation;

    private UserSubmission submission;
    private FormData form;

    @Inject
    ParaPesquisaOpenHelper mHelper;
    @Inject
    SectionHelper mSectionHelper;
    @Inject
    ModeratorFieldHelper mModeratorFieldHelper;
    @Inject
    ParaPesquisaPreferences mPreferences;

    private SectionPagerAdapter adapter;
    public static UserData user;

    private DateTime mTimestamp = DateTime.now();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_submission_approval);
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

        adapter = SectionPagerAdapter.builderSectionForModerator(form, submission);
        mContainer.setAdapter(adapter);
        mContainer.addOnPageChangeListener(this);
        previous.setVisibility(View.INVISIBLE);
        onPageSelected(0);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.next)
    public void nextSection() {
        mContainer.setCurrentItem(mContainer.getCurrentItem() + 1);
        if (mContainer.getCurrentItem() == mContainer.getAdapter().getCount() - 1) {
            next.setVisibility(View.INVISIBLE);
        }
        previous.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.previous)
    public void previousSection() {
        mContainer.setCurrentItem(mContainer.getCurrentItem() - 1);
        if (mContainer.getCurrentItem() == 0) {
            previous.setVisibility(View.INVISIBLE);
        }
        next.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        sectionIndicator.setText(String.format("%02d/%02d",
                ++position, mContainer.getAdapter().getCount()));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
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
                    mHelper.addApprovedSubmission(submission.addLog(mTimestamp, SubmissionLogAction.APPROVED, mPreferences.getUser().getId()));
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
                    mHelper.addRejectedSubmission(submission.addLog(mTimestamp, SubmissionLogAction.REPROVED, mPreferences.getUser().getId()));
                    mHelper.removeSubmission(submission.getFormId(), submission.getId());
                    finish();
                })
                .show();
    }

    @OnClick(R.id.request_correction)
    public void requestCorrection() {
        List<SubmissionCorrection> corrections = adapter.getCorrections(mContainer);

        new MaterialDialog.Builder(this)
                .title(corrections.isEmpty() ? R.string.title_warning : R.string.title_correction_requested)
                .content(corrections.isEmpty() ? getString(R.string.message_no_comments) : getString(R.string.message_correction_requested, submission.getOwner().getName()))
                .neutralText(R.string.button_ok)
                .autoDismiss(true)
                .onNeutral((materialDialog, dialogAction) -> {
                    if (!corrections.isEmpty()) {
                        submission = submission.setCorrections(corrections);
                        mHelper.removeSubmission(submission.getFormId(), submission.getId());
                        mHelper.addSubmission(submission.getFormId(), submission.addLog(mTimestamp, SubmissionLogAction.REVISED, mPreferences.getUser().getId()));
                        finish();
                    }
                })
                .show();
    }

}
