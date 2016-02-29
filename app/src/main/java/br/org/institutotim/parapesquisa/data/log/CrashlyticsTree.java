package br.org.institutotim.parapesquisa.data.log;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import timber.log.Timber;

public class CrashlyticsTree extends Timber.Tree {

    @Override
    public void i(String message, Object... args) {
        Crashlytics.log(String.format(message, args));
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        i(message, args);
    }

    @Override
    public void e(String message, Object... args) {
        i("ERROR: " + message, args);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        e(message, args);
        Crashlytics.logException(t);
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

        Crashlytics.log(priority, tag, message);

        if (t != null) {
            Crashlytics.logException(t);
        }
    }
}

