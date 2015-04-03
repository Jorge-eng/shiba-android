package is.hello.shiba.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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
    public static final String PREFS_NAME = "Api";
    public static final String PREF_ACCESS_TOKEN = "access_token";

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;
    private final SharedPreferences preferences;

    private Environment environment;

    public final ReplaySubject<ApiService> service = ReplaySubject.createWithSize(1);
    public final ReplaySubject<String> accessToken = ReplaySubject.createWithSize(1);

    @Inject ApiPresenter(@NonNull Context appContext,
                         @NonNull ObjectMapper objectMapper,
                         @NonNull OkHttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.preferences = appContext.getSharedPreferences(PREFS_NAME, 0);

        setEnvironment(Environment.DEV);
    }


    //region Vending Api Services

    private RestAdapter createRestAdapter(@NonNull Environment environment) {
        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(new OkClient(httpClient));
        builder.setConverter(new JacksonConverter(objectMapper));
        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        builder.setEndpoint(environment.host);
        builder.setRequestInterceptor(request -> {
            String accessToken = retrieveAccessToken();
            if (!TextUtils.isEmpty(accessToken)) {
                request.addHeader("Authorization", "Bearer " + accessToken);
            }
        });
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


    //region Access Tokens

    public void clearAccessToken() {
        preferences.edit()
                .remove(PREF_ACCESS_TOKEN)
                .apply();

        this.accessToken.onNext(null);
    }

    public void storeAccessToken(@NonNull String accessToken) {
        preferences.edit()
                .putString(PREF_ACCESS_TOKEN, accessToken)
                .apply();
        this.accessToken.onNext(accessToken);
    }

    private String retrieveAccessToken() {
        return preferences.getString(PREF_ACCESS_TOKEN, null);
    }

    //endregion
}
