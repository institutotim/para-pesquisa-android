package br.org.institutotim.parapesquisa.ui.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.model.Attribution;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserForm;
import br.org.institutotim.parapesquisa.data.model.UserRole;
import br.org.institutotim.parapesquisa.util.DateUtils;
import butterknife.Bind;
import butterknife.ButterKnife;

public class FormAdapter extends RecyclerView.Adapter<FormAdapter.ViewHolder> {
    private List<UserForm> mForms;
    private Context mContext;
    private ParaPesquisaOpenHelper mHelper;
    private UserData mUser;

    public FormAdapter(Context context, ParaPesquisaOpenHelper helper, @Nullable UserData user) {
        this.mContext = context;
        this.mHelper = helper;
        this.mUser = user;
    }

    public UserForm getItem(int position) {
        return mForms.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_form, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserForm form = mForms.get(position);

        DateTimeFormatter format = DateUtils.getShortDateInstanceWithoutYears();
        FormData formData = form.getForm();

        holder.title.setText(formData.getName());

        DateTime startTime = formData.getPubStart();
        String startDate = startTime == null ? mContext.getString(R.string.text_indefined_date) : format.print(startTime);

        DateTime endTime = formData.getPubEnd();
        String endDate =  endTime == null ? mContext.getString(R.string.text_indefined_date) : format.print(endTime);



        int remaining;
        if (endTime != null && DateTime.now().isAfter(endTime.plusDays(1)
                .withTimeAtStartOfDay())) {
            remaining = 0;
        } else {
            remaining = endTime != null ? Days.daysBetween(DateTime.now(), endTime)
                    .getDays() + 1 : 999;
        }
        holder.remainingDays.setText(String.valueOf(remaining));

        DateTime realStartTime = startTime == null ? formData.getCreatedAt() : startTime;
        DateTime realEndTime = endTime == null ? realStartTime.plusDays(999) : endTime;

        String totalDays = mContext.getString(R.string.slash_separator,
                String.valueOf(Days.daysBetween(realStartTime, realEndTime).getDays()),
                mContext.getString(R.string.text_days_remaining));
        holder.totalDays.setText(totalDays);


        String subtitle = (formData.getSubtitle() != null ? formData.getSubtitle() + " | " : "");
        if (form.getForm().getPubEnd() != null) {
            subtitle += mContext.getString(R.string.text_from_dates, startDate,
                    endDate);
        } else {
            subtitle += mContext.getString(R.string.undefined_date);
        }
        holder.subtitle.setText(subtitle);

        long quota = 0;
        if (UserRole.AGENT.equals(mUser.getRole())) {
            quota = form.getQuota();
            holder.remainingSurveys.setText(String.valueOf(mHelper.getRemainingSurveys(form.getFormId())));
        } else {
            List<Attribution> attributions = mHelper.getAttributions(form.getFormId());
            for (int i = 0; i < attributions.size(); i++) {
                quota += attributions.get(i).getQuota();
            }
            holder.remainingSurveys.setText(String.valueOf(mHelper.getRemainingSurveys(form.getFormId(), quota)));
        }
        holder.totalSurveys.setText(mContext.getString(R.string.slash_separator, String.valueOf(quota),
                mContext.getString(R.string.text_submissions_remaining)));
    }

    @Override
    public int getItemCount() {
        return mForms != null ? mForms.size() : 0;
    }

    public void setData(List<UserForm> forms) {
        this.mForms = forms;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.subtitle)
        TextView subtitle;
        @Bind(R.id.remaining_surveys)
        TextView remainingSurveys;
        @Bind(R.id.total_surveys)
        TextView totalSurveys;
        @Bind(R.id.remaining_days)
        TextView remainingDays;
        @Bind(R.id.total_days)
        TextView totalDays;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
