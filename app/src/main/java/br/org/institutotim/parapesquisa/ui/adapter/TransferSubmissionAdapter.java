package br.org.institutotim.parapesquisa.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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

public class TransferSubmissionAdapter extends BaseAdapter {

    private final Context context;
    private List<UserData> users;

    public TransferSubmissionAdapter(Context context, List<UserData> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public UserData getItem(int position) {
        return users.get(position);
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
        UserData user = users.get(position);
        holder.name.setText(user.getName());

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
