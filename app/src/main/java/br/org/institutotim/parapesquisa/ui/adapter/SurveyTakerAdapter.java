package br.org.institutotim.parapesquisa.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.UserData;
import butterknife.Bind;
import butterknife.ButterKnife;

public class SurveyTakerAdapter extends BaseAdapter {

    private final Context context;
    private List<UserData> users;

    public SurveyTakerAdapter(Context context, List<UserData> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size() + 1;
    }

    @Override
    public UserData getItem(int position) {
        if (position == 0) return null;
        return users.get(position - 1);
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

        holder.name.setTextColor(ContextCompat.getColor(context, R.color.color_5));
        if (position == 0) {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(context.getString(R.string.message_all_survey_takers, users.size()));
            holder.name.setText(builder);
        } else {
            UserData user = users.get(position - 1);
            holder.name.setText(user.getName());
        }

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
