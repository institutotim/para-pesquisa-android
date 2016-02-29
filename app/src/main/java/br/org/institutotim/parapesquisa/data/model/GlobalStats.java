package br.org.institutotim.parapesquisa.data.model;

import java.io.Serializable;

public class GlobalStats implements Serializable {

    private int assignedToMe;
    private int surveyTakers;

    private int approved;
    private int pendingCorrection;
    private int pendingApproval;
    private int rescheduled;
    private int cancelled;
    private int totalGoal;
    private int remaining;

    public int getAssignedToMe() {
        return assignedToMe;
    }

    public void setAssignedToMe(int assignedToMe) {
        this.assignedToMe = assignedToMe;
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

    public int getTotalGoal() {
        return totalGoal;
    }

    public void setTotalGoal(int totalGoal) {
        this.totalGoal = totalGoal;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }
}

