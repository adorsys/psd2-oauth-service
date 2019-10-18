/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.xs2a.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import de.adorsys.xs2a.adapter.config.BankConfig;
import de.adorsys.xs2a.adapter.exception.OAuthRestException;
import de.adorsys.xs2a.adapter.model.StateTO;
import de.adorsys.xs2a.adapter.rest.psd2.model.TokenResponseTO;
import de.adorsys.xs2a.adapter.service.RequestHeaders;
import de.adorsys.xs2a.adapter.service.TokenService;
import de.adorsys.xs2a.adapter.service.model.TokenBO;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthorizationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

    static final String AUTHORIZATION_CODE_ENDPOINT = "/oauth2/{bank}/authorization-code";
    static final String CODE_PARAMETER = "code";
    static final String STATE_PARAMETER = "state";
    static final String ERROR_PARAMETER = "error";
    static final String ERROR_DESCRIPTION_PARAMETER = "error_description";
    static final String ERROR_URI_PARAMETER = "error_uri";

    private String successUrl;
    private String errorUrl;
    private ObjectMapper objectMapper;
    private TokenService tokenService;
    private Oauth2Client oauth2Client;
    private BankConfig bankConfig;

    @Value("${oauth.redirect.success-url}")
    void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    @Value("${oauth.redirect.error-url}")
    void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    @Autowired
    public void setBankConfig(BankConfig bankConfig) {
        this.bankConfig = bankConfig;
    }

    @Autowired
    void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Autowired
    public void setOauth2Client(Oauth2Client oauth2Client) {
        this.oauth2Client = oauth2Client;
    }

    @GetMapping(value = AUTHORIZATION_CODE_ENDPOINT, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView getAuthorizationCode(
            @PathVariable("bank") String bank,
            @RequestParam(value = CODE_PARAMETER, required = false) String code,
            @RequestParam(STATE_PARAMETER) String state,
            @RequestParam(value = ERROR_PARAMETER, required = false) String error,
            @RequestParam(value = ERROR_DESCRIPTION_PARAMETER, required = false) String errorDescription,
            @RequestParam(value = ERROR_URI_PARAMETER, required = false) String errorUri,
            RedirectAttributes redirectAttributes
    ) {
        if (error == null || error.isEmpty()) {
            StateTO stateTO;
            TokenResponseTO token;
            try {
                stateTO = decodeState(state);

                token = getToken(bank, code, stateTO);
            } catch (OAuthRestException e) {
                return redirectToErrorPage(e, redirectAttributes);
            }

            tokenService.save(buildToken(token, stateTO));
            logger.info("Token successfully obtained and stored to db");
            logger.debug("Token could be retrieved by uri: /oauth2/tokens/{}", stateTO.getClientId());

            redirectAttributes.addFlashAttribute("clientId", stateTO.getClientId());
            return new RedirectView(successUrl);
        }

        return redirectToErrorPage(error, errorDescription, errorUri, redirectAttributes);
    }

    private TokenResponseTO getToken(String bank, String code, StateTO stateTO) throws OAuthRestException {
        Map<String, String> headers = buildHeaders(stateTO);
        logger.debug("Headers are {}", headers);

        Map<String, String> params = buildParams(bank, code);
        logger.debug("Parameters are {}", params);

        TokenResponseTO token;
        try {
            token = oauth2Client.getToken(headers, params);
            logger.debug("Access token is {}", token);
        } catch (IOException | FeignException e) {
            throw new OAuthRestException("xs2a-adapter_error", e.getMessage());
        }
        return token;
    }

    private RedirectView redirectToErrorPage(String error, String errorDescription, String errorUri, RedirectAttributes redirectAttributes) {
        logger.error("Error with code={} was appeared. Details: {}", error, errorDescription);
        redirectAttributes.addFlashAttribute("errorCode", error);
        redirectAttributes.addFlashAttribute("errorDescription", errorDescription);
        redirectAttributes.addFlashAttribute("errorUri", errorUri);
        return new RedirectView(errorUrl);
    }

    private RedirectView redirectToErrorPage(OAuthRestException e, RedirectAttributes redirectAttributes) {
        return redirectToErrorPage(e.getCode(), e.getDescription(), e.getUri(), redirectAttributes);
    }

    private StateTO decodeState(String state) throws OAuthRestException {
        logger.debug("Try to decode state={}", state);
        StateTO stateTO;
        try {
            byte[] bytes = Base64.getDecoder().decode(state.getBytes());
            stateTO = objectMapper.readValue(bytes, StateTO.class);
            logger.debug("Decoded state is {}", stateTO);
        } catch (IOException e) {
            throw new OAuthRestException("corrupted_state", "Could not decode state. Seems it was corrupted");
        }
        return stateTO;
    }

    private Map<String, String> buildParams(String bank, String code) throws OAuthRestException {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", bankConfig.getRedirectUri(bank));
        params.put("client_id", bankConfig.getClientId(bank));
        return params;
    }

    private Map<String, String> buildHeaders(StateTO stateTO) {
        Map<String, String> headers = new HashMap<>();
        headers.put(RequestHeaders.X_GTW_ASPSP_ID, stateTO.getAspspId());
        headers.put(RequestHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return headers;
    }

    private TokenBO buildToken(TokenResponseTO tokenResponse, StateTO state) {
        TokenBO tokenBO = new TokenBO();
        tokenBO.setId(state.getClientId());
        tokenBO.setAspspId(state.getAspspId());
        tokenBO.setTokenType(tokenResponse.getTokenType());
        tokenBO.setScope(tokenResponse.getScope());
        tokenBO.setAccessToken(tokenResponse.getAccessToken());
        tokenBO.setRefreshToken(tokenResponse.getRefreshToken());
        tokenBO.setExpiresInSeconds(tokenResponse.getExpiresInSeconds());
        return tokenBO;
    }
}
