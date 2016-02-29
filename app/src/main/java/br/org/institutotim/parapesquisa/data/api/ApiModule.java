package br.org.institutotim.parapesquisa.data.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import br.org.institutotim.parapesquisa.BuildConfig;
import br.org.institutotim.parapesquisa.data.annotation.ApiUrl;
import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;

@Module
public class ApiModule {

    public static final int API_VERSION = 2;

    @Provides
    @Singleton
    public Endpoint provideEndpoint(@ApiUrl String url) {
        return Endpoints.newFixedEndpoint(url);
    }

    @Provides
    @Singleton
    public RestAdapter provideRestAdapter(Endpoint endpoint, OkHttpClient client,
                                          ObjectMapper objectMapper,
                                          ApiInterceptor interceptor) {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setClient(new OkClient(client))
                .setRequestInterceptor(interceptor)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setConverter(new JacksonConverter(objectMapper))
                .build();
    }

    @Provides
    @Singleton
    public ParaPesquisaApi provideParaPesquisaApi(RestAdapter adapter) {
        return adapter.create(ParaPesquisaApi.class);
    }
}
