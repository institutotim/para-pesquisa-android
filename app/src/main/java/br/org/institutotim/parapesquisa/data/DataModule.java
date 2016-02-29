package br.org.institutotim.parapesquisa.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import android.support.annotation.Nullable;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jakewharton.byteunits.DecimalByteUnit;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import br.org.institutotim.parapesquisa.data.annotation.ApiUrl;
import br.org.institutotim.parapesquisa.data.api.ApiModule;
import br.org.institutotim.parapesquisa.data.db.DatabaseModule;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.serializer.DateDeserializer;
import br.org.institutotim.parapesquisa.data.serializer.DateSerializer;
import dagger.Module;
import dagger.Provides;

@Module(includes = {DatabaseModule.class, ApiModule.class})
public class DataModule {

    private static final int DISK_CACHE_SIZE = (int) DecimalByteUnit.MEGABYTES.toBytes(50);

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule dateModule = new SimpleModule();
        dateModule.addSerializer(new DateSerializer());
        dateModule.addDeserializer(DateTime.class, new DateDeserializer());
        objectMapper.registerModule(dateModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Provides
    public UserData provideUserData(ParaPesquisaPreferences preferences) {
        return preferences.getUser();
    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences(Application app) {
        return app.getSharedPreferences("parapesquisa", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    @ApiUrl
    public String provideApiUrl(ParaPesquisaPreferences preferences) {
        String apiUrl = preferences.getServerUrl();
        if (apiUrl == null) {
            return "http://placeholder";
        }
        return apiUrl.startsWith("http") ? apiUrl : "http://" + apiUrl;
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(Application app) {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setConnectTimeout(10, TimeUnit.SECONDS);

        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
        client.setCache(cache);

        return client;
    }
}
