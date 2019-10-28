package de.adorsys.psd2.oauth.repository.model;


import java.time.LocalDateTime;
import java.util.Objects;

public class TokenPO {
    private String id;
    private String accessToken;
    private String tokenType;
    private Long expiresInSeconds;
    private LocalDateTime expirationDate;
    private String refreshToken;
    private String scope;
    private String aspspId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAspspId() {
        return aspspId;
    }

    public void setAspspId(String aspspId) {
        this.aspspId = aspspId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenPO tokenPO = (TokenPO) o;
        return Objects.equals(id, tokenPO.id) &&
                       Objects.equals(accessToken, tokenPO.accessToken) &&
                       Objects.equals(tokenType, tokenPO.tokenType) &&
                       Objects.equals(expiresInSeconds, tokenPO.expiresInSeconds) &&
                       Objects.equals(expirationDate, tokenPO.expirationDate) &&
                       Objects.equals(refreshToken, tokenPO.refreshToken) &&
                       Objects.equals(scope, tokenPO.scope) &&
                       Objects.equals(aspspId, tokenPO.aspspId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accessToken, tokenType, expiresInSeconds, expirationDate, refreshToken, scope, aspspId);
    }

    @Override
    public String toString() {
        return "TokenPO{" +
                       "id='" + id + '\'' +
                       ", accessToken='" + accessToken + '\'' +
                       ", tokenType='" + tokenType + '\'' +
                       ", expiresInSeconds=" + expiresInSeconds +
                       ", expirationDate=" + expirationDate +
                       ", refreshToken='" + refreshToken + '\'' +
                       ", scope='" + scope + '\'' +
                       ", aspspId='" + aspspId + '\'' +
                       '}';
    }
}
