package br.org.institutotim.parapesquisa;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ParaPesquisaModule {

    private final ParaPesquisaApp app;

    public ParaPesquisaModule(ParaPesquisaApp app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return app;
    }
}
