package br.org.institutotim.parapesquisa.ui.viewholder;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper;
import br.org.institutotim.parapesquisa.util.ViewUtils;
import butterknife.Bind;
import de.greenrobot.event.EventBus;

import static butterknife.ButterKnife.bind;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.title)
    public TextView title;
    @Nullable
    @Bind(R.id.number)
    public TextView number;
    @Bind(R.id.description)
    public TextView description;
    @Nullable
    @Bind(R.id.validation_message)
    public TextView validationMessage;


    @Bind(R.id.comment_area)
    public LinearLayout commentArea;
    @Bind(R.id.comment_anchor)
    public ImageView commentAnchor;
    @Bind(R.id.edit_anchor)
    public ImageView commentEditAnchor;
    @Bind(R.id.delete_anchor)
    public ImageView commentDeleteAnchor;
    @Bind(R.id.comment)
    public TextView comment;
    @Bind(R.id.user_image)
    public ImageView commentUserImage;
    @Bind(R.id.user_name)
    public TextView commentUserName;

    private Field field;

    private OnAnswerValueChangedListener listener;

    protected boolean fillingData;

    public BaseViewHolder(View itemView) {
        super(itemView);
        bind(this, itemView);
    }

    public void disable() {
        ViewUtils.disable(itemView);
    }

    public void enable() {
        ViewUtils.enable(itemView);
    }

    public void fillData(Field field, @Nullable Answer answer) {
        fillingData = true;
        this.field = field;
        commentArea.setVisibility(View.GONE);
        itemView.setTag(field);
        title.setText(field.getLabel());
        if (field.getDescription() == null || field.getDescription().trim().isEmpty()) {
            description.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
            description.setText(field.getDescription());
        }
        if (number != null) {
            number.setText(String.valueOf(getAdapterPosition() + 1));
        }
        fillingData = false;
    }

    public abstract Answer extractAnswer();

    public void setOnAnswerValueChangedListener(OnAnswerValueChangedListener listener) {
        this.listener = listener;
    }

    protected Field getField() {
        return field;
    }

    protected void notifyListener(Field field, Answer answer) {
        if (!fillingData && listener != null) {
            listener.onChange(field, answer);

            if (shouldProcessActions()) {
                final RecyclerViewHelper.ActionWrapper actionWrapper = RecyclerViewHelper.processActions(field, answer);

                if (!actionWrapper.getDisable().isEmpty()
                        || !actionWrapper.getEnable().isEmpty()
                        || !actionWrapper.getDisableSections().isEmpty()
                        || !actionWrapper.getEnableSections().isEmpty()) {
                    EventBus.getDefault().post(actionWrapper);
                }
            }
        }
    }

    private boolean shouldProcessActions() {
        return this instanceof CheckboxViewHolder
                || this instanceof RadioViewHolder
                || this instanceof SelectViewHolder;
    }

    public interface OnAnswerValueChangedListener {

        void onChange(Field field, Answer answer);
        void addComment(Field field, String comment);
        void removeComment(Field field);
    }

    public void showError(@StringRes int text) {
        if (validationMessage != null) {
            validationMessage.setVisibility(View.VISIBLE);
            validationMessage.setText(text);
        }
        title.setTextColor(ContextCompat.getColor(title.getContext(), R.color.color_15));
    }

    public void hideError() {
        if (validationMessage != null) {
            validationMessage.setVisibility(View.GONE);
        }
        title.setTextColor(ContextCompat.getColor(title.getContext(), R.color.color_7));
    }

    public void highlightTitle() {
        final Context context = itemView.getContext();
        title.setTextColor(ContextCompat.getColor(context, R.color.color_15));
        if (number != null) {
            number.setTextColor(ContextCompat.getColor(context, R.color.color_15));
        }
    }

    public void addCommentReadOnly(SubmissionCorrection correction, UserData user) {
        final Context context = itemView.getContext();
        commentArea.setVisibility(View.VISIBLE);

        commentAnchor.setVisibility(View.GONE);
        commentEditAnchor.setVisibility(View.GONE);
        commentDeleteAnchor.setVisibility(View.GONE);
        comment.setVisibility(View.VISIBLE);
        comment.setBackgroundColor(ContextCompat.getColor(context, R.color.color_15));

        if (user != null) {
            commentUserName.setText(user.getName());
            if (!TextUtils.isEmpty(user.getAvatarUrl())) {
                ((Activity) context).runOnUiThread(() -> Glide.with(context).load(user.getAvatarUrl()).into(commentUserImage));
            }
        }

        comment.setText(correction.getMessage());

        commentUserImage.setVisibility(View.VISIBLE);
        commentUserImage.setVisibility(View.VISIBLE);

    }

    public void addCommentArea(SubmissionCorrection submissionCorrection, UserData user) {
        final Context context = itemView.getContext();
        description.setVisibility(View.GONE);
        commentArea.setVisibility(View.VISIBLE);
        commentAnchor.setVisibility(View.VISIBLE);

        StringBuilder builder = new StringBuilder();

        if (submissionCorrection != null) {
            addComment(context, submissionCorrection.getMessage(), user);
        }

        commentAnchor.setEnabled(true);
        commentAnchor.setOnClickListener(v -> {
            MaterialDialog dialog = getDialogBuilder(context, builder, field, user).build();
            dialog.setTitle(R.string.title_add_comment);
            EditText content = (EditText) dialog.getCustomView();
            content.setText("");
            dialog.show();
        });

        commentEditAnchor.setEnabled(true);
        commentEditAnchor.setOnClickListener(v -> {
            MaterialDialog dialog = getDialogBuilder(context, builder, field, user).build();
            dialog.setTitle(R.string.title_edit_comment);
            EditText content = (EditText) dialog.getCustomView();
            content.setText(builder.toString());
            dialog.show();
        });

        commentDeleteAnchor.setEnabled(true);
        commentDeleteAnchor.setOnClickListener(v -> new MaterialDialog.Builder(context)
                .title(R.string.title_remove_comment)
                .positiveText(R.string.button_yes)
                .negativeText(R.string.button_no)
                .content(R.string.message_remove_comment_confirmation)
                .autoDismiss(true)
                .onPositive((materialDialog, dialogAction) -> {
                    removeComment();
                    if (listener != null)
                        listener.removeComment(field);
                    builder.setLength(0);
                })
                .show());
    }

    private MaterialDialog.Builder getDialogBuilder(Context context, StringBuilder builder, Field field, UserData userData) {
        return new MaterialDialog.Builder(context)
                .title(R.string.title_add_comment)
                .negativeText(R.string.button_cancel)
                .positiveText(R.string.button_done)
                .customView(R.layout.comment_edit_text, false)
                .autoDismiss(true)
                .onPositive((materialDialog1, dialogAction1) -> {
                    EditText content = (EditText) materialDialog1.getCustomView();
                    if (!content.getText().toString().trim().isEmpty()) {
                        addComment(context, content.getText().toString().trim(), userData);
                        builder.setLength(0);
                        builder.append(content.getText().toString().trim());
                        if (listener != null)
                            listener.addComment(field, content.getText().toString().trim());
                        content.setText("");
                    }
                });
    }

    private void addComment(Context context, String content, UserData user) {
        commentArea.setVisibility(View.VISIBLE);

        commentAnchor.setVisibility(View.GONE);
        commentEditAnchor.setVisibility(View.VISIBLE);
        commentDeleteAnchor.setVisibility(View.VISIBLE);
        comment.setVisibility(View.VISIBLE);

        if (user != null) {
            commentUserName.setText(user.getName());
            if (!TextUtils.isEmpty(user.getAvatarUrl())) {
                Glide.with(context).load(user.getAvatarUrl()).into(commentUserImage);
            }
        }

        comment.setText(content);

        commentUserName.setVisibility(View.VISIBLE);
        commentUserImage.setVisibility(View.VISIBLE);
    }

    private void removeComment() {
        commentAnchor.setVisibility(View.VISIBLE);
        commentEditAnchor.setVisibility(View.GONE);
        commentDeleteAnchor.setVisibility(View.GONE);
        comment.setVisibility(View.GONE);
        commentUserName.setVisibility(View.GONE);
        commentUserImage.setVisibility(View.GONE);
    }

}
