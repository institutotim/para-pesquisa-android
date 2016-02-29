package br.org.institutotim.parapesquisa.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Stats;
import br.org.institutotim.parapesquisa.ui.helper.StatsHelper;
import br.org.institutotim.parapesquisa.util.DateUtils;
import butterknife.Bind;
import butterknife.ButterKnife;

public class FormReportActivity extends BaseActivity {

    public static final String FORM_ID_EXTRA = "form_id_extra";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.header)
    ViewGroup header;

    @Bind(R.id.assigned)
    TextView remainingDays;
    @Bind(R.id.surveyTakers)
    TextView surveyTakers;
    @Bind(R.id.assignedLabel)
    TextView assignedLabel;
    @Bind(R.id.individualReports)
    TextView individualReports;
    @Bind(R.id.approved)
    TextView approved;
    @Bind(R.id.pendingCorrection)
    TextView pendingCorrection;
    @Bind(R.id.waitingApproval)
    TextView waitingApproval;
    @Bind(R.id.rescheduled)
    TextView rescheduled;
    @Bind(R.id.cancelled)
    TextView cancelled;
    @Bind(R.id.goal)
    TextView goal;
    @Bind(R.id.remaining)
    TextView remaining;
    @Bind(R.id.since)
    TextView since;

    @Bind(R.id.cornerIcon)
    ImageView cornerIcon;

    private long formId;

    @Inject
    StatsHelper mStatsHelper;
    @Inject
    ParaPesquisaOpenHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_report);
        ButterKnife.bind(this);
        getComponent().inject(this);

        formId = getIntent().getLongExtra(FORM_ID_EXTRA, 0l);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        header.setVisibility(View.VISIBLE);

        setUpData(mStatsHelper.getFormStats(formId));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpData(Stats stats) {
        remainingDays.setText(String.valueOf(stats.getRemainingDays()));
        surveyTakers.setText(String.valueOf(stats.getSurveyTakers()));
        approved.setText(String.valueOf(stats.getApproved()));
        pendingCorrection.setText(String.valueOf(stats.getPendingCorrection()));
        waitingApproval.setText(String.valueOf(stats.getPendingApproval()));
        rescheduled.setText(String.valueOf(stats.getRescheduled()));
        cancelled.setText(String.valueOf(stats.getCancelled()));
        goal.setText(String.valueOf(stats.getGoal()));
        remaining.setText(String.valueOf(stats.getRemaining()));

        cornerIcon.setImageResource(R.drawable.report_days_icon);
        assignedLabel.setText(R.string.text_remaining_days);
        individualReports.setVisibility(View.GONE);

        FormData form = mHelper.getForm(formId);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(form.getName());
        }
        since.setText(getString(R.string.text_since, DateUtils.getFullDateTimeInstanceWithoutSeconds().print(form.getCreatedAt())));
    }
}
