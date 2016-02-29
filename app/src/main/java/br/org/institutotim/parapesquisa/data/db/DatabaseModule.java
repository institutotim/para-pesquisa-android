package br.org.institutotim.parapesquisa.data.db;

import android.app.Application;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseModule {

    @Provides
    @Singleton
    public ParaPesquisaOpenHelper provideParaPesquisaOpenHelper(Application app, ObjectMapper objectMapper,
                                                                ParaPesquisaPreferences preferences) {
        return new ParaPesquisaOpenHelper(app, objectMapper, preferences);
    }

    @Provides
    @Singleton
    public ParaPesquisaPreferences provideParaPesquisaPreferences(SharedPreferences preferences,
                                                                  ObjectMapper objectMapper) {
        return new ParaPesquisaPreferences(preferences, objectMapper);
    }
}
