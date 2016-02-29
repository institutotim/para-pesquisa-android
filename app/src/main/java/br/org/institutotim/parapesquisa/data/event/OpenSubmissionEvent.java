package br.org.institutotim.parapesquisa.data.event;

import br.org.institutotim.parapesquisa.data.model.UserSubmission;

/**
 * Created by tpinho on 1/7/16.
 */
public class OpenSubmissionEvent {

    private final UserSubmission submission;

    public OpenSubmissionEvent(UserSubmission submission) {
        this.submission = submission;
    }

    public UserSubmission getSubmission() {
        return submission;
    }

}
