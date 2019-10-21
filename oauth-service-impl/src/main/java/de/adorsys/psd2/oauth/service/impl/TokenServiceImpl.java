package de.adorsys.psd2.oauth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.oauth.repository.TokenRepository;
import de.adorsys.psd2.oauth.repository.exception.TokenNotFoundDBException;
import de.adorsys.psd2.oauth.repository.model.TokenPO;
import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import de.adorsys.xs2a.adapter.api.TokenResponseTO;
import de.adorsys.xs2a.adapter.service.RequestHeaders;
import de.adorsys.psd2.oauth.service.TokenService;
import de.adorsys.psd2.oauth.service.converter.TokenBOConverter;
import de.adorsys.psd2.oauth.service.exception.ExchangeAuthCodeException;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

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
        State stateObj = decodeState(state);

        Map<String, String> headers = buildHeaders(stateObj);
        Map<String, String> params = buildParams(code, redirectUri, clientId);

        TokenResponseTO token;
        try {
            token = oauth2Client.getToken(headers, params);
        } catch (IOException | FeignException e) {
            throw new ExchangeAuthCodeException("xs2a-adapter_error", e.getMessage());
        }
        return converter.toTokenBO(token, stateObj.clientId, stateObj.aspspId);
    }

    private Map<String, String> buildParams(String code, String redirectUri, String clientId) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", redirectUri);
        params.put("client_id", clientId);
        return params;
    }

    private Map<String, String> buildHeaders(State state) {
        Map<String, String> headers = new HashMap<>();
        headers.put(RequestHeaders.X_GTW_ASPSP_ID, state.getAspspId());
        headers.put(RequestHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return headers;
    }


    private State decodeState(String state) throws ExchangeAuthCodeException {
        State stateObj;
        try {
            byte[] bytes = Base64.getDecoder().decode(state.getBytes());
            stateObj = objectMapper.readValue(bytes, State.class);
        } catch (IOException e) {
            throw new ExchangeAuthCodeException("corrupted_state", "Could not decode state. Seems it was corrupted");
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
    public void delete(String id) {
        logger.info("Deleting token by id={}", id);
        repository.delete(id);
    }

    private static class State {
        private String clientId;
        private String aspspId;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getAspspId() {
            return aspspId;
        }

        public void setAspspId(String aspspId) {
            this.aspspId = aspspId;
        }

        @Override
        public String toString() {
            return "State{" +
                           "clientId='" + clientId + '\'' +
                           ", aspspId='" + aspspId + '\'' +
                           '}';
        }
    }
}
