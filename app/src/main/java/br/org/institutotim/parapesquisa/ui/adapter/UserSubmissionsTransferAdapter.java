package br.org.institutotim.parapesquisa.ui.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Attribution;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import butterknife.Bind;
import butterknife.ButterKnife;

public class UserSubmissionsTransferAdapter extends BaseAdapter {

    private final Context context;
    private List<SubmissionStatus> statuses = new ArrayList<>();
    private List<String> strings = new ArrayList<>();

    public UserSubmissionsTransferAdapter(Context context, UserData user, long formId, List<UserSubmission> submissions,
                                          List<Attribution> attributions) {
        this.context = context;

        Map<SubmissionStatus, Integer> counter = new ArrayMap<>();
        for (UserSubmission submission : submissions) {
            if (submission.getOwner().getId() == user.getId()) {
                if (counter.containsKey(submission.getStatus())) {
                    counter.put(submission.getStatus(), counter.get(submission.getStatus()) + 1);
                } else {
                    counter.put(submission.getStatus(), 1);
                }
            }
        }

        int total = 0;
        for (SubmissionStatus status : counter.keySet()) {
            statuses.add(status);
            total += counter.get(status);
            switch (status) {
                case NEW:
                    strings.add(0, context.getString(R.string.text_new_submissions_counter, counter.get(status)));
                    break;
                case WAITING_APPROVAL:
                    strings.add(context.getString(R.string.text_waiting_approval_submissions_counter, counter.get(status)));
                    break;
                case WAITING_CORRECTION:
                    strings.add(context.getString(R.string.text_waiting_correction_submissions_counter, counter.get(status)));
                    break;
                case APPROVED:
                    strings.add(context.getString(R.string.text_approved_submissions_counter, counter.get(status)));
                    break;
                case CANCELLED:
                    strings.add(context.getString(R.string.text_cancelled_submissions_counter, counter.get(status)));
                    break;
                case RESCHEDULED:
                    strings.add(context.getString(R.string.text_rescheduled_submissions_counter, counter.get(status)));
                    break;
            }
        }

        for (Attribution quota : attributions) {
            if (quota.getUser().getId() == user.getId() && quota.getFormId() == formId) {
                if (!statuses.contains(SubmissionStatus.NEW)) strings.add(0, context.getString(R.string.text_new_submissions_counter, quota.getQuota() - total));
                strings.add(0, context.getString(R.string.text_all_submissions_counter, quota.getQuota()));
                break;
            }
        }
    }

    @Override
    public int getCount() {
        return strings.size();
    }

    @Override
    public SubmissionStatus getItem(int position) {
        if (position == 0) return null;
        if (position == 1) return SubmissionStatus.NEW;
        return statuses.get(position - (statuses.contains(SubmissionStatus.NEW) ? 1 : 2));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setTextColor(context.getResources().getColor(R.color.color_5));
        holder.name.setText(strings.get(position));

        return convertView;
    }

    class ViewHolder {

        @Bind(android.R.id.text1)
        TextView name;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
