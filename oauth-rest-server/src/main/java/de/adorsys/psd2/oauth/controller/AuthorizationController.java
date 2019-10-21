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

package de.adorsys.psd2.oauth.controller;

import de.adorsys.psd2.oauth.config.BankConfig;
import de.adorsys.psd2.oauth.exception.BankNotSupportedException;
import de.adorsys.psd2.oauth.service.TokenService;
import de.adorsys.psd2.oauth.service.exception.AuthCodeException;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class AuthorizationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

    static final String AUTHORIZATION_CODE_ENDPOINT = "/oauth2/{bank}/authorization-code";

    private String successUrl;
    private String errorUrl;
    private String serverUrl;
    private final TokenService tokenService;
    private final BankConfig bankConfig;

    public AuthorizationController(
            TokenService tokenService,
            BankConfig bankConfig,
            @Value("${oauth.redirect.success-url}") String successUrl,
            @Value("${oauth.redirect.error-url}") String errorUrl,
            @Value("${oauth.server.host}") String serverUrl) {
        this.tokenService = tokenService;
        this.bankConfig = bankConfig;
        this.successUrl = successUrl;
        this.errorUrl = errorUrl;
        this.serverUrl = serverUrl;
    }

    @GetMapping(value = AUTHORIZATION_CODE_ENDPOINT, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView getAuthorizationCode(
            @PathVariable("bank") String bank,
            AuthCode authCode,
            ErrorRequest errReq,
            RedirectAttributes redirectAttributes
    ) {
        if (errReq.error == null || errReq.error.isEmpty()) {
            TokenBO token;
            try {
                String redirectUri = buildRedirectUri(bank);
                String clientId = bankConfig.getClientId(bank);
                token = tokenService.exchangeAuthCode(authCode.code, redirectUri, clientId, authCode.state);
                tokenService.save(token);
            } catch (AuthCodeException e) {
                return redirectToErrorPage(e, redirectAttributes);
            }

            logger.info("Token successfully obtained and stored to db");
            logger.debug("Token could be retrieved by uri: /oauth2/tokens/{}", token.getId());

            redirectAttributes.addFlashAttribute("clientId", token.getId());
            return new RedirectView(successUrl);
        }

        return redirectToErrorPage(errReq.error, errReq.error_description, errReq.error_uri, redirectAttributes);
    }

    String buildRedirectUri(String bank) throws BankNotSupportedException {
        String redirectUri = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        return redirectUri + AUTHORIZATION_CODE_ENDPOINT.replace("{bank}", bank);
    }

    private RedirectView redirectToErrorPage(AuthCodeException e, RedirectAttributes redirectAttributes) {
        return redirectToErrorPage(e.getCode(), e.getMessage(), null, redirectAttributes);
    }

    private RedirectView redirectToErrorPage(String error, String errorDescription, String errorUri, RedirectAttributes redirectAttributes) {
        logger.error("Error with code={} was appeared. Details: {}", error, errorDescription);
        redirectAttributes.addFlashAttribute("errorCode", error);
        redirectAttributes.addFlashAttribute("errorDescription", errorDescription);
        redirectAttributes.addFlashAttribute("errorUri", errorUri);
        return new RedirectView(errorUrl);
    }

    private static class ErrorRequest {
        private String error;
        private String error_description;
        private String error_uri;


        public void setError(String error) {
            this.error = error;
        }

        public void setError_description(String errorDescription) {
            this.error_description = errorDescription;
        }

        public void setError_uri(String errorUri) {
            this.error_uri = errorUri;
        }
    }

    private static class AuthCode {
        private String code;
        private String state;

        public void setCode(String code) {
            this.code = code;
        }

        public void setState(String state) {
            this.state = state;
        }
    }

    void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
