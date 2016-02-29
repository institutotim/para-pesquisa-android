package br.org.institutotim.parapesquisa.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.org.institutotim.parapesquisa.R;
import butterknife.Bind;
import butterknife.ButterKnife;

public class AgentHelpAdapter extends RecyclerView.Adapter<AgentHelpAdapter.ViewHolder> {

    private final Context context;
    String[] INDEX = {"1", "2", "3", "3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8", "3.9", "3.10", "4", "4.1", "4.2", "4.3", "4.4", "4.5", "5", "6", "7", "8"};
    Integer[] INT_INDEX = {1, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 13, 14, 15, 16, 17, 18, 19, 20, 21};

    public AgentHelpAdapter(Context context) {
        this.context = context;
    }

    public int getIndex(int position) {
        return INT_INDEX[position];
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_help_index, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (INDEX[position].contains(".")) {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.title.setVisibility(View.GONE);
            holder.subtitle.setText(context.getResources().getStringArray(R.array.help_index_survey_taker)[position]);
        } else {
            holder.subtitle.setVisibility(View.GONE);
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(context.getResources().getStringArray(R.array.help_index_survey_taker)[position]);
        }

        holder.index.setText(INDEX[position]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return INDEX.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.subtitle)
        TextView subtitle;
        @Bind(R.id.index)
        TextView index;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
