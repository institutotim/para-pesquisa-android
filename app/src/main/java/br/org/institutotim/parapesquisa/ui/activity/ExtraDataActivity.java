package br.org.institutotim.parapesquisa.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.adapter.ExtraDataAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;

public class ExtraDataActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.status_image)
    ImageView statusImage;
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.started)
    TextView started;

    @Bind(R.id.extra_data_list)
    RecyclerView list;

    private UserSubmission submission;
    private FormData form;

    @Inject
    ParaPesquisaOpenHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_data);
        ButterKnife.bind(this);
        getComponent().inject(this);

        statusImage.setImageResource(R.drawable.inprogress_survey);

        list.setLayoutManager(new LinearLayoutManager(this));

        overridePendingTransition(R.anim.activity_open_translate2, R.anim.activity_close_scale);

        form = getIntent().getParcelableExtra(FORM_EXTRA);
        submission = getIntent().getParcelableExtra(SUBMISSION_EXTRA);

        started.setText(getString(R.string.atext_started_on, getIntent().getStringExtra(STARTED_EXTRA)));

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setUpTitle();
        setUpExtraData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpExtraData() {
        List<List<String>> content = new ArrayList<>();
        for (int i = 0; i < form.getSections().size(); i++) {
            Section section = form.getSections().get(i);
            for (int j = 0; j < section.getFields().size(); j++) {
                Field field = section.getFields().get(j);
                for (int k = 0; k < submission.getAnswers().size(); k++) {
                    Answer answer = submission.getAnswers().get(k);
                    if (answer.getFieldId() == field.getId()) {
                        content.add(Arrays.asList(field.getLabel(), answer.getValues()));
                    }
                }
            }
        }

        list.setAdapter(new ExtraDataAdapter(content));
    }

    private void setUpTitle() {
        String title = mHelper.getIdentifier(submission);
        if (title != null && !title.isEmpty()) {
            this.title.setText(title);
        } else {
            this.title.setText(R.string.text_new_submission);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate2);
    }
}
