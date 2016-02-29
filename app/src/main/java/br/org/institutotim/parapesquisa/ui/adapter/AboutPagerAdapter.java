package br.org.institutotim.parapesquisa.ui.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import br.org.institutotim.parapesquisa.data.model.AboutText;
import br.org.institutotim.parapesquisa.ui.fragment.WebViewFragment;

public class AboutPagerAdapter extends FragmentStatePagerAdapter {

    private List<AboutText> texts;

    public AboutPagerAdapter(FragmentManager fm, List<AboutText> texts) {
        super(fm);
        this.texts = texts;
    }

    @Override
    public Fragment getItem(int position) {
        AboutText text = texts.get(position);
        Fragment fragment = new WebViewFragment();
        Bundle params = new Bundle();
        params.putParcelable(WebViewFragment.TEXT_EXTRA, text);
        fragment.setArguments(params);
        return fragment;
    }

    @Override
    public int getCount() {
        return texts.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return texts.get(position).getTitle();
    }
}
