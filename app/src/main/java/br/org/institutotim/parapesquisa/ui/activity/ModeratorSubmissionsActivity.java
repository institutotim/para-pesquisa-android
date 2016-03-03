package br.org.institutotim.parapesquisa.ui.activity;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.event.SyncCompletedEvent;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserForm;
import br.org.institutotim.parapesquisa.service.ModeratorUpdateService;
import br.org.institutotim.parapesquisa.ui.adapter.DestinationTransferSubmissionAdapter;
import br.org.institutotim.parapesquisa.ui.adapter.SubmissionsPagerAdapter;
import br.org.institutotim.parapesquisa.ui.adapter.SurveyTakerAdapter;
import br.org.institutotim.parapesquisa.ui.adapter.TransferSubmissionAdapter;
import br.org.institutotim.parapesquisa.ui.adapter.UserSubmissionsTransferAdapter;
import br.org.institutotim.parapesquisa.ui.helper.ModeratorHelper;
import br.org.institutotim.parapesquisa.ui.helper.NotificationHelper;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ModeratorSubmissionsActivity extends BaseActivity implements View.OnClickListener, SearchView.OnCloseListener, SearchView.OnQueryTextListener {

    public static final String USER_FORM = "br.org.institutotim.parapesquisa.user_form";

    @Bind(R.id.tabs)
    TabLayout tabs;
    @Bind(R.id.pager)
    ViewPager pager;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.sync_image)
    View syncImage;
    @Bind(R.id.sync_indicator)
    View syncIndicator;

    @Bind(R.id.remaining_surveys)
    TextView surveyTakers;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.notifications)
    RecyclerView notifications;
    @Bind(R.id.clear_notifications)
    Button clearNotifications;
    @Bind(R.id.notification_message)
    TextView message;
    private TextView notificationCounter;

    @Bind(R.id.transfer_submissions)
    View transferSubmissionsButton;

    private UserForm form;
    private boolean syncing = false;

    private boolean searching = false;

    @Inject
    ParaPesquisaOpenHelper mHelper;
    @Inject
    NotificationHelper mNotificationHelper;
    @Inject
    ModeratorHelper mModeratorHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_submissions);
        ButterKnife.bind(this);
        getComponent().inject(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        form = getIntent().getParcelableExtra(USER_FORM);
        getSupportActionBar().setTitle(form.getForm().getName());
        getSupportActionBar().setSubtitle(form.getForm().getSubtitleAndPubDate(this));

        notifications.setLayoutManager(new LinearLayoutManager(this));

        transferSubmissionsButton.setVisibility(form.getForm().isAllowTransfer() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        showProgressDialog();
        new Thread(() -> {
            setUpData();
            dismissProgressDialog();
        }).start();
    }

    private void setUpData() {
        setUpData(null);
    }

    private void setUpData(String query) {
        runOnUiThread(() -> {
            pager.removeAllViews();
            pager.setAdapter(new SubmissionsPagerAdapter(this, getSupportFragmentManager(), form.getFormId(), query));
            tabs.setupWithViewPager(pager);
        });

        setSurveyTakersLabel();
    }

    private void setUpDataByUser(UserData user) {
        if (user == null) {
            setUpData();
        } else {
            pager.removeAllViews();
            pager.setAdapter(new SubmissionsPagerAdapter(this, getSupportFragmentManager(), form.getFormId(), user));
            tabs.setupWithViewPager(pager);

            surveyTakers.setText(getString(R.string.text_surveys_by, user.getName()));
        }
    }

    private void setSurveyTakersLabel() {
        runOnUiThread(() -> surveyTakers.setText(R.string.text_all_survey_takers));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!searching) {
            getMenuInflater().inflate(R.menu.notifications, menu);
            View menuItem = menu.findItem(R.id.action_notifications).getActionView();
            notificationCounter = (TextView) menuItem.findViewById(R.id.counter);
            menuItem.setOnClickListener(this);

            mNotificationHelper.setUpNotifications(notificationCounter, notifications,
                    clearNotifications, message);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.start_sync)
    public void startSync() {
        if (!syncing) {
            showProgressDialog();
            syncing = true;
            syncImage.setVisibility(View.GONE);
            syncIndicator.setVisibility(View.VISIBLE);
            startService(new Intent(this, ModeratorUpdateService.class));
        }
    }

    @OnClick(R.id.filter_user)
    public void filterUser() {
        new MaterialDialog.Builder(this)
                .title(R.string.title_select_survey_taker)
                .negativeText(R.string.button_cancel)
                .adapter(new SurveyTakerAdapter(this, mHelper.getUsers(form.getFormId())), (materialDialog, view, i, charSequence) -> {
                    UserData user = (UserData) materialDialog.getListView().getAdapter().getItem(i);
                    setUpDataByUser(user);
                    materialDialog.dismiss();
                })
                .show();
    }

    @OnClick(R.id.report)
    public void showReport() {
        Intent intent = new Intent(this, FormReportActivity.class);
        intent.putExtra(FormReportActivity.FORM_ID_EXTRA, form.getFormId());
        startActivity(intent);
    }

    @OnClick(R.id.search)
    public void search() {
        searching = true;
        supportInvalidateOptionsMenu();
    }

    public void onEventMainThread(SyncCompletedEvent event) {
        dismissProgressDialog();
        syncImage.setVisibility(View.VISIBLE);
        syncIndicator.setVisibility(View.GONE);
        syncing = false;
        switch (event.getResult()) {
            case SUCCESS:
                setUpData();
                showSnackBar(getString(R.string.message_sync_completed));
                mNotificationHelper.setUpNotifications(notificationCounter, notifications,
                        clearNotifications, message);
                break;
            case ERROR:
                showSnackBar(event.getMessage());
                break;
        }
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

    @Override
    public boolean onClose() {
        searching = false;
        supportInvalidateOptionsMenu();
        setUpData();
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notificationCounter != null) {
            mNotificationHelper.setUpNotifications(notificationCounter, notifications,
                    clearNotifications, message);
        }
    }

    @OnClick(R.id.clear_notifications)
    public void clearNotifications() {
        notificationCounter.setVisibility(View.GONE);
        notifications.setVisibility(View.GONE);
        clearNotifications.setVisibility(View.GONE);
        message.setVisibility(View.VISIBLE);
        mHelper.clearNotifications();
    }

    @OnClick(R.id.transfer_submissions)
    public void transferSubmissions() {
        new MaterialDialog.Builder(this)
                .title(R.string.title_transfer_submissions)
                .adapter(new TransferSubmissionAdapter(this, mHelper.getUsers(form.getFormId())), (materialDialog, view, i, charSequence) -> {
                    materialDialog.dismiss();
                    showTransferDialogForUser((UserData) materialDialog.getListView().getAdapter().getItem(i));
                })
                .negativeText(R.string.button_cancel)
                .show();
    }

    private void showTransferDialogForUser(UserData user) {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.title_user_is_going_to_cede, user.getName()))
                .negativeText(R.string.button_cancel)
                .adapter(new UserSubmissionsTransferAdapter(this, user, form.getFormId(),
                        mHelper.getSubmissions(form.getFormId()), mHelper.getAttributions()), (dialog, view, position, charSequence) -> {
                    dialog.dismiss();
                    pickDestinationSurveyTaker(user, (SubmissionStatus) dialog.getListView().getAdapter().getItem(position));
                })
                .show();
    }

    private void pickDestinationSurveyTaker(UserData user, SubmissionStatus status) {
        new MaterialDialog.Builder(this)
                .title(R.string.title_transfer_to)
                .adapter(new DestinationTransferSubmissionAdapter(this, user, mHelper.getUsers(form.getFormId())), (dialog, view, position, charSequence) -> {
                    dialog.dismiss();
                    showConfirmationTransferDialog(user, status, (UserData) dialog.getListView().getAdapter().getItem(position));
                })
                .negativeText(R.string.button_cancel)
                .show();
    }

    private void showConfirmationTransferDialog(UserData from, SubmissionStatus status, UserData destination) {
        String message;
        if (status == null) {
            message = getString(R.string.message_transfer_submissions, from.getName(), destination.getName());
        } else if (status.equals(SubmissionStatus.NEW)) {
            message = getString(R.string.message_transfer_new_submissions, from.getName(), destination.getName());
        } else {
            message = getString(R.string.message_transfer_status_submissions, status.toString(this), from.getName(), destination.getName());
        }

        new MaterialDialog.Builder(this)
                .title(R.string.title_transfer_submissions_confirmation)
                .positiveText(R.string.button_yes)
                .negativeText(R.string.button_no)
                .content(message)
                .onPositive((materialDialog, dialogAction) -> {
                    mHelper.addTransfer(
                            mModeratorHelper.getAttributionId(from.getId(), form.getFormId()),
                            form.getFormId(),
                            status,
                            mModeratorHelper.getAttributionId(destination.getId(), form.getFormId())
                    );
                })
                .show();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        setUpData(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }
}
