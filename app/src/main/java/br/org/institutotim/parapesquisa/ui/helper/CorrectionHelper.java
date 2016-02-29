package br.org.institutotim.parapesquisa.ui.helper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.org.institutotim.parapesquisa.data.model.Field;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;

public class CorrectionHelper {

    public static Set<Long> getModeratorIds(UserSubmission submission) {
        Set<Long> ids = new HashSet<>();

        if (submission.getCorrections() != null) {
            for (int i = 0; i < submission.getCorrections().size(); i++) {
                ids.add(submission.getCorrections().get(i).getUserId());
            }
        }

        return ids;
    }

    public static Set<Long> getModeratorsIds(List<UserSubmission> submissions) {
        Set<Long> ids = new HashSet<>();

        for (int i = 0; i < submissions.size(); i++) {
            ids.addAll(getModeratorIds(submissions.get(i)));
        }

        return ids;
    }

    public boolean needsCorrection(Field field, List<SubmissionCorrection> corrections) {
        if (corrections == null) return false;

        for (int i = 0; i < corrections.size(); i++) {
            if (corrections.get(i).getFieldId() == field.getId()) {
                return true;
            }
        }
        return false;
    }
}
