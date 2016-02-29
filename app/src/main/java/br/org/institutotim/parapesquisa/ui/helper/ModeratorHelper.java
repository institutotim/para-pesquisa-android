package br.org.institutotim.parapesquisa.ui.helper;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.model.Attribution;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.UserData;
import butterknife.ButterKnife;

public class ModeratorHelper {

    private final ParaPesquisaPreferences mPreferences;
    private final ParaPesquisaOpenHelper mHelper;

    public ModeratorHelper(ParaPesquisaPreferences preferences, ParaPesquisaOpenHelper helper) {
        this.mPreferences = preferences;
        this.mHelper = helper;
    }

    public void addComment(Context context, View view, String content) {
        ButterKnife.findById(view, R.id.comment_area).setVisibility(View.VISIBLE);

        view.findViewById(R.id.comment_anchor).setVisibility(View.GONE);
        view.findViewById(R.id.edit_anchor).setVisibility(View.VISIBLE);
        view.findViewById(R.id.delete_anchor).setVisibility(View.VISIBLE);
        view.findViewById(R.id.comment).setVisibility(View.VISIBLE);

        UserData user = mPreferences.getUser();

        if (user != null) {
            ((TextView) ButterKnife.findById(view, R.id.user_name)).setText(user.getName());
            Glide.with(context).load(user.getAvatarUrl()).into((ImageView) ButterKnife.findById(view, R.id.user_image));
        }

        ((TextView) ButterKnife.findById(view, R.id.comment)).setText(content);

        ButterKnife.findById(view, R.id.user_name).setVisibility(View.VISIBLE);
        ButterKnife.findById(view, R.id.user_image).setVisibility(View.VISIBLE);
    }

    public void removeComment(View view) {
        view.findViewById(R.id.comment_anchor).setVisibility(View.VISIBLE);
        view.findViewById(R.id.edit_anchor).setVisibility(View.GONE);
        view.findViewById(R.id.delete_anchor).setVisibility(View.GONE);
        view.findViewById(R.id.comment).setVisibility(View.GONE);
        ButterKnife.findById(view, R.id.user_name).setVisibility(View.GONE);
        ButterKnife.findById(view, R.id.user_image).setVisibility(View.GONE);
    }

    public void addCommentReadOnly(Context context, View view, SubmissionCorrection correction) {
        ButterKnife.findById(view, R.id.comment_area).setVisibility(View.VISIBLE);

        view.findViewById(R.id.comment_anchor).setVisibility(View.GONE);
        view.findViewById(R.id.edit_anchor).setVisibility(View.GONE);
        view.findViewById(R.id.delete_anchor).setVisibility(View.GONE);
        view.findViewById(R.id.comment).setVisibility(View.VISIBLE);
        view.findViewById(R.id.comment).setBackgroundColor(ContextCompat.getColor(context, R.color.color_15));

        UserData user = mPreferences.getUser();

        if (user != null) {
            ((TextView) ButterKnife.findById(view, R.id.user_name)).setText(user.getName());
            ((Activity) context).runOnUiThread(() -> Glide.with(context).load(user.getAvatarUrl()).into((ImageView) ButterKnife.findById(view, R.id.user_image)));
        }

        ((TextView) ButterKnife.findById(view, R.id.comment)).setText(correction.getMessage());

        ButterKnife.findById(view, R.id.user_name).setVisibility(View.VISIBLE);
        ButterKnife.findById(view, R.id.user_image).setVisibility(View.VISIBLE);
    }

    public long getAttributionId(long userId, long formId) {
        List<Attribution> attributions = mHelper.getAttributions();
        for (int i = 0; i < attributions.size(); i++) {
            if (attributions.get(i).getFormId() == formId && attributions.get(i).getUser().getId() == userId) {
                return attributions.get(i).getId();
            }
        }
        return -1l;
    }
}
