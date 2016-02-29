package br.org.institutotim.parapesquisa;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.OkHttpClient;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.InputStream;
import java.util.Locale;

import javax.inject.Inject;

import br.org.institutotim.parapesquisa.data.log.CrashlyticsTree;
import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class ParaPesquisaApp extends Application {

    private ParaPesquisaComponent component;

    @Inject
    OkHttpClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("App", Locale.getDefault().getDisplayLanguage());

        setupComponent();

        EventBus.builder()
                .throwSubscriberException(false)
                .sendNoSubscriberEvent(false)
                .installDefaultEventBus();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Fabric.with(this, new Crashlytics());
            Timber.plant(new CrashlyticsTree());
        }

//        LeakCanary.install(this);
        JodaTimeAndroid.init(this);

        Glide.get(this).register(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(client));
    }

    public void setupComponent() {
        component = DaggerParaPesquisaComponent.builder()
                .paraPesquisaModule(new ParaPesquisaModule(this))
                .build();
        component.inject(this);
    }

    public ParaPesquisaComponent getComponent() {
        return component;
    }

    public static ParaPesquisaApp get(Context context) {
        return (ParaPesquisaApp) context.getApplicationContext();
    }
}
