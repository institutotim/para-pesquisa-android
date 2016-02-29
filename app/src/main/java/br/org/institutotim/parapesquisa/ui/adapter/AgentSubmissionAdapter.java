package br.org.institutotim.parapesquisa.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.SubmissionLog;
import br.org.institutotim.parapesquisa.data.model.SubmissionLogAction;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.util.DateUtils;
import butterknife.Bind;
import butterknife.ButterKnife;

public class AgentSubmissionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;

    private List<UserSubmission> mSubmissions;
    private boolean showLoading = true;

    public AgentSubmissionAdapter(Context context) {
        mContext = context;
    }

    public UserSubmission getItem(int position) {
        return mSubmissions.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_list_loading,
                    parent, false));
        }

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agent_submission,
                parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) viewHolder;
            UserSubmission submission = mSubmissions.get(position);

            if (submission.getIdentifier() != null) {
                holder.title.setText(submission.getIdentifier());
            } else {
                holder.title.setText(mContext.getString(R.string.text_submission) + (submission.getId() > 0 ? String.format(" #%d", submission.getId()) : ""));
            }

            SubmissionLog log = submission.getLatestLog();
            String when = log != null ? DateUtils.getFullDateTimeInstanceWithoutSeconds().print(log.getWhen()) : "";

            String statusText, date = null;
            int color = 0;

            if (submission.getInProgress() != null && submission.getInProgress()) {
                holder.statusImage.setImageResource(R.drawable.inprogress_survey);
                color = ContextCompat.getColor(mContext, R.color.color_14);
                statusText = mContext.getString(R.string.text_in_progress);
                date = " " + mContext.getString(R.string.text_since, when);
            } else if (submission.getStatus() == null) {
                holder.statusImage.setImageResource(R.drawable.syncpending_survey);
                color = ContextCompat.getColor(mContext, R.color.color_2);
                statusText = mContext.getString(R.string.text_waiting_sync);
                date = " " + mContext.getString(R.string.text_since, when);
            } else {
                switch (submission.getStatus()) {
                    case WAITING_APPROVAL:
                        holder.statusImage.setImageResource(R.drawable.pendingapproval_survey);
                        color = ContextCompat.getColor(mContext, R.color.color_16);
                        statusText = mContext.getString(R.string.text_pending_approve);
                        date = " " + mContext.getString(R.string.text_since, when);
                        break;
                    case WAITING_CORRECTION:
                        holder.statusImage.setImageResource(R.drawable.requestcorrection_survey);
                        color = ContextCompat.getColor(mContext, R.color.color_15);
                        statusText = mContext.getString(R.string.text_pending_correction);
                        date = " " + mContext.getString(R.string.text_since, when);
                        break;
                    case APPROVED:
                        holder.statusImage.setImageResource(R.drawable.approved_survey);
                        color = ContextCompat.getColor(mContext, R.color.color_13);
                        statusText = mContext.getString(R.string.text_approved);
                        date = " " + mContext.getString(R.string.text_on, when);
                        break;
                    case CANCELLED:
                        holder.statusImage.setImageResource(R.drawable.canceled_survey);
                        color = ContextCompat.getColor(mContext, R.color.color_3);
                        statusText = mContext.getString(R.string.text_cancelled);
                        date = " " + mContext.getString(R.string.text_on, when);
                        break;
                    case RESCHEDULED:
                        holder.statusImage.setImageResource(R.drawable.rescheduled_survey);
                        color = ContextCompat.getColor(mContext, R.color.color_12);
                        statusText = mContext.getString(R.string.text_rescheduled);
                        log = submission.getLatestLogByAction(SubmissionLogAction.RESCHEDULED);
                        when = log != null ? DateUtils.getFullDateTimeInstanceWithoutSeconds().print(log.getWhen()) : "";
                        date = " " + mContext.getString(R.string.text_to, when);
                        break;
                    default:
                        statusText = "";
                        break;
                }
            }

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(statusText).append(date);
            builder.setSpan(new ForegroundColorSpan(color), 0, statusText.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.status.setText(builder);
        }
    }

    @Override
    public int getItemCount() {
        return mSubmissions != null ? mSubmissions.size() + (showLoading ? 1 : 0) : 0;
    }

    public void addData(List<UserSubmission> submissions) {
        if (!mSubmissions.containsAll(submissions)) {
            mSubmissions.addAll(submissions);
            notifyDataSetChanged();
        }
    }

    public void setData(List<UserSubmission> submissions) {
        mSubmissions = submissions;
        if (submissions.isEmpty()) showLoading = false;
        notifyDataSetChanged();
    }

    public void hideLoading() {
        showLoading = false;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (mSubmissions != null) {
            return position == mSubmissions.size() ? (showLoading ? 1 : 0) : 0;
        }
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.status_image)
        ImageView statusImage;
        @Bind(R.id.status)
        TextView status;
        @Bind(R.id.title)
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }
}
