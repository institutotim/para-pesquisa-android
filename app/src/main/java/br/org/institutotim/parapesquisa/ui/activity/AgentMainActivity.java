package br.org.institutotim.parapesquisa.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.api.ParaPesquisaApi;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.event.SyncCompletedEvent;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.service.AgentUpdateService;
import br.org.institutotim.parapesquisa.ui.fragment.AboutFragment;
import br.org.institutotim.parapesquisa.ui.fragment.AgentFormsFragment;
import br.org.institutotim.parapesquisa.ui.fragment.HelpFragment;
import br.org.institutotim.parapesquisa.ui.helper.NotificationHelper;
import br.org.institutotim.parapesquisa.ui.widget.WrapperLinearLayoutManager;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import progress.menu.item.ProgressMenuItemHelper;
import rx.android.schedulers.AndroidSchedulers;

public class AgentMainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.nav_view)
    NavigationView mNavView;

    @Inject
    ParaPesquisaPreferences mPreferences;
    @Inject
    ParaPesquisaOpenHelper mHelper;
    @Inject
    ParaPesquisaApi mApi;
    @Inject
    NotificationHelper mNotificationHelper;

    @Bind(R.id.notifications)
    RecyclerView mNotifications;
    @Bind(R.id.clear_notifications)
    Button mClearNotifications;
    @Bind(R.id.notification_message)
    TextView mEmptyMessage;

    private TextView mNotificationCounter;

    private ProgressMenuItemHelper mProgressHelper;

    int mSelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_main);
        ButterKnife.bind(this);
        getComponent().inject(this);

        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        mToolbar.setNavigationOnClickListener(v -> mDrawerLayout.openDrawer(GravityCompat.START));

        mNotifications.setLayoutManager(new WrapperLinearLayoutManager(this));

        setupNavigationDrawerHeader();

        mNavView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.text_my_surveys);
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container,
                    new AgentFormsFragment()).commitAllowingStateLoss();
            mSelected = R.id.my_surveys;
            supportInvalidateOptionsMenu();
        }
    }

    private void setupNavigationDrawerHeader() {
        View view = getLayoutInflater().inflate(R.layout.header_nav, mNavView, false);

        ImageView image = ButterKnife.findById(view, R.id.user_image);
        TextView name = ButterKnife.findById(view, R.id.user_name);
        TextView server = ButterKnife.findById(view, R.id.server_address);

        UserData user = mPreferences.getUser();
        if (user != null) {
            name.setText(user.getName());
            server.setText(mPreferences.getServerUrl());

            if (user.getAvatarUrl() != null) {
                Glide.with(this).load(user.getAvatarUrl()).asBitmap().centerCrop().into(new BitmapImageViewTarget(image) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        image.setImageDrawable(circularBitmapDrawable);
                    }
                });
            } else {
                Glide.with(this).load(R.drawable.sidebar_default_user_avatar).asBitmap().centerCrop().into(new BitmapImageViewTarget(image) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        image.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }
        }

        mNavView.addHeaderView(view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sync) {
            mProgressHelper.startProgress();
            showProgressDialog();
            startService(new Intent(this, AgentUpdateService.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mDrawerLayout.closeDrawer(GravityCompat.START);

        ActionBar actionBar = getSupportActionBar();
        Fragment fragment = null;
        switch (menuItem.getItemId()) {
            case R.id.my_surveys:
                if (actionBar != null) actionBar.setTitle(R.string.text_my_surveys);
                fragment = Fragment.instantiate(this, AgentFormsFragment.class.getName());
                break;
            case R.id.help:
                if (actionBar != null) actionBar.setTitle(R.string.text_help);
                fragment = Fragment.instantiate(this, HelpFragment.class.getName());
                break;
            case R.id.about:
                if (actionBar != null) actionBar.setTitle(R.string.text_about_app);
                fragment = Fragment.instantiate(this, AboutFragment.class.getName());
                break;
            case R.id.logout:
                logout();
                break;
        }

        if (fragment != null) {
            mSelected = menuItem.getItemId();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment)
                    .commitAllowingStateLoss();
            supportInvalidateOptionsMenu();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mSelected == R.id.my_surveys) {
            MenuInflater inflater = new MenuInflater(this);

            inflater.inflate(R.menu.agent_main, menu);
            mProgressHelper = new ProgressMenuItemHelper(menu, R.id.action_sync);

            View menuItem = menu.findItem(R.id.action_notifications).getActionView();
            mNotificationCounter = (TextView) menuItem.findViewById(R.id.counter);
            menuItem.setOnClickListener(this);

            mNotificationHelper.setUpNotifications(mNotificationCounter, mNotifications,
                    mClearNotifications, mEmptyMessage);

            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    void logout() {
        final List<UserSubmission> pendingSubmissions = mHelper.getPendingSubmissions();
        if (pendingSubmissions == null || pendingSubmissions.isEmpty()) {
            mApi.signOut().observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
                mPreferences.clearSession();
                mHelper.clearAll();
                startActivity(new Intent(this, SignInActivity.class));
                finish();
            }, this::showError);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_warning)
                    .setMessage(R.string.message_logout_warning)
                    .setPositiveButton(R.string.button_ok, null)
                    .show();
        }
    }

    @Override
    public void onClick(View v) {
        mDrawerLayout.openDrawer(GravityCompat.END);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNotificationCounter != null) {
            mNotificationHelper.setUpNotifications(mNotificationCounter, mNotifications,
                    mClearNotifications, mEmptyMessage);
        }
    }

    @OnClick(R.id.clear_notifications)
    public void clearNotifications() {
        mNotificationCounter.setVisibility(View.GONE);
        mNotifications.setVisibility(View.GONE);
        mClearNotifications.setVisibility(View.GONE);
        mEmptyMessage.setVisibility(View.VISIBLE);
        mHelper.clearNotifications();
    }

    public void onEventMainThread(SyncCompletedEvent event) {
        dismissProgressDialog();
        mProgressHelper.stopProgress();
        switch (event.getResult()) {
            case SUCCESS:
                showSnackBar(getString(R.string.message_sync_completed));
                mNotificationHelper.setUpNotifications(mNotificationCounter, mNotifications,
                        mClearNotifications, mEmptyMessage);
                break;
            case ERROR:
                showSnackBar(event.getMessage());
                break;
        }
    }
}
