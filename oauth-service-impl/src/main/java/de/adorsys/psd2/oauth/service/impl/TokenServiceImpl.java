package de.adorsys.psd2.oauth.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.oauth.repository.TokenRepository;
import de.adorsys.psd2.oauth.repository.exception.TokenNotFoundDBException;
import de.adorsys.psd2.oauth.repository.model.TokenPO;
import de.adorsys.psd2.oauth.service.TokenService;
import de.adorsys.psd2.oauth.service.converter.TokenBOConverter;
import de.adorsys.psd2.oauth.service.exception.ExchangeCodeException;
import de.adorsys.psd2.oauth.service.exception.RefreshTokenException;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.OauthStateBO;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import de.adorsys.xs2a.adapter.api.TokenResponseTO;
import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import de.adorsys.xs2a.adapter.service.RequestHeaders;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.StringUtils.isEmpty;

@Service
public class TokenServiceImpl implements TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);
    private static final String STATE_REGEX = "[&?]state=([^&]*)";
    private static final Pattern STATE_PATTERN = Pattern.compile(STATE_REGEX);
    static final String STATE_PARAMETER = "&state=";

    private final TokenRepository repository;
    private TokenBOConverter converter;
    private Oauth2Client oauth2Client;
    private ObjectMapper objectMapper;
    @Value("${oauth.refresh-token-implicitly.enabled:false}")
    private boolean isRefreshTokenImplicitlyEnabled;
    @Value("${oauth.refresh-token-implicitly.seconds-before-expiration:30}")
    private long secondsBeforeExpiration;

    public TokenServiceImpl(TokenRepository repository, TokenBOConverter converter, Oauth2Client oauth2Client, ObjectMapper objectMapper) {
        this.repository = repository;
        this.converter = converter;
        this.oauth2Client = oauth2Client;
        this.objectMapper = objectMapper;
    }

    @Override
    public TokenBO exchangeAuthCode(String code, String redirectUri, String clientId, String state) throws ExchangeCodeException {
        logger.info("Exchange auth code by token");
        OauthStateBO stateObj = decodeState(state);

        Map<String, String> headers = buildHeaders(stateObj.getAspspId());
        Map<String, String> params = buildAuthCodeParams(code, redirectUri, clientId);

        TokenResponseTO token;
        try {
            token = oauth2Client.getToken(headers, params);
        } catch (IOException | FeignException e) {
            throw new ExchangeCodeException("xs2a-adapter_error", e.getMessage());
        }
        LocalDateTime expirationDate = LocalDateTime.now().plusSeconds(token.getExpiresInSeconds());
        return converter.toTokenBO(token, stateObj.getClientId(), stateObj.getAspspId(), expirationDate, clientId);
    }

    @Override
    public TokenBO refreshToken(TokenBO existingToken) throws RefreshTokenException {
        logger.info("Refresh token");

        Map<String, String> headers = buildHeaders(existingToken.getAspspId());
        Map<String, String> params = buildRefreshTokenParams(existingToken.getRefreshToken(), existingToken.getClientId());

        TokenResponseTO token;
        try {
            token = oauth2Client.getToken(headers, params);
        } catch (IOException | FeignException e) {
            throw new RefreshTokenException("xs2a-adapter_error", e.getMessage());
        }
        LocalDateTime expirationDate = LocalDateTime.now().plusSeconds(token.getExpiresInSeconds());
        return converter.toTokenBO(token, existingToken.getId(), existingToken.getAspspId(), expirationDate, existingToken.getClientId());
    }

    private Map<String, String> buildAuthCodeParams(String code, String redirectUri, String clientId) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", redirectUri);
        params.put("client_id", clientId);
        return params;
    }

    private Map<String, String> buildRefreshTokenParams(String refreshToken, String clientId) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);
        params.put("client_id", clientId);
        return params;
    }

    private Map<String, String> buildHeaders(String apspsId) {
        Map<String, String> headers = new HashMap<>();
        headers.put(RequestHeaders.X_GTW_ASPSP_ID, apspsId);
        headers.put(RequestHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return headers;
    }


    private OauthStateBO decodeState(String state) throws ExchangeCodeException {
        OauthStateBO stateObj;
        try {
            byte[] bytes = Base64.getDecoder().decode(state.getBytes());
            stateObj = objectMapper.readValue(bytes, OauthStateBO.class);
        } catch (IOException e) {
            throw new ExchangeCodeException("Could not decode state. Seems it was corrupted", "corrupted_state");
        }
        return stateObj;
    }

    @Override
    public TokenBO findById(String id) throws TokenNotFoundServiceException, RefreshTokenException {
        logger.info("Get token by id={}", id);
        try {
            TokenPO tokenPO = repository.findById(id);
            TokenBO existingToken = converter.toTokenBO(tokenPO);

            logger.info("Refresh token implicitly is {}", isRefreshTokenImplicitlyEnabled ? "enable" : "disable");
            if (isRefreshTokenImplicitlyEnabled) {
                String refreshToken = tokenPO.getRefreshToken();
                if (isNotBlank(refreshToken) && isReadyToRefresh(tokenPO)) {
                    TokenBO newToken = refreshToken(existingToken);
                    return save(newToken);
                }
            }

            return existingToken;
        } catch (TokenNotFoundDBException e) {
            throw new TokenNotFoundServiceException(e.getMessage());
        }
    }

    private boolean isReadyToRefresh(TokenPO tokenPO) {
        Optional<LocalDateTime> expirationDate = Optional.ofNullable(tokenPO.getExpirationDate());
        if (expirationDate.isPresent()) {
            LocalDateTime beforeExpirationTime = expirationDate.get().minusSeconds(secondsBeforeExpiration);
            return beforeExpirationTime.isBefore(LocalDateTime.now());
        }
        return true;
    }

    @Override
    public TokenBO save(TokenBO token) {
        logger.info("Trying to save token with id={}", token.getId());
        TokenPO po = converter.toTokenPO(token);
        TokenPO saved = repository.save(po);
        return converter.toTokenBO(saved);
    }

    @Override
    public String attachState(String scaOAuthLink, String aspspId, String psuId) {
        if (isEmpty(scaOAuthLink) || isEmpty(aspspId)) {
            throw new IllegalArgumentException("scaOAuth link and aspspId must be not empty");
        }
        if (isEmpty(psuId)) {
            psuId = UUID.randomUUID().toString();
        }

        OauthStateBO state = new OauthStateBO(psuId, aspspId);
        String encodedState = encodeState(state);

        Matcher matcher = STATE_PATTERN.matcher(scaOAuthLink);

        if (matcher.find()) {
            scaOAuthLink = replaceExistingState(encodedState, matcher);
        } else {
            scaOAuthLink += STATE_PARAMETER + encodedState;
        }

        return scaOAuthLink;
    }

    private String replaceExistingState(String encodedState, Matcher matcher) {
        StringBuffer sb = new StringBuffer();
        String quote = Pattern.quote(matcher.group(1));
        matcher.appendReplacement(sb, matcher.group(0).replaceFirst(quote, encodedState));
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public void delete(String id) {
        logger.info("Deleting token by id={}", id);
        repository.delete(id);
    }

    String encodeState(OauthStateBO state) {
        try {
            return Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(state).getBytes());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("State object couldn't be encoded");
        }
    }

    void setRefreshTokenImplicitlyEnabled(boolean refreshTokenImplicitlyEnabled) {
        isRefreshTokenImplicitlyEnabled = refreshTokenImplicitlyEnabled;
    }

    void setSecondsBeforeExpiration(long secondsBeforeExpiration) {
        this.secondsBeforeExpiration = secondsBeforeExpiration;
    }

    void setConverter(TokenBOConverter converter) {
        this.converter = converter;
    }
}
