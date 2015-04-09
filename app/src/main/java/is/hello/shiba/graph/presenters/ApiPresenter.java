package is.hello.shiba.graph.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.buruberi.bluetooth.logging.LoggerFacade;
import is.hello.shiba.api.ApiService;
import is.hello.shiba.api.model.Environment;
import is.hello.shiba.ui.util.Optional;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;
import rx.subjects.ReplaySubject;

@Singleton public class ApiPresenter implements RestAdapter.Log, RequestInterceptor {
    public static final String PREFS_NAME = "Api";
    public static final String PREF_ACCESS_TOKEN = "access_token";
    public static final String PREF_ENVIRONMENT_HOST = "environment_host";
    public static final String PREF_ENVIRONMENT_CLIENT_ID = "environment_client_id";
    public static final String PREF_ENVIRONMENT_CLIENT_SECRET = "environment_client_secret";

    private static final String RETROFIT_LOG_TAG = "Retrofit";

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;
    private final LoggerFacade logger;
    private final SharedPreferences preferences;

    public final ReplaySubject<ApiService> service = ReplaySubject.createWithSize(1);
    public final ReplaySubject<Environment> environment = ReplaySubject.createWithSize(1);
    public final ReplaySubject<Optional<String>> accessToken = ReplaySubject.createWithSize(1);

    @Inject ApiPresenter(@NonNull Context appContext,
                         @NonNull ObjectMapper objectMapper,
                         @NonNull OkHttpClient httpClient,
                         @NonNull LoggerFacade logger) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.logger = logger;
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
        builder.setLog(this);
        builder.setEndpoint(environment.host);
        builder.setRequestInterceptor(this);
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

        this.accessToken.onNext(Optional.empty());
    }

    public void storeAccessToken(@NonNull String accessToken) {
        preferences.edit()
                .putString(PREF_ACCESS_TOKEN, accessToken)
                .apply();
        this.accessToken.onNext(Optional.of(accessToken));
    }

    private Optional<String> retrieveAccessToken() {
        return Optional.ofNullable(preferences.getString(PREF_ACCESS_TOKEN, null));
    }

    //endregion


    //region Retrofit

    @Override
    public void log(String message) {
        logger.info(RETROFIT_LOG_TAG, message);
    }

    @Override
    public void intercept(RequestFacade request) {
        Optional<String> accessToken = retrieveAccessToken();
        accessToken.ifPresent(value -> request.addHeader("Authorization", "Bearer " + accessToken));
    }

    //endregion
}
