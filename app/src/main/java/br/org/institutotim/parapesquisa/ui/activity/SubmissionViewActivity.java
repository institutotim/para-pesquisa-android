package br.org.institutotim.parapesquisa.ui.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.adapter.SectionPagerAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SubmissionViewActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

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

    private UserSubmission mSubmission;
    private FormData mForm;
    private SectionPagerAdapter mAdapter;

    @Inject
    ParaPesquisaOpenHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_view);
        ButterKnife.bind(this);
        getComponent().inject(this);

        mSubmission = getIntent().getParcelableExtra(SUBMISSION_EXTRA);
        mForm = mHelper.getForm(getIntent().getLongExtra(FORM_EXTRA, 0));

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            String identifier = mHelper.getIdentifier(mSubmission);
            if (identifier != null) {
                actionBar.setTitle(identifier);
            } else {
                getSupportActionBar().setTitle(getString(R.string.text_submission) + String.format(" #%d",
                        mSubmission.getId()));
            }
        }

        mAdapter = SectionPagerAdapter.builderSectionDisabled(mForm, mSubmission);
        mContainer.setAdapter(mAdapter);
        mContainer.addOnPageChangeListener(this);
        mPrevious.setVisibility(View.INVISIBLE);

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
        if (!mAdapter.validateCurrentPage(mContainer)) {
            return;
        }

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
