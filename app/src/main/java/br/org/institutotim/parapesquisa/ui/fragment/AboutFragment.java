package br.org.institutotim.parapesquisa.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.ui.adapter.AboutPagerAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;

public class AboutFragment extends BaseFragment {

    @Bind(R.id.tabs)
    TabLayout tabs;
    @Bind(R.id.pager)
    ViewPager pager;

    @Inject
    ParaPesquisaOpenHelper mHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);
        getComponent().inject(this);

        pager.setAdapter(new AboutPagerAdapter(getChildFragmentManager(), mHelper.getAboutText()));
        tabs.setupWithViewPager(pager);

        return view;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }
}
