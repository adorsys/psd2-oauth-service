package de.adorsys.xs2a.adapter.service;


import de.adorsys.xs2a.adapter.service.exception.TokenNotFoundServiceException;
import de.adorsys.xs2a.adapter.service.model.TokenBO;

import java.util.UUID;

public interface TokenService {
    TokenBO findById(String id) throws TokenNotFoundServiceException;

    TokenBO save(TokenBO token);

    void delete(String id);
}