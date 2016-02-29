package br.org.institutotim.parapesquisa.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.helper.FieldHelper;
import br.org.institutotim.parapesquisa.ui.helper.SectionHelper;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SubmissionViewActivityOld extends BaseActivity {

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

    private UserSubmission mSubmission;
    private FormData mForm;

    private List<View> mSections = new ArrayList<>();

    private int mCurrentSection;

    @Inject
    ParaPesquisaOpenHelper mHelper;
    @Inject
    FieldHelper mFieldHelper;
    @Inject
    SectionHelper mSectionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_view);
        ButterKnife.bind(this);
        //getComponent().inject(this);

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
        for (int i = 0; i < mForm.getSections().size(); i++) {
            setUpSection(mForm.getSections().get(i), i + 1);
        }
    }

    private void setUpSection(Section section, int sectionNumber) {
        View sectionView = getLayoutInflater().inflate(R.layout.view_section_old, mContainer, false);
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

        mSections.add(sectionView);
        runOnUiThread(() -> mContainer.addView(sectionView));
    }

    private void setUpField(ViewGroup container, Field field, int fieldNumber, int sectionNumber) {
        View view = mFieldHelper.inflateFilledReadOnlyWithComment(this, container, field, fieldNumber, sectionNumber, mSubmission);
        if (view != null) container.addView(view);
    }

    private void selectSection(int section) {
        mCurrentSection = section;

        runOnUiThread(() -> {
            for (int i = 0; i < mSections.size(); i++) {
                mSections.get(i).setVisibility(View.INVISIBLE);
            }
        });

        if (mSections.size() == 0) return;
        runOnUiThread(() -> {
            mSections.get(section - 1).setVisibility(View.VISIBLE);

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

    private void setSectionCounterText(int currentSection) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String current = String.valueOf(currentSection);
        String total = String.format("/%d", mSectionHelper.getTotalSections(mSections));

        runOnUiThread(() -> mNavigation.setVisibility(mSectionHelper.getTotalSections(mSections) == 1 ? View.GONE : View.VISIBLE));

        builder.append(current).append(total);
        runOnUiThread(() -> mSectionIndicator.setText(builder));
    }

    @OnClick(R.id.next)
    public void nextSection() {
        selectSection(mCurrentSection + 1);
    }

    @OnClick(R.id.previous)
    public void previousSection() {
        selectSection(mCurrentSection - 1);
    }
}
