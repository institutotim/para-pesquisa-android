package br.org.institutotim.parapesquisa.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.org.institutotim.parapesquisa.data.model.UserForm;

public class UserFormAdapter extends BaseAdapter {

    private final Context context;
    private final List<UserForm> forms;

    public UserFormAdapter(Context context, List<UserForm> forms) {
        this.context = context;
        this.forms = forms;
    }

    @Override
    public int getCount() {
        return forms.size();
    }

    @Override
    public UserForm getItem(int position) {
        return forms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        ((TextView) convertView).setText(forms.get(position).getForm().getName());

        return convertView;
    }
}
