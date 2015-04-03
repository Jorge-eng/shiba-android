package is.hello.shiba.api;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.shiba.api.model.Environment;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;
import rx.subjects.ReplaySubject;

@Singleton public class ApiPresenter {
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    private Environment environment;

    public final ReplaySubject<ApiService> service = ReplaySubject.createWithSize(1);

    @Inject ApiPresenter(@NonNull ObjectMapper objectMapper,
                         @NonNull OkHttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;

        setEnvironment(Environment.DEV);
    }


    //region Vending Api Services

    private RestAdapter createRestAdapter(@NonNull Environment environment) {
        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(new OkClient(httpClient));
        builder.setConverter(new JacksonConverter(objectMapper));
        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        builder.setEndpoint(environment.host);
        return builder.build();
    }

    public void setEnvironment(@NonNull Environment environment) {
        if (environment == this.environment) {
            return;
        }

        this.environment = environment;

        RestAdapter adapter = createRestAdapter(environment);
        ApiService service = adapter.create(ApiService.class);
        this.service.onNext(service);
    }

    //endregion
}
