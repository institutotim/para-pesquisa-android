package br.org.institutotim.parapesquisa.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.model.GlobalStats;
import br.org.institutotim.parapesquisa.data.model.UserForm;
import br.org.institutotim.parapesquisa.ui.adapter.UserFormAdapter;
import br.org.institutotim.parapesquisa.ui.helper.StatsHelper;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GlobalReportActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.header)
    ViewGroup header;

    @Bind(R.id.assigned)
    TextView assignedToMe;
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

    @Bind(R.id.cornerIcon)
    ImageView cornerIcon;

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

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        header.setVisibility(View.GONE);

        setUpData(mStatsHelper.getGlobalStats());
    }

    private void setUpData(GlobalStats stats) {
        assignedToMe.setText(String.valueOf(stats.getAssignedToMe()));
        surveyTakers.setText(String.valueOf(stats.getSurveyTakers()));
        approved.setText(String.valueOf(stats.getApproved()));
        pendingCorrection.setText(String.valueOf(stats.getPendingCorrection()));
        waitingApproval.setText(String.valueOf(stats.getPendingApproval()));
        rescheduled.setText(String.valueOf(stats.getRescheduled()));
        cancelled.setText(String.valueOf(stats.getCancelled()));
        goal.setText(String.valueOf(stats.getTotalGoal()));
        remaining.setText(String.valueOf(stats.getRemaining()));

        cornerIcon.setImageResource(R.drawable.report_surveys_icon);
        assignedLabel.setText(R.string.text_surveys_assigned_to_me);
        individualReports.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.individualReports)
    public void openIndividualReports() {
        List<UserForm> forms = mHelper.getUserForms();

        new MaterialDialog.Builder(this)
                .title(R.string.title_individual_reports)
                .neutralText(R.string.button_cancel)
                .adapter(new UserFormAdapter(this, forms), (dialog, view, position, charSequence) -> {
                    Intent intent = new Intent(GlobalReportActivity.this, FormReportActivity.class);
                    intent.putExtra(FormReportActivity.FORM_ID_EXTRA, forms.get(position).getFormId());
                    dialog.dismiss();
                    startActivity(intent);
                    finish();
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
