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

public class ModeratorHelpAdapter extends RecyclerView.Adapter<ModeratorHelpAdapter.ViewHolder> {

    private final Context context;
    String[] INDEX = {"1", "2", "2.1", "2.2", "2.3", "2.4", "3", "4", "4.1", "4.2", "4.3", "4.4", "4.5", "4.6", "4.7", "5", "6", "6.1", "6.2", "6.3", "6.4", "6.5", "6.6", "6.7", "7", "8", "9", "10"};
    Integer[] INT_INDEX = {1, 2, 2, 3, 4, 5, 6, 7, 7, 8, 9, 10, 11, 12, 13, 14, 15, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};

    public ModeratorHelpAdapter(Context context) {
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
            holder.subtitle.setText(context.getResources().getStringArray(R.array.help_index_moderator)[position]);
        } else {
            holder.subtitle.setVisibility(View.GONE);
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(context.getResources().getStringArray(R.array.help_index_moderator)[position]);
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
