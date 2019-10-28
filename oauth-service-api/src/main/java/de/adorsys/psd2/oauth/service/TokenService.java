package de.adorsys.psd2.oauth.service;


import de.adorsys.psd2.oauth.service.exception.ExchangeCodeException;
import de.adorsys.psd2.oauth.service.exception.RefreshTokenException;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.TokenBO;

public interface TokenService {

    TokenBO exchangeAuthCode(String code, String redirectUri, String clientId, String state) throws ExchangeCodeException;

    TokenBO refreshToken(TokenBO existingToken) throws RefreshTokenException;

    TokenBO findById(String id) throws TokenNotFoundServiceException, RefreshTokenException;

    TokenBO save(TokenBO token);

    String attachState(String scaOAuthLink, String aspspId, String psuId);

    void delete(String id);
}
