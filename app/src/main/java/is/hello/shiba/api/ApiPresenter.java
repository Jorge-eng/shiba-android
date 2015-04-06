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
    public static final String PREF_ENVIRONMENT_HOST = "environment_host";
    public static final String PREF_ENVIRONMENT_CLIENT_ID = "environment_client_id";
    public static final String PREF_ENVIRONMENT_CLIENT_SECRET = "environment_client_secret";

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;
    private final SharedPreferences preferences;

    public final ReplaySubject<ApiService> service = ReplaySubject.createWithSize(1);
    public final ReplaySubject<Environment> environment = ReplaySubject.createWithSize(1);
    public final ReplaySubject<String> accessToken = ReplaySubject.createWithSize(1);

    @Inject ApiPresenter(@NonNull Context appContext,
                         @NonNull ObjectMapper objectMapper,
                         @NonNull OkHttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.preferences = appContext.getSharedPreferences(PREFS_NAME, 0);

        this.accessToken.onNext(retrieveAccessToken());
        pushEnvironment(getEnvironment());
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

    private void pushEnvironment(@NonNull Environment environment) {
        RestAdapter adapter = createRestAdapter(environment);
        ApiService service = adapter.create(ApiService.class);
        this.environment.onNext(environment);
        this.service.onNext(service);
    }

    public void setEnvironment(@NonNull Environment environment) {
        preferences.edit()
                   .putString(PREF_ENVIRONMENT_HOST, environment.host)
                   .putString(PREF_ENVIRONMENT_CLIENT_ID, environment.clientId)
                   .putString(PREF_ENVIRONMENT_CLIENT_SECRET, environment.clientSecret)
                   .apply();

        pushEnvironment(environment);
    }

    public Environment getEnvironment() {
        String host = preferences.getString(PREF_ENVIRONMENT_HOST, Environment.DEV.host);
        String clientId = preferences.getString(PREF_ENVIRONMENT_CLIENT_ID, Environment.DEV.clientId);
        String clientHost = preferences.getString(PREF_ENVIRONMENT_CLIENT_SECRET, Environment.DEV.clientSecret);
        return new Environment(host, clientId, clientHost);
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
