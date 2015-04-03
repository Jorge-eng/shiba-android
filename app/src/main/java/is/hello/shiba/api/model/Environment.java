package is.hello.shiba.api.model;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class Environment {
    public static final Environment DEV = new Environment("Dev", "https://dev-api.hello.is/v1", "android_dev", "99999secret");

    public static List<Environment> getAll() {
        return Arrays.asList(DEV);
    }

    public final String name;
    public final String host;
    public final String clientId;
    public final String clientSecret;


    public Environment(@NonNull String name,
                       @NonNull String host,
                       @NonNull String clientId,
                       @NonNull String clientSecret) {
        this.name = name;
        this.host = host;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }


    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "Environment{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                '}';
    }
}
