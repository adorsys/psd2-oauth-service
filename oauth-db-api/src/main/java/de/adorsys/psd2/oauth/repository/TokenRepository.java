package de.adorsys.psd2.oauth.repository;


import de.adorsys.psd2.oauth.repository.exception.TokenNotFoundDBException;
import de.adorsys.psd2.oauth.repository.model.TokenPO;

public interface TokenRepository {
    TokenPO findById(String id) throws TokenNotFoundDBException;

    TokenPO save(TokenPO token);

    void delete(String id);
}