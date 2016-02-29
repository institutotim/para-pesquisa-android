package br.org.institutotim.parapesquisa.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserRole;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HelpActivity extends BaseActivity {

    public static final String HELP_INDEX = "br.org.institutotim.parapesquisa.help_index";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.previous)
    View previous;
    @Bind(R.id.next)
    View next;

    @Bind(R.id.image)
    ImageView image;
    @Bind(R.id.text)
    TextView text;
    @Bind(R.id.section_indicator)
    TextView sectionIndicator;
    @Bind(R.id.warning)
    View warning;

    int index = 1;

    private UserRole role = UserRole.AGENT;

    @Inject
    ParaPesquisaPreferences mPreferences;

    private static final int MAX_INDEX_AGENT = 21;
    private static final int MAX_INDEX_MODERATOR = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);
        getComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        UserData user = mPreferences.getUser();
        if (user != null) {
            role = user.getRole();
        }

        index = getIntent().getIntExtra(HELP_INDEX, 1);
        loadData(index);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void loadData(int index) {
        image.setImageResource(getResources().getIdentifier((role.equals(UserRole.AGENT) ? "p_help_img" : "c_help_img") + index, "drawable", getPackageName()));
        if (index == 1) previous.setVisibility(View.INVISIBLE);
        if (index == (role.equals(UserRole.AGENT) ? MAX_INDEX_AGENT : MAX_INDEX_MODERATOR))
            next.setVisibility(View.INVISIBLE);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        String current = String.valueOf(index);

        String total = String.format("/%d", role.equals(UserRole.AGENT) ? MAX_INDEX_AGENT : MAX_INDEX_MODERATOR);

        builder.append(current).append(total);
        sectionIndicator.setText(builder);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getTitleForIndex());
            actionBar.setSubtitle(getSubtitleForIndex());
        }

        text.setText(Html.fromHtml(getResources().getStringArray(role.equals(UserRole.AGENT) ? R.array.help_text_survey_taker : R.array.help_text_moderator)[index - 1]));

        setWarningVisibility();
    }

    private void setWarningVisibility() {
        if (role.equals(UserRole.AGENT)) {
            if (Arrays.asList(4, 8, 11, 12, 13, 14, 15, 16, 17).contains(index)) {
                warning.setVisibility(View.VISIBLE);
            } else {
                warning.setVisibility(View.GONE);
            }
        } else {
            if (index == 12) {
                warning.setVisibility(View.VISIBLE);
            } else {
                warning.setVisibility(View.GONE);
            }
        }
    }

    private String getSubtitleForIndex() {
        if (role.equals(UserRole.AGENT)) {
            int title;
            switch (index) {
                case 3:
                    title = index;
                    break;
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                    title = index;
                    break;
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                    title = index + 1;
                    break;
                default:
                    return "";
            }

            return getResources().getStringArray(R.array.help_index_survey_taker)[title];
        } else {
            int title;
            switch (index) {
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                    title = index + 1;
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    title = index;
                    break;
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                    title = index + 2;
                    break;
                default:
                    return "";
            }

            return getResources().getStringArray(R.array.help_index_moderator)[title];
        }
    }

    private String getTitleForIndex() {
        if (role.equals(UserRole.AGENT)) {
            int title;
            switch (index) {
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                    title = 2;
                    break;
                case 13:
                case 15:
                case 16:
                case 17:
                    title = 13;
                    break;
                case 18:
                case 19:
                case 20:
                case 21:
                    title = index + 1;
                    break;
                default:
                    title = index - 1;
                    break;
            }

            return getResources().getStringArray(R.array.help_index_survey_taker)[title];
        } else {
            int title;
            switch (index) {
                case 2:
                case 3:
                case 4:
                case 5:
                    title = 1;
                    break;
                case 6:
                    title = index;
                    break;
                case 14:
                    title = index + 1;
                    break;
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                    title = 7;
                    break;
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                    title = 16;
                    break;
                case 22:
                case 23:
                case 24:
                case 25:
                    title = index + 2;
                    break;
                default:
                    title = index - 1;
                    break;
            }

            return getResources().getStringArray(R.array.help_index_moderator)[title];
        }
    }

    @OnClick(R.id.previous)
    public void previous() {
        next.setVisibility(View.VISIBLE);
        index--;
        if (index == 1) previous.setVisibility(View.INVISIBLE);
        loadData(index);
    }

    @OnClick(R.id.next)
    public void next() {
        previous.setVisibility(View.VISIBLE);
        index++;
        if (index == (role.equals(UserRole.AGENT) ? MAX_INDEX_AGENT : MAX_INDEX_MODERATOR))
            next.setVisibility(View.INVISIBLE);
        loadData(index);
    }
}
