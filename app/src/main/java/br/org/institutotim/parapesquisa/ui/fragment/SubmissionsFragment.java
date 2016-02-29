package br.org.institutotim.parapesquisa.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.event.ContinueSurveyEvent;
import br.org.institutotim.parapesquisa.data.event.OpenSubmissionEvent;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserRole;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.activity.AgentSubmissionCorrectionActivity;
import br.org.institutotim.parapesquisa.ui.activity.BaseActivity;
import br.org.institutotim.parapesquisa.ui.activity.ModeratorSubmissionApprovalActivity;
import br.org.institutotim.parapesquisa.ui.activity.SubmissionViewActivity;
import br.org.institutotim.parapesquisa.ui.adapter.AgentSubmissionAdapter;
import br.org.institutotim.parapesquisa.ui.adapter.ModeratorSubmissionAdapter;
import br.org.institutotim.parapesquisa.ui.decorator.DividerItemDecoration;
import br.org.institutotim.parapesquisa.ui.listener.EndlessRecyclerOnScrollListener;
import br.org.institutotim.parapesquisa.util.ItemClickSupport;
import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import de.greenrobot.event.EventBus;

public class SubmissionsFragment extends BaseFragment implements ItemClickSupport.OnItemClickListener {

    @Bind(android.R.id.list)
    RecyclerView mList;
    @Bind(R.id.progress)
    ProgressBar mProgress;

    private long formId;

    private UserData mUser;

    @Inject
    ParaPesquisaOpenHelper mHelper;
    @Inject
    ParaPesquisaPreferences mPreferences;

    int count = 0;

    @Inject
    Lazy<ModeratorSubmissionAdapter> mModeratorAdapter;
    @Inject
    Lazy<AgentSubmissionAdapter> mAgentAdapter;
    private SubmissionStatus status;
    private UserData userData;
    private String text;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_submissions, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mList.setLayoutManager(layoutManager);
        mList.addItemDecoration(new DividerItemDecoration(getContext()));
        mList.setItemAnimator(new DefaultItemAnimator());
        ItemClickSupport click = ItemClickSupport.addTo(mList);
        click.setOnItemClickListener(this);
        mList.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                if (count == -1) {
                    hideLoading();
                    return;
                }

