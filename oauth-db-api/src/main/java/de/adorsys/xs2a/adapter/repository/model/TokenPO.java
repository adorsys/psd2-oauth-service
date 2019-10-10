package de.adorsys.xs2a.adapter.repository.model;


import java.util.Objects;

public class TokenPO {
    private String id;
    private String accessToken;
    private String tokenType;
    private Long expiresInSeconds;
    private String refreshToken;
    private String scope;
    private String adapterId;

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

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
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
                       Objects.equals(refreshToken, tokenPO.refreshToken) &&
                       Objects.equals(scope, tokenPO.scope) &&
                       Objects.equals(adapterId, tokenPO.adapterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accessToken, tokenType, expiresInSeconds, refreshToken, scope, adapterId);
    }

    @Override
    public String toString() {
        return "TokenPO{" +
                       "id=" + id +
                       ", accessToken='" + accessToken + '\'' +
                       ", tokenType='" + tokenType + '\'' +
                       ", expiresInSeconds=" + expiresInSeconds +
                       ", refreshToken='" + refreshToken + '\'' +
                       ", scope='" + scope + '\'' +
                       ", adapterId='" + adapterId + '\'' +
                       '}';
    }
}
