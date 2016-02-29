package br.org.institutotim.parapesquisa.data.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

import java.util.List;

import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.SubmissionCorrection;
import br.org.institutotim.parapesquisa.data.model.SubmissionStatus;

public class SubmissionUpdate {

    private List<SubmissionCorrection> corrections;
    private List<Answer> answers;
    @JsonProperty("started_at")
    private DateTime startedAt;
    private SubmissionStatus status;

    public SubmissionUpdate(List<SubmissionCorrection> corrections) {
        this.corrections = corrections;
    }

    public SubmissionUpdate() {
    }

    public List<SubmissionCorrection> getCorrections() {
        return corrections;
    }

    public void setCorrections(List<SubmissionCorrection> corrections) {
        this.corrections = corrections;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public DateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(DateTime startedAt) {
        this.startedAt = startedAt;
    }

    public SubmissionStatus getStatus() { return status; }

    public void setStatus(SubmissionStatus status) { this.status = status; }
}
