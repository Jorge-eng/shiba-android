package is.hello.shiba.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthSession {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("account_id")
    private String accountId;


    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccountId() {
        return accountId;
    }

    @Override
    public String toString() {
        return "OAuthSession{" +
                "accessToken='" + accessToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", refreshToken='" + refreshToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", accountId='" + accountId + '\'' +
                '}';
    }
}
