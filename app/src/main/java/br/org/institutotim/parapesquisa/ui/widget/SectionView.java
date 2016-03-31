package br.org.institutotim.parapesquisa.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.event.RefreshFieldEvent;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.activity.AgentFormActivity;
import br.org.institutotim.parapesquisa.ui.activity.AgentSubmissionCorrectionActivity;
import br.org.institutotim.parapesquisa.ui.activity.BaseSubmissionViewActivity;
import br.org.institutotim.parapesquisa.ui.activity.ModeratorSubmissionApprovalActivity;
import br.org.institutotim.parapesquisa.ui.adapter.FieldAdapter;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import static java.util.Collections.sort;

public class SectionView extends FrameLayout {

    private Section mSection;
    private FieldAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private TextView mSectionName;
    private TextView mSectionNumber;

    public SectionView(Context context) {
        super(context);
        init();
    }

    public SectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SectionView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_section, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSectionName = (TextView) findViewById(R.id.section_name);
        mSectionNumber = (TextView) findViewById(R.id.section_number);

        mRecyclerView.setLayoutManager(new WrapperLinearLayoutManager(getContext()));
    }

    public void setData(Section section, UserSubmission submission, @Nullable List<Answer> answers, int position, boolean disable, boolean correction, boolean moderator, List<SubmissionCorrection> corrections) {
        this.mSection = section;
        this.mSectionName.setText(section.getName());
        this.mSectionNumber.setText(String.valueOf(position + 1));
        this.mAdapter = new FieldAdapter(mSection, submission, answers, disable, correction, moderator, corrections);
        mRecyclerView.setAdapter(this.mAdapter);
    }

    public Section getSection() {
        return mSection;
    }

    public List<Answer> getAnswers() {
        return mAdapter.getAnswers();
    }

    public List<SubmissionCorrection> getCorrections() {
        return mAdapter.getCorrections();
    }

    public boolean validate() {
        int position = mAdapter.validate();
        if (position != -1) {
            mRecyclerView.smoothScrollToPosition(position);
        }
        return position == -1;
    }

    public void refreshDataExceptField(Field field) {
        mAdapter.refreshDataExceptField(field);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final RefreshFieldEvent event) {
        mAdapter.notifyDataSetChanged();
    }

    public static class MyLinearLayoutManager extends WrapperLinearLayoutManager {

        public MyLinearLayoutManager(Context context, int orientation, boolean reverseLayout)    {
            super(context, orientation, reverseLayout);
        }

        private int[] mMeasuredDimension = new int[2];

        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                              int widthSpec, int heightSpec) {
            final int widthMode = View.MeasureSpec.getMode(widthSpec);
            final int heightMode = View.MeasureSpec.getMode(heightSpec);
            final int widthSize = View.MeasureSpec.getSize(widthSpec);
            final int heightSize = View.MeasureSpec.getSize(heightSpec);
            int width = 0;
            int height = 0;
            for (int i = 0; i < getItemCount(); i++) {
                measureScrapChild(recycler, i,
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        mMeasuredDimension);

                if (getOrientation() == HORIZONTAL) {
                    width = width + mMeasuredDimension[0];
                    if (i == 0) {
                        height = mMeasuredDimension[1];
                    }
                } else {
                    height = height + mMeasuredDimension[1];
                    if (i == 0) {
                        width = mMeasuredDimension[0];
                    }
                }
            }
            switch (widthMode) {
                case View.MeasureSpec.EXACTLY:
                    width = widthSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            switch (heightMode) {
                case View.MeasureSpec.EXACTLY:
                    height = heightSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            setMeasuredDimension(width, height);
        }

        private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                       int heightSpec, int[] measuredDimension) {
            View view = recycler.getViewForPosition(position);
            if (view != null) {
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
                int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                        getPaddingLeft() + getPaddingRight(), p.width);
                int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                        getPaddingTop() + getPaddingBottom(), p.height);
                view.measure(childWidthSpec, childHeightSpec);
                measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
                recycler.recycleView(view);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            Timber.d(e, "No onEvent() methods found");
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(BaseSubmissionViewActivity.RefreshFields refreshFields) {
        List<Field> fields = new ArrayList<>();
        for (int i = 0; i < mSection.getFields().size(); i++) {
            Field field = mSection.getFields().get(i);

            Boolean shouldDisable = BaseSubmissionViewActivity.getReadOnlyStatus(field.getId());

            if (!shouldDisable && !field.isReadOnly()) {
                fields.add(field);
            } else {
                mAdapter.removeAnswer(field);
                BaseSubmissionViewActivity.removeReadOnlyStatus(field.getId());
            }
        }
        sort(fields, (lhs, rhs) -> {
            if (lhs.getOrder() == null || rhs.getOrder() == null)
                return lhs.getId() < rhs.getId() ? -1 : 1;
            if (lhs.getOrder() < rhs.getOrder()) return -1;
            if (lhs.getOrder() > rhs.getOrder()) return 1;
            return 0;
        });
        mAdapter.setmFields(fields);
        mAdapter.refreshDataExceptField(refreshFields.getExceptField());
    }

    public void scrollToField(Field field) {
        int position = mAdapter.getFieldPosition(field);
        if (position > -1) mRecyclerView.smoothScrollToPosition(position);
    }
}
