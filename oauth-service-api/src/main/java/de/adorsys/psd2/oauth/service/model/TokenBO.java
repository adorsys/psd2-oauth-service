package de.adorsys.psd2.oauth.service.model;


import java.time.LocalDateTime;
import java.util.Objects;

public class TokenBO {
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
        TokenBO tokenBO = (TokenBO) o;
        return Objects.equals(id, tokenBO.id) &&
                       Objects.equals(accessToken, tokenBO.accessToken) &&
                       Objects.equals(tokenType, tokenBO.tokenType) &&
                       Objects.equals(expiresInSeconds, tokenBO.expiresInSeconds) &&
                       Objects.equals(expirationDate, tokenBO.expirationDate) &&
                       Objects.equals(refreshToken, tokenBO.refreshToken) &&
                       Objects.equals(scope, tokenBO.scope) &&
                       Objects.equals(aspspId, tokenBO.aspspId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accessToken, tokenType, expiresInSeconds, expirationDate, refreshToken, scope, aspspId);
    }

    @Override
    public String toString() {
        return "TokenBO{" +
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
