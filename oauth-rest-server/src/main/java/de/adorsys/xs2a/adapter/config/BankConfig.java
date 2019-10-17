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

package de.adorsys.xs2a.adapter.config;

import de.adorsys.xs2a.adapter.exception.OAuthRestException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "oauth")
public class BankConfig {

    public static final String CLIENT_ID = "client-id";
    public static final String REDIRECT_URI = "redirect-uri";
    Map<String, Map<String,String>> bankConfig;

    public Map<String, Map<String, String>> getBankConfig() {
        return bankConfig;
    }

    public void setBankConfig(Map<String, Map<String, String>> bankConfig) {
        this.bankConfig = bankConfig;
    }

    public String getClientId(String bank) throws OAuthRestException {
        isBankSupported(bank);
        return bankConfig.get(bank).get(CLIENT_ID);
    }

    public String getRedirectUri(String bank) throws OAuthRestException {
        isBankSupported(bank);
        return bankConfig.get(bank).get(REDIRECT_URI);
    }

    private void isBankSupported(String bank) throws OAuthRestException {
        if (!bankConfig.containsKey(bank)) {
            throw new OAuthRestException("bank_not_supported",bank+" is not supported by psd2-oauth service");
        }
    }
}
