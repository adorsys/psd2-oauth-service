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

package de.adorsys.xs2a.adapter.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import de.adorsys.xs2a.adapter.config.BankConfig;
import de.adorsys.xs2a.adapter.model.StateTO;
import de.adorsys.xs2a.adapter.rest.psd2.model.TokenResponseTO;
import de.adorsys.xs2a.adapter.service.RequestHeaders;
import de.adorsys.xs2a.adapter.service.TokenService;
import de.adorsys.xs2a.adapter.service.model.TokenBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthorizationController {

    static final String AUTHORIZATION_CODE_ENDPOINT = "/{bank}/authorization-code";
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
            @RequestParam(value = ERROR_URI_PARAMETER, required = false) String errorUri
    ) throws IOException {
        String redirectUrl = errorUrl;

        if (error == null || error.isEmpty()) {
            redirectUrl = successUrl;

            StateTO stateTO = decodeState(state);

            TokenResponseTO token = oauth2Client.getToken(buildHeaders(stateTO), buildParams(bank, code));

            tokenService.save(buildToken(token, stateTO));
        }

        return new RedirectView(redirectUrl);
    }

    private StateTO decodeState(String state) {
        StateTO stateTO;
        try {
            byte[] bytes = Base64.getDecoder().decode(state.getBytes());
            stateTO = objectMapper.readValue(bytes, StateTO.class);
        } catch (IOException e) {
//            todo: replace with custom exception
            throw new IllegalStateException();
        }
        return stateTO;
    }

    private Map<String, String> buildParams(String bank, String code) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("authorization_code", code);
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
