package br.org.institutotim.parapesquisa.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.org.institutotim.parapesquisa.R;
import butterknife.Bind;
import butterknife.ButterKnife;

public class ExtraDataAdapter extends RecyclerView.Adapter<ExtraDataAdapter.ViewHolder> {

    private List<List<String>> items;

    public ExtraDataAdapter(List<List<String>> items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_extra_data, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        List<String> item = items.get(position);
        holder.label.setText(item.get(0));
        holder.content.setText(item.get(1));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.label)
        TextView label;
        @Bind(R.id.content)
        TextView content;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
