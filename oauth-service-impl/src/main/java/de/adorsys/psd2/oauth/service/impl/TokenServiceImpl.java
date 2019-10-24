package de.adorsys.psd2.oauth.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.oauth.repository.TokenRepository;
import de.adorsys.psd2.oauth.repository.exception.TokenNotFoundDBException;
import de.adorsys.psd2.oauth.repository.model.TokenPO;
import de.adorsys.psd2.oauth.service.TokenService;
import de.adorsys.psd2.oauth.service.converter.TokenBOConverter;
import de.adorsys.psd2.oauth.service.exception.ExchangeAuthCodeException;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.OauthStateBO;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import de.adorsys.xs2a.adapter.api.TokenResponseTO;
import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import de.adorsys.xs2a.adapter.service.RequestHeaders;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.isEmpty;

@Service
public class TokenServiceImpl implements TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);
    private static final String STATE_REGEX = "[&?]state=([^&]*)";
    private static final Pattern STATE_PATTERN = Pattern.compile(STATE_REGEX);
    static final String STATE_PARAMETER = "&state=";

    private final TokenRepository repository;
    private final TokenBOConverter converter;
    private Oauth2Client oauth2Client;
    private ObjectMapper objectMapper;

    public TokenServiceImpl(TokenRepository repository, TokenBOConverter converter, Oauth2Client oauth2Client, ObjectMapper objectMapper) {
        this.repository = repository;
        this.converter = converter;
        this.oauth2Client = oauth2Client;
        this.objectMapper = objectMapper;
    }

    @Override
    public TokenBO exchangeAuthCode(String code, String redirectUri, String clientId, String state) throws ExchangeAuthCodeException {
        logger.info("Exchange auth code by token");
        OauthStateBO stateObj = decodeState(state);

        Map<String, String> headers = buildHeaders(stateObj);
        Map<String, String> params = buildParams(code, redirectUri, clientId);

        TokenResponseTO token;
        try {
            token = oauth2Client.getToken(headers, params);
        } catch (IOException | FeignException e) {
            throw new ExchangeAuthCodeException("xs2a-adapter_error", e.getMessage());
        }
        return converter.toTokenBO(token, stateObj.getClientId(), stateObj.getAspspId());
    }

    private Map<String, String> buildParams(String code, String redirectUri, String clientId) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", redirectUri);
        params.put("client_id", clientId);
        return params;
    }

    private Map<String, String> buildHeaders(OauthStateBO state) {
        Map<String, String> headers = new HashMap<>();
        headers.put(RequestHeaders.X_GTW_ASPSP_ID, state.getAspspId());
        headers.put(RequestHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return headers;
    }


    private OauthStateBO decodeState(String state) throws ExchangeAuthCodeException {
        OauthStateBO stateObj;
        try {
            byte[] bytes = Base64.getDecoder().decode(state.getBytes());
            stateObj = objectMapper.readValue(bytes, OauthStateBO.class);
        } catch (IOException e) {
            throw new ExchangeAuthCodeException("Could not decode state. Seems it was corrupted", "corrupted_state");
        }
        return stateObj;
    }

    @Override
    public TokenBO findById(String id) throws TokenNotFoundServiceException {
        logger.info("Get token by id={}", id);
        try {
            TokenPO tokenPO = repository.findById(id);
            return converter.toTokenBO(tokenPO);
        } catch (TokenNotFoundDBException e) {
            throw new TokenNotFoundServiceException(e.getMessage());
        }
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
            StringBuffer sb = new StringBuffer();
            String quote = Pattern.quote(matcher.group(1));
            matcher.appendReplacement(sb, matcher.group(0).replaceFirst(quote, encodedState));
            matcher.appendTail(sb);
            scaOAuthLink = sb.toString();
        } else {
            scaOAuthLink += STATE_PARAMETER + encodedState;
        }

        return scaOAuthLink;
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
}
