package br.org.institutotim.parapesquisa.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.event.ContinueSurveyEvent;
import br.org.institutotim.parapesquisa.data.event.OpenSubmissionEvent;
import br.org.institutotim.parapesquisa.data.event.SyncCompletedEvent;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.UserForm;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.service.AgentUpdateService;
import br.org.institutotim.parapesquisa.ui.adapter.SubmissionsPagerAdapter;
import br.org.institutotim.parapesquisa.ui.helper.NotificationHelper;
import br.org.institutotim.parapesquisa.ui.helper.SubmissionHelper;
import br.org.institutotim.parapesquisa.util.DateUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AgentSubmissionsActivity extends BaseActivity implements View.OnClickListener, SearchView.OnCloseListener, SearchView.OnQueryTextListener {

    public static final String USER_FORM = "br.org.institutotim.parapesquisa.user_form";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.tab_layout)
    TabLayout mTabLayout;
    @Bind(R.id.view_pager)
    ViewPager mViewPager;

    @Bind(R.id.sync_image)
    View mSyncImage;
    @Bind(R.id.sync_indicator)
    View mSyncIndicator;

    @Bind(R.id.pending_submission)
    View mPendingSubmission;
    @Bind(R.id.empty_message)
    TextView mEmptyMessage;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.remaining_surveys)
    TextView mRemainingSurveys;

    @Bind(R.id.notifications)
    RecyclerView mNotifications;
    @Bind(R.id.clear_notifications)
    Button mClearNotifications;
    @Bind(R.id.notification_message)
    TextView mMessage;

    private TextView mNotificationCounter;

    UserForm mForm;

    @Inject
    ParaPesquisaOpenHelper mHelper;
    @Inject
    SubmissionHelper mSubmissionHelper;
    @Inject
    NotificationHelper mNotificationHelper;

    private boolean syncing = false;

    private boolean searching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_submissions);
        ButterKnife.bind(this);
        getComponent().inject(this);

        mForm = getIntent().getParcelableExtra(USER_FORM);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            actionBar.setTitle(mForm.getForm().getName());
            actionBar.setSubtitle(mForm.getForm().getSubtitleAndPubDate(this));
        }

        mNotifications.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        showProgressDialog();
        new Thread(() -> {
            setupData();

            runOnUiThread(() -> mPendingSubmission.setVisibility(mHelper.hasSubmissionInProgress(mForm.getFormId()) ?
                    View.VISIBLE : View.GONE));
            dismissProgressDialog();
        }).start();
    }

    void setupData() {
        setupData(null);
    }

    void setupData(String query) {
        runOnUiThread(() -> {
            mViewPager.removeAllViews();
            mViewPager.setAdapter(new SubmissionsPagerAdapter(getApplicationContext(),
                    getSupportFragmentManager(), mForm.getFormId(), query));
            mTabLayout.setupWithViewPager(mViewPager);
        });

        setRemainingSurveysLabel();
    }

    private void setRemainingSurveysLabel() {
        long size = mHelper.getRemainingSurveys(mForm.getFormId());

        String remaining = String.valueOf(size);
        String total = getString(R.string.text_remaining_surveys, mForm.getQuota());

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(remaining).append(total);
        runOnUiThread(() -> mRemainingSurveys.setText(builder));

        if (mHelper.getAllSubmissionsCount(mForm.getFormId()) == 0) {
            runOnUiThread(() -> {
                mTabLayout.setVisibility(View.GONE);
                mEmptyMessage.setVisibility(View.VISIBLE);
            });
        } else {
            runOnUiThread(() -> {
                mTabLayout.setVisibility(View.VISIBLE);
                mEmptyMessage.setVisibility(View.GONE);
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!searching) {
            getMenuInflater().inflate(R.menu.notifications, menu);
            View menuItem = menu.findItem(R.id.action_notifications).getActionView();
            mNotificationCounter = (TextView) menuItem.findViewById(R.id.counter);
            menuItem.setOnClickListener(this);

            mNotificationHelper.setUpNotifications(mNotificationCounter, mNotifications,
                    mClearNotifications, mMessage);
        } else {
            getMenuInflater().inflate(R.menu.search, menu);

            MenuItem searchItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);
            searchView.setQueryHint(getString(R.string.message_search_surveys));
            searchView.setIconified(false);
        }
        return true;
    }

    @OnClick(R.id.new_survey)
    public void newSurvey() {
        if (isSubmissionInProgress()) {
            return;
        }

        if (isOverQuota()) {
            showSnackBar(R.string.message_submission_quota_excedeed);
        } else if (mForm.getForm().getPubEnd() != null && mForm.getForm().getPubEnd().plusDays(1).isBeforeNow()) {
            showSnackBar(R.string.message_expired_form);
        } else if (mForm.getForm().getPubStart() != null && mForm.getForm().getPubStart().isAfterNow()) {
            showSnackBar(getString(R.string.message_not_started_form, DateUtils.formatShortDate(this, mForm.getForm().getPubStart())));
        } else {
            Intent intent = new Intent(this, AgentFormActivity.class);
            intent.putExtra(BaseActivity.FORM_EXTRA, mForm.getForm());

            if (mHelper.hasExtraData(mForm.getFormId())) {
                List<UserSubmission> options = mHelper.getSubmissions(mForm.getFormId(), SubmissionStatus.NEW);
                List<String> list = mSubmissionHelper.getList(mForm.getForm(), options);

                if (list != null && !list.isEmpty()) {
                    mSubmissionHelper.showExtraDataPicker(this, list, (materialDialog, view, i, charSequence) -> {
                        UserSubmission submission = options.get(i);
                        intent.putExtra(BaseActivity.SUBMISSION_EXTRA, submission);
                        startActivityForResult(intent, FORM_RESULT);
                    });
                } else {
                    startActivityForResult(intent, FORM_RESULT);
                }
            } else {
                startActivityForResult(intent, FORM_RESULT);
            }
        }
    }

    private boolean isSubmissionInProgress() {
        final boolean exists = mHelper.hasSubmissionInProgress(mForm.getForm().getId());

        if (exists) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_warning)
                    .setMessage(R.string.message_survey_in_progress)
                    .setNeutralButton(R.string.button_ok, null)
                    .show();
        }

        return exists;
    }

    private boolean isOverQuota() {
        return mHelper.getRemainingSurveys(mForm.getFormId()) <= 0;
    }

    @OnClick(R.id.start_sync)
    public void startSync() {
        if (!syncing) {
            showProgressDialog();
            syncing = true;
            mSyncImage.setVisibility(View.GONE);
            mSyncIndicator.setVisibility(View.VISIBLE);
            startService(new Intent(this, AgentUpdateService.class));
        }
    }

    public void onEventMainThread(SyncCompletedEvent event) {
        dismissProgressDialog();
        mSyncImage.setVisibility(View.VISIBLE);
        mSyncIndicator.setVisibility(View.GONE);
        syncing = false;
        switch (event.getResult()) {
            case SUCCESS:
                mForm = mHelper.getUserFormById(mForm.getId());
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(mForm.getForm().getName());
                    actionBar.setSubtitle(mForm.getForm().getSubtitleAndPubDate(this));
                }

                setupData();
                showSnackBar(R.string.message_sync_completed);
                mNotificationHelper.setUpNotifications(mNotificationCounter, mNotifications,
                        mClearNotifications, mMessage);
                break;
            case ERROR:
                showSnackBar(event.getMessage());
                break;
        }
    }

    private void openSubmission(final UserSubmission submission) {
        Intent intent = new Intent(this, AgentFormActivity.class);
        intent.putExtra(BaseActivity.FORM_EXTRA, mForm.getForm());
        intent.putExtra(BaseActivity.SUBMISSION_EXTRA, submission);
        startActivityForResult(intent, FORM_RESULT);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContinueSurveyEvent event) {
        continueSurvey();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(OpenSubmissionEvent event) {
        if (isSubmissionInProgress()
                || event == null
                || event.getSubmission() == null
                || event.getSubmission().getFormId() == null)
            return;
        if (event.getSubmission().getFormId().equals(mForm.getFormId()))
            openSubmission(event.getSubmission());
    }

    @OnClick(R.id.continue_survey)
    public void continueSurvey() {
        openSubmission(mHelper.getSubmissionInProgress(mForm.getFormId()));
    }

    @OnClick(R.id.reschedule_submission)
    public void rescheduleSubmission() {
        Intent intent = new Intent(this, AgentFormActivity.class);
        intent.putExtra(BaseActivity.FORM_EXTRA, mForm.getForm());
        intent.putExtra(BaseActivity.SUBMISSION_EXTRA, mHelper.getSubmissionInProgress(mForm.getFormId()));
        intent.putExtra(BaseActivity.RESCHEDULE_EXTRA, true);
        startActivityForResult(intent, FORM_RESULT);
    }

    @Override
    public void onClick(View v) {
        mDrawerLayout.openDrawer(GravityCompat.END);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @OnClick(R.id.search)
    public void search() {
        searching = true;
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onClose() {
        searching = false;
        supportInvalidateOptionsMenu();
        setupData();
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNotificationCounter != null) {
            mNotificationHelper.setUpNotifications(mNotificationCounter, mNotifications,
                    mClearNotifications, mMessage);
        }
    }

    @OnClick(R.id.clear_notifications)
    public void clearNotifications() {
        mNotificationCounter.setVisibility(View.GONE);
        mNotifications.setVisibility(View.GONE);
        mClearNotifications.setVisibility(View.GONE);
        mMessage.setVisibility(View.VISIBLE);
        mHelper.clearNotifications();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        setupData(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }
}