                new Thread(() -> {
                    if (!SubmissionStatus.WAITING_SYNC.equals(status)) {
                        List<UserSubmission> submissions = getSubmissionsWithIdentifier(status, userData);
                        if (count == -1 || submissions.isEmpty()) {
                            hideLoading();
                            //return;
                        }
                        updateAdapter(submissions);
                    } else {
                        hideLoading();
                    }
                }).start();
            }
        });

        mUser = mPreferences.getUser();

        setupData();
    }

    private void updateAdapter(List<UserSubmission> submissions) {
        if (!isAdded()) {
            return;
        }

        if (submissions.isEmpty()) return;

        if (mUser != null && mUser.getRole().equals(UserRole.AGENT)) {
            getActivity().runOnUiThread(() -> mAgentAdapter.get().addData(submissions));
        } else {
            getActivity().runOnUiThread(() -> mModeratorAdapter.get().addData(submissions));
        }
    }

    private void hideLoading() {
        if (!isAdded()) {
            return;
        }

        if (mUser != null && mUser.getRole().equals(UserRole.AGENT)) {
            getActivity().runOnUiThread(() -> mAgentAdapter.get().hideLoading());
        } else {
            getActivity().runOnUiThread(() -> mModeratorAdapter.get().hideLoading());
        }
    }

    private void setupData() {
        new Thread(() -> {
            formId = getArguments().getLong(FORM_EXTRA);

            int statusOrdinal = getArguments().getInt(FILTER_EXTRA, -1);
            status = statusOrdinal != -1 ? SubmissionStatus.values()[statusOrdinal] : null;
            text = getArguments().getString(TEXT_EXTRA, null);
            if (text != null) text = text.toLowerCase();
            userData = getArguments().getParcelable(USER_EXTRA);

            final List<UserSubmission> submissions = getSubmissionsWithIdentifier(status, userData);

            if (isAdded()) {
                if (mUser != null && mUser.getRole().equals(UserRole.AGENT)) {
                    getActivity().runOnUiThread(() -> {
                        mProgress.setVisibility(View.GONE);
                        mList.setVisibility(View.VISIBLE);
                        mList.setAdapter(mAgentAdapter.get());
                        mAgentAdapter.get().setData(submissions);
                        if (count == -1) mAgentAdapter.get().hideLoading();
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        mProgress.setVisibility(View.GONE);
                        mList.setVisibility(View.VISIBLE);
                        mList.setAdapter(mModeratorAdapter.get());
                        mModeratorAdapter.get().setData(submissions);
                        if (count == -1) mModeratorAdapter.get().hideLoading();
                    });
                }
            }
        }).start();
    }

    private List<UserSubmission> getSubmissionsWithIdentifier(SubmissionStatus status, UserData userData) {
        List<UserSubmission> submissions = getSubmissions(status, userData);
        Field field = mHelper.getIdentifierField(formId);
        if (field == null) return filterById(submissions);

        List<UserSubmission> filtered = new ArrayList<>();
        for (int i = 0; i < submissions.size(); i++) {
            UserSubmission submission = submissions.get(i);
            filtered.add(submission.setIdentifier(field));
        }
        return filterByIdentifier(filtered);
    }

    private List<UserSubmission> filterByIdentifier(List<UserSubmission> submissions) {
        if (text == null) return submissions;

        List<UserSubmission> filtered = new ArrayList<>();
        for (int i = 0; i < submissions.size(); i++) {
            String identifier = submissions.get(i).getIdentifier();
            if ((identifier != null && identifier.toLowerCase().contains(text)) ||
                    String.valueOf(submissions.get(i).getId()).contains(text)) {
                filtered.add(submissions.get(i));
            }
        }
        return filtered;
    }

    private List<UserSubmission> filterById(List<UserSubmission> submissions) {
        if (text == null) return submissions;

        List<UserSubmission> filtered = new ArrayList<>();
        for (int i = 0; i < submissions.size(); i++) {
            if (String.valueOf(submissions.get(i).getId()).contains(text)) {
                filtered.add(submissions.get(i));
            }
        }
        return filtered;
    }

    private List<UserSubmission> getSubmissions(SubmissionStatus status, UserData userData) {
        if (count == -1) return Collections.emptyList();

        List<UserSubmission> submissions;
        if (status != null && SubmissionStatus.WAITING_SYNC.equals(status)) {
            submissions = mHelper.getPendingSubmissions(formId);
        } else {
            submissions = mHelper.getFilteredSubmissions(formId, status, userData, count);
            count += submissions.size();

            if (submissions.isEmpty() || submissions.size() < 25) {
                count = -1;
                hideLoading();
            }

            submissions.addAll(0, mHelper.getPendingSubmissions(formId, status));

            if (status == null) {
                UserSubmission inProgress = mHelper.getSubmissionInProgress(formId);
                if (inProgress != null) submissions.add(0, inProgress);
            }
        }

        if (mUser == null || mUser.getRole().equals(UserRole.AGENT)) {
            return submissions;
        }

        List<UserSubmission> allPending = new ArrayList<>(submissions);
        if (SubmissionStatus.APPROVED.equals(status) || status == null) {
            allPending.addAll(0, mHelper.getApprovedSubmissions());
        }
        if (SubmissionStatus.WAITING_SYNC.equals(status) || status == null) {
            allPending.addAll(0, mHelper.getRepprovedSubmissions());
            allPending.addAll(0, mHelper.getApprovedSubmissions());
        }

        return allPending;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onItemClick(RecyclerView parent, View view, int position, long id) {
        if (isAdded() && position != -1) {
            if (mUser != null && mUser.getRole().equals(UserRole.AGENT)) {
                UserSubmission submission = mAgentAdapter.get().getItem(position);
                if (submission.getInProgress() != null && submission.getInProgress()) {
                    EventBus.getDefault().post(new ContinueSurveyEvent());
                } else if (SubmissionStatus.RESCHEDULED.equals(submission.getStatus())) {
                    EventBus.getDefault().post(new OpenSubmissionEvent(submission));
                } else if (SubmissionStatus.WAITING_CORRECTION.equals(submission.getStatus())) {
                    Intent intent = new Intent(getActivity(), AgentSubmissionCorrectionActivity.class);
                    intent.putExtra(BaseActivity.SUBMISSION_EXTRA, submission);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), SubmissionViewActivity.class);
                    intent.putExtra(BaseActivity.SUBMISSION_EXTRA, submission);
                    intent.putExtra(BaseActivity.FORM_EXTRA, formId);
                    startActivity(intent);
                }
            } else {
                UserSubmission submission = mModeratorAdapter.get().getItem(position);
                Intent intent = new Intent(getContext(), SubmissionStatus.WAITING_APPROVAL.equals(submission.getStatus()) ?
                        ModeratorSubmissionApprovalActivity.class : SubmissionViewActivity.class);
                intent.putExtra(BaseActivity.SUBMISSION_EXTRA, submission);
                intent.putExtra(BaseActivity.FORM_EXTRA, formId);
                startActivity(intent);
            }
        }
    }
}
