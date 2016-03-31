package br.org.institutotim.parapesquisa.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserRole;
import br.org.institutotim.parapesquisa.ui.activity.HelpActivity;
import br.org.institutotim.parapesquisa.ui.adapter.AgentHelpAdapter;
import br.org.institutotim.parapesquisa.ui.adapter.ModeratorHelpAdapter;
import br.org.institutotim.parapesquisa.ui.widget.WrapperLinearLayoutManager;
import br.org.institutotim.parapesquisa.util.ItemClickSupport;
import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;

public class HelpFragment extends BaseFragment implements ItemClickSupport.OnItemClickListener {

    @Bind(R.id.list)
    RecyclerView mList;

    @Inject
    ParaPesquisaPreferences mPreferences;

    @Inject
    Lazy<AgentHelpAdapter> mAgentAdapter;
    @Inject
    Lazy<ModeratorHelpAdapter> mModeratorAdapter;

    private UserData mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        ButterKnife.bind(this, view);
        getComponent().inject(this);

        mList.setLayoutManager(new WrapperLinearLayoutManager(getActivity()));

        mUser = mPreferences.getUser();

        if (mUser != null && mUser.getRole().equals(UserRole.AGENT)) {
            mList.setAdapter(mAgentAdapter.get());
        } else {
            mList.setAdapter(mModeratorAdapter.get());
        }

        ItemClickSupport clickSupport = ItemClickSupport.addTo(mList);
        clickSupport.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onItemClick(RecyclerView parent, View view, int position, long id) {
        Intent intent = new Intent(getContext(), HelpActivity.class);
        int helpIndex;
        if (mUser != null && mUser.getRole().equals(UserRole.AGENT)) {
            helpIndex = mAgentAdapter.get().getIndex(position);
        } else {
            helpIndex = mModeratorAdapter.get().getIndex(position);
        }
        intent.putExtra(HelpActivity.HELP_INDEX, helpIndex);
        startActivity(intent);
    }
}
