package de.adorsys.xs2a.adapter.service;


import de.adorsys.xs2a.adapter.service.exception.ExchangeAuthCodeException;
import de.adorsys.xs2a.adapter.service.exception.TokenNotFoundServiceException;
import de.adorsys.xs2a.adapter.service.model.TokenBO;

public interface TokenService {

    TokenBO exchangeAuthCode(String code, String redirectUri, String clientId, String state) throws ExchangeAuthCodeException;

    TokenBO findById(String id) throws TokenNotFoundServiceException;

    TokenBO save(TokenBO token);

    void delete(String id);
}