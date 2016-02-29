package br.org.institutotim.parapesquisa.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.ui.fragment.SubmissionsFragment;

public class SubmissionsPagerAdapter extends FragmentStatePagerAdapter {

    private final Integer[] TITLES = {R.string.tab_all_submissions,
            R.string.tab_pending_correction, R.string.tab_rescheduled,
            R.string.tab_cancelled, R.string.tab_pending_approval,
            R.string.tab_approved, R.string.tab_waiting_sync};

    private Context context;
    private long formId;
    private final UserData user;
    private final String filter;

    public SubmissionsPagerAdapter(Context context, FragmentManager fm, long formId, String filter) {
        super(fm);
        this.context = context;
        this.formId = formId;
        this.filter = filter;
        this.user = null;
    }

    public SubmissionsPagerAdapter(Context context, FragmentManager fm, long formId, UserData user) {
        super(fm);
        this.context = context;
        this.formId = formId;
        this.user = user;
        this.filter = null;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return context.getString(TITLES[position]);
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putLong(SubmissionsFragment.FORM_EXTRA, formId);
        if (filter != null) bundle.putString(SubmissionsFragment.TEXT_EXTRA, filter);
        if (user != null) bundle.putParcelable(SubmissionsFragment.USER_EXTRA, user);

        switch (position) {
            case 1:
                bundle.putInt(SubmissionsFragment.FILTER_EXTRA, SubmissionStatus.WAITING_CORRECTION.ordinal());
                break;
            case 2:
                bundle.putInt(SubmissionsFragment.FILTER_EXTRA, SubmissionStatus.RESCHEDULED.ordinal());
                break;
            case 3:
                bundle.putInt(SubmissionsFragment.FILTER_EXTRA, SubmissionStatus.CANCELLED.ordinal());
                break;
            case 4:
                bundle.putInt(SubmissionsFragment.FILTER_EXTRA, SubmissionStatus.WAITING_APPROVAL.ordinal());
                break;
            case 5:
                bundle.putInt(SubmissionsFragment.FILTER_EXTRA, SubmissionStatus.APPROVED.ordinal());
                break;
            case 6:
                bundle.putInt(SubmissionsFragment.FILTER_EXTRA, SubmissionStatus.WAITING_SYNC.ordinal());
                break;
        }

        Fragment fragment = new SubmissionsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
