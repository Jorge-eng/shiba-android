package is.hello.shiba.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import is.hello.shiba.graph.presenters.ApiPresenter;

@Module(
        injects = {
                ApiPresenter.class
        },
        complete = false
)
@SuppressWarnings("UnusedDeclaration")
public class ApiModule {
    public static final int HTTP_CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    public static final int HTTP_READ_TIMEOUT_MILLIS = 20 * 1000; // 20s

    @Provides @Singleton ObjectMapper provideObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JodaModule());
        return mapper;
    }

    @Singleton @Provides OkHttpClient provideHttpClient() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(HTTP_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        return client;
    }
}
