package br.org.institutotim.parapesquisa.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.event.SyncCompletedEvent;
import br.org.institutotim.parapesquisa.data.model.UserForm;
import br.org.institutotim.parapesquisa.ui.activity.AgentSubmissionsActivity;
import br.org.institutotim.parapesquisa.ui.adapter.FormAdapter;
import br.org.institutotim.parapesquisa.util.ItemClickSupport;
import butterknife.Bind;
import butterknife.ButterKnife;

public class AgentFormsFragment extends BaseFragment implements ItemClickSupport.OnItemClickListener {

    @Bind(R.id.list)
    RecyclerView mRecyclerView;

    @Inject
    FormAdapter mAdapter;
    @Inject
    ParaPesquisaOpenHelper mHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moderator_forms, container, false);
        ButterKnife.bind(this, view);
        getComponent().inject(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        ItemClickSupport click = ItemClickSupport.addTo(mRecyclerView);
        click.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(() -> {
            List<UserForm> forms = mHelper.getUserForms();
            if (isAdded()) {
                getActivity().runOnUiThread(() -> mAdapter.setData(forms));
            }
        }).start();
    }

    @Override
    public void onItemClick(RecyclerView parent, View view, int position, long id) {
        Intent intent = new Intent(getContext(), AgentSubmissionsActivity.class);
        intent.putExtra(AgentSubmissionsActivity.USER_FORM, mAdapter.getItem(position));
        startActivity(intent);
    }

    public void onEventMainThread(SyncCompletedEvent event) {
        if (event.getResult().equals(SyncCompletedEvent.Result.SUCCESS)) {
            new Thread(() -> {
                List<UserForm> forms = mHelper.getUserForms();
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> mAdapter.setData(forms));
                }
            }).start();
        }
    }
}
