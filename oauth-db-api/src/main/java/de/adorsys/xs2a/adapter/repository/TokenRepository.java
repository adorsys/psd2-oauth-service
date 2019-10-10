package de.adorsys.xs2a.adapter.repository;


import de.adorsys.xs2a.adapter.repository.exception.TokenNotFoundDBException;
import de.adorsys.xs2a.adapter.repository.model.TokenPO;

public interface TokenRepository {
    TokenPO findById(String id) throws TokenNotFoundDBException;

    TokenPO save(TokenPO token);

    void delete(String id);
}