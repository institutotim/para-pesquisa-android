package br.org.institutotim.parapesquisa.data.model;

public class Stats {

    private int remainingDays;
    private int surveyTakers;

    private int approved;
    private int pendingCorrection;
    private int pendingApproval;
    private int rescheduled;
    private int cancelled;

    private int goal;
    private int remaining;

    public int getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(int remainingDays) {
        this.remainingDays = remainingDays;

        if (this.remainingDays < 0) this.remainingDays = 0;
    }

    public int getSurveyTakers() {
        return surveyTakers;
    }

    public void setSurveyTakers(int surveyTakers) {
        this.surveyTakers = surveyTakers;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public int getPendingCorrection() {
        return pendingCorrection;
    }

    public void setPendingCorrection(int pendingCorrection) {
        this.pendingCorrection = pendingCorrection;
    }

    public int getPendingApproval() {
        return pendingApproval;
    }

    public void setPendingApproval(int pendingApproval) {
        this.pendingApproval = pendingApproval;
    }

    public int getRescheduled() {
        return rescheduled;
    }

    public void setRescheduled(int rescheduled) {
        this.rescheduled = rescheduled;
    }

    public int getCancelled() {
        return cancelled;
    }

    public void setCancelled(int cancelled) {
        this.cancelled = cancelled;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }
}
