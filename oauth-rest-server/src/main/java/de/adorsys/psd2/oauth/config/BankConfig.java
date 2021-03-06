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

package de.adorsys.psd2.oauth.config;

import de.adorsys.psd2.oauth.exception.BankNotSupportedException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "oauth")
public class BankConfig {

    Map<String, BankSettings> bankConfig;

    public Map<String, BankSettings> getBankConfig() {
        return bankConfig;
    }

    public void setBankConfig(Map<String, BankSettings> bankConfig) {
        this.bankConfig = bankConfig;
    }

    public String getClientId(String bank) throws BankNotSupportedException {
        checkBankSupported(bank);
        return bankConfig.get(bank).getClientId();
    }

    private void checkBankSupported(String bank) throws BankNotSupportedException {
        if (!isBankSupported(bank)) {
            throw new BankNotSupportedException(bank + " is not supported by psd2-oauth service", "bank_not_supported");
        }
    }

    public boolean isBankSupported(String bank) {
        return bankConfig.containsKey(bank);
    }
}
