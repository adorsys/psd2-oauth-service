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

import de.adorsys.psd2.oauth.service.TokenService;
import de.adorsys.psd2.oauth.service.exception.RefreshTokenException;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Profile("dev")
@Controller
public class ViewController {

    private final TokenService tokenService;

    public ViewController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/success")
    public String successPage(Model model) throws TokenNotFoundServiceException, RefreshTokenException {
        String clientId = (String) model.asMap().get("clientId");
        TokenBO token = tokenService.findById(clientId);
        model.addAttribute("token", token.getAccessToken());
        return "success";
    }

    @GetMapping("/error")
    public String errorPage() {
        return "error";
    }

}
