package br.org.institutotim.parapesquisa.data.event;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import br.org.institutotim.parapesquisa.data.model.Notification;

public class SyncCompletedEvent {

    private final Result result;
    private final String message;
    private final List<Notification> notifications;

    public SyncCompletedEvent(Result result, @Nullable String message, List<Notification> notifications) {
        this.result = result;
        this.message = message;
        this.notifications = notifications;
    }

    public SyncCompletedEvent(Result result, @Nullable String message) {
        this(result, message, new ArrayList<>());
    }

    public SyncCompletedEvent(Result result) {
        this(result, null, new ArrayList<>());
    }

    public Result getResult() {
        return result;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public enum Result {
        SUCCESS, ERROR
    }
}
