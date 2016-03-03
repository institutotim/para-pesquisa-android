package br.org.institutotim.parapesquisa.ui.activity;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.FormData;
import br.org.institutotim.parapesquisa.data.model.Section;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import br.org.institutotim.parapesquisa.ui.helper.RecyclerViewHelper;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * Created by Renan on 01/03/2016.
 */
public abstract class BaseSubmissionViewActivity extends BaseActivity {
    protected UserSubmission mSubmission;
    protected FormData mForm;
    private static Map<Long, List<Pair<Long, Boolean>>> fieldReadOnlyStatus = new HashMap<>();

    public static Boolean getReadOnlyStatus(final Long fieldId) {
        final List<Pair<Long, Boolean>> pairs = fieldReadOnlyStatus.get(fieldId);

        if (pairs == null || pairs.isEmpty())
            return false;

        for (Pair<Long, Boolean> pair : pairs) {
            if (pair != null && pair.second) {
                return true;
            }
        }

        return false;
    }

    protected void handleReadOnlyStatus() {
        final long start = System.currentTimeMillis();
        fieldReadOnlyStatus.clear();

        if (mSubmission != null) {
            for (int i = 0; i < mForm.getSections().size(); i++) {
                final Section section = mForm.getSections().get(i);
                for (int y = 0; y < section.getFields().size(); y++) {
                    final Field field = section.getFields().get(y);
                    if (!field.isReadOnly()) {
                        final Answer answer = mSubmission.getAnswerForField(field.getId());
                        if (answer != null) {
                            final RecyclerViewHelper.ActionWrapper actionWrapper = RecyclerViewHelper.processActions(field, answer);

                            if (!actionWrapper.getDisable().isEmpty()
                                    || !actionWrapper.getEnable().isEmpty()
                                    || !actionWrapper.getDisableSections().isEmpty()
                                    || !actionWrapper.getEnableSections().isEmpty()) {
                                processActionWrapper(actionWrapper, false);
                            }
                        }
                    }
                }
            }
        }
        Timber.d("Process actions: %s milliseconds", TimeUnit.MILLISECONDS.convert(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS));
    }

    @SuppressWarnings("unused")
    public void onEvent(RecyclerViewHelper.ActionWrapper event) {
        processActionWrapper(event, true);
    }

    private void processActionWrapper(RecyclerViewHelper.ActionWrapper event, boolean refresh) {
        final long fieldIdAction = event.getField().getId();
        changeFieldVisibility(fieldIdAction, event.getDisable(), event.getDisableReadOnly());
        changeFieldVisibility(fieldIdAction, event.getEnable(), event.getEnableReadOnly());
        changeSectionVisibility(fieldIdAction, event.getDisableSections(), event.getDisableSectionReadOnly());
        changeSectionVisibility(fieldIdAction, event.getEnableSections(), !event.getDisableSectionReadOnly());

        if (refresh)
            EventBus.getDefault().post(new RefreshFields(event.getField()));
    }

    public static class RefreshFields {
        private final Field exceptField;

        public RefreshFields(Field exceptField) {
            this.exceptField = exceptField;
        }

        public Field getExceptField() {
            return exceptField;
        }
    }

    private void changeFieldVisibility(long fieldIdAction, List<Long> ids, boolean readOnly) {
        final List<Section> sections = mForm.getSections();
        for (int i = 0; i < sections.size(); i++) {
            final Section section = sections.get(i);
            for (int y = 0; y < section.getFields().size(); y++) {
                final Field field = section.getFields().get(y);
                if (ids.contains(field.getId())) {
                    addReadOnlyStatus(fieldIdAction, field.getId(), readOnly);
                }
            }
        }
    }

    private void changeSectionVisibility(long fieldIdAction, List<Long> ids, boolean readOnly) {
        final List<Section> sections = mForm.getSections();
        for (int i = 0; i < sections.size(); i++) {
            final Section section = sections.get(i);
            if (ids.contains(section.getId())) {
                for (int y = 0; y < section.getFields().size(); y++) {
                    final Field field = section.getFields().get(y);
                    addReadOnlyStatus(fieldIdAction, field.getId(), readOnly);
                }
            }
        }
    }

    private void addReadOnlyStatus(long fieldIdAction, long fieldId, boolean readOnly) {
        List<Pair<Long, Boolean>> pairs = fieldReadOnlyStatus.get(fieldId);

        if (pairs == null) pairs = new ArrayList<>();

        int position = getPairPosition(pairs, fieldIdAction);

        Pair<Long, Boolean> pair = new Pair<>(fieldIdAction, readOnly);

        if (position == -1) {
            pairs.add(pair);
        } else {
            pairs.set(position, pair);
        }

        fieldReadOnlyStatus.put(fieldId, pairs);
    }

    private int getPairPosition(List<Pair<Long, Boolean>> pairs, long id) {
        for (int i = 0; i < pairs.size(); i++) {
            Pair<Long, Boolean> pair = pairs.get(i);
            if (pair.first.equals(id)) return i;
        }

        return -1;
    }
}
