package de.adorsys.xs2a.adapter.model;


import java.util.Objects;
import java.util.UUID;

public class TokenTO {
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
        TokenTO tokenTO = (TokenTO) o;
        return Objects.equals(id, tokenTO.id) &&
                       Objects.equals(accessToken, tokenTO.accessToken) &&
                       Objects.equals(tokenType, tokenTO.tokenType) &&
                       Objects.equals(expiresInSeconds, tokenTO.expiresInSeconds) &&
                       Objects.equals(refreshToken, tokenTO.refreshToken) &&
                       Objects.equals(scope, tokenTO.scope) &&
                       Objects.equals(adapterId, tokenTO.adapterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accessToken, tokenType, expiresInSeconds, refreshToken, scope, adapterId);
    }

    @Override
    public String toString() {
        return "TokenTO{" +
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
