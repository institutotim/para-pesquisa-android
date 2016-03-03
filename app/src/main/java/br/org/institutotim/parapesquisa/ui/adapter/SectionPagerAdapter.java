package br.org.institutotim.parapesquisa.ui.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.widget.SectionView;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import static java.util.Collections.sort;

public class SectionPagerAdapter extends PagerAdapter {


    final private List<Section> sections;
    final private UserSubmission mSubmission;
    final private boolean disable;
    final private boolean correction;
    final private boolean moderator;

    private Map<Section, List<Answer>> mData = new ArrayMap<>();
    private Map<Section, List<SubmissionCorrection>> corrections = new ArrayMap<>();

    private SectionPagerAdapter(FormData form, @Nullable UserSubmission submission, boolean disable, boolean correction, boolean moderator) {
        this.sections = form.getSections();
        this.disable = disable;
        this.correction = correction;
        this.moderator = moderator;
        sort(sections, (lhs, rhs) -> {
            if (lhs.getOrder() == null || rhs.getOrder() == null) return 0;
            if (lhs.getOrder() < rhs.getOrder()) return -1;
            if (lhs.getOrder() > rhs.getOrder()) return 1;
            return 0;
        });
        this.mSubmission = submission;
        setupAnswers();
    }

    public static SectionPagerAdapter builderSectionPagerDefault(FormData form, @Nullable UserSubmission submission) {
        return new SectionPagerAdapter(form, submission, false, false, false);
    }

    public static SectionPagerAdapter builderSectionDisabled(FormData form, @Nullable UserSubmission submission) {
        return new SectionPagerAdapter(form, submission, true, false, false);
    }

    public static SectionPagerAdapter builderSectionForCorrection(FormData form, @Nullable UserSubmission submission) {
        return new SectionPagerAdapter(form, submission, false, true, false);
    }

    public static SectionPagerAdapter builderSectionForModerator(FormData form, @Nullable UserSubmission submission) {
        return new SectionPagerAdapter(form, submission, true, false, true);
    }

    private void setupAnswers() {
        if (mSubmission == null || mSubmission.getAnswers() == null || mSubmission.getAnswers().isEmpty())
            return;

        List<Answer> answers;
        for (int i = 0; i < sections.size(); i++) {
            final Section section = sections.get(i);
            answers = new ArrayList<>();

            for (int y = 0; y < section.getFields().size(); y++) {
                final Field field = section.getFields().get(y);
                final Answer answer = getAnswerByField(mSubmission.getAnswers(), field);

                if (answer != null) {
                    answers.add(answer);
                }
            }

            if (!answers.isEmpty())
                mData.put(section, answers);
        }
    }

    private Answer getAnswerByField(List<Answer> answers, Field field) {
        for (int i = 0; i < answers.size(); i++) {
            final Answer answer = answers.get(i);
            if (answer != null) {
                if (answer.getFieldId() == field.getId()) {
                    return answer;
                }
            }
        }
        return null;
    }


    @Override
    public int getCount() {
        return sections.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Section section = sections.get(position);

        SectionView sectionView = instantiateItem(container, section, position);
        sectionView.setTag("page" + position);

        container.addView(sectionView);

        try {
            EventBus.getDefault().register(sectionView);
        } catch (Exception e) {
            Timber.d("This class doesn't have onEvent() methods");
        }
        return sectionView;
    }

    private SectionView instantiateItem(ViewGroup container, Section section, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        SectionView sectionView = (SectionView) inflater.inflate(R.layout.item_section_page, container, false);
        sectionView.setData(section, mSubmission, mData.get(section), position, disable, correction, moderator, corrections.get(section));
        if (EventBus.getDefault().isRegistered(sectionView)) {
            EventBus.getDefault().unregister(sectionView);
        }
        return sectionView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        SectionView view = (SectionView) object;
        mData.put(view.getSection(), view.getAnswers());
        corrections.put(view.getSection(), view.getCorrections());
        container.removeView(view);
    }

    public boolean validateCurrentPage(ViewPager pager) {
        SectionView view = getSectionViewByPosition(pager, pager.getCurrentItem());
        return view.validate();
    }

    public void refreshCurrentSection(ViewPager pager, Field field) {
        ((SectionView) pager.findViewWithTag("page" + pager.getCurrentItem())).refreshDataExceptField(field);
    }

    public void scrollToField(ViewPager pager, Field field) {
        ((SectionView) pager.findViewWithTag("page" + pager.getCurrentItem())).scrollToField(field);
    }

    private SectionView getSectionViewByPosition(ViewPager pager, int position) {
        return (SectionView) pager.findViewWithTag("page" + position);
    }

    public List<Answer> getAnswers(ViewPager viewPager) {
        List<Answer> answers = new ArrayList<>();

        int size = getCount();
        for (int i = 0; i < size; i++) {
            SectionView sectionView = getSectionViewByPosition(viewPager, i);

            if (sectionView == null) {
                final Section section = sections.get(i);
                sectionView = instantiateItem(viewPager, section, i);
            }

            if (sectionView != null) {
                answers.addAll(sectionView.getAnswers());
            }
        }

        return answers;
    }

    public List<SubmissionCorrection> getCorrections(ViewPager viewPager) {
        List<SubmissionCorrection> corrections = new ArrayList<>();

        int size = getCount();
        for (int i = 0; i < size; i++) {
            SectionView sectionView = getSectionViewByPosition(viewPager, i);

            if (sectionView == null) {
                final Section section = sections.get(i);
                sectionView = instantiateItem(viewPager, section, i);
            }

            if (sectionView != null) {
                corrections.addAll(sectionView.getCorrections());
            }
        }

        return corrections;
    }

    public boolean isValidSections(ViewPager viewPager) {
        int size = getCount();
        for (int i = 0; i < size; i++) {
            SectionView sectionView = getSectionViewByPosition(viewPager, i);

            if (sectionView == null) {
                final Section section = sections.get(i);
                sectionView = instantiateItem(viewPager, section, i);
            }

            if (sectionView != null && !sectionView.validate())
                return false;
        }

        return true;
    }

}
