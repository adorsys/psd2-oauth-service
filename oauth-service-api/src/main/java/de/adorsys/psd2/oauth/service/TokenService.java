package de.adorsys.psd2.oauth.service;


import de.adorsys.psd2.oauth.service.exception.ExchangeAuthCodeException;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.TokenBO;

public interface TokenService {

    TokenBO exchangeAuthCode(String code, String redirectUri, String clientId, String state) throws ExchangeAuthCodeException;

    TokenBO findById(String id) throws TokenNotFoundServiceException;

    TokenBO save(TokenBO token);

    void delete(String id);
}