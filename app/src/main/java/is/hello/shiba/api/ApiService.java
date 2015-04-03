package is.hello.shiba.api;

import android.support.annotation.NonNull;

import java.util.List;

import is.hello.shiba.api.model.Account;
import is.hello.shiba.api.model.Device;
import is.hello.shiba.api.model.OAuthCredentials;
import is.hello.shiba.api.model.OAuthSession;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

public interface ApiService {
    String DATE_FORMAT = "yyyy-MM-dd";

    @POST("/oauth2/token")
    Observable<OAuthSession> authorize(@NonNull @Body OAuthCredentials request);

    @GET("/account")
    Observable<Account> getAccount();

    @POST("/account")
    Observable<Account> createAccount(@NonNull @Body Account account);

    @PUT("/account")
    Observable<Account> updateAccount(@NonNull @Body Account account);

    @GET("/devices")
    Observable<List<Device>> registeredDevices();

    @DELETE("/devices/sense/{id}/all")
    Observable<Void> removeSenseAssociations(@Path("id") @NonNull String senseId);
}
