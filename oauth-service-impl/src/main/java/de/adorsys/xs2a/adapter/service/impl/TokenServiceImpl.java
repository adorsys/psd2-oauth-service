package de.adorsys.xs2a.adapter.service.impl;

import de.adorsys.xs2a.adapter.repository.TokenRepository;
import de.adorsys.xs2a.adapter.repository.exception.TokenNotFoundDBException;
import de.adorsys.xs2a.adapter.repository.model.TokenPO;
import de.adorsys.xs2a.adapter.service.TokenService;
import de.adorsys.xs2a.adapter.service.converter.TokenBOConverter;
import de.adorsys.xs2a.adapter.service.exception.TokenNotFoundServiceException;
import de.adorsys.xs2a.adapter.service.model.TokenBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    private final TokenRepository repository;
    private final TokenBOConverter converter;

    public TokenServiceImpl(TokenRepository repository, TokenBOConverter converter) {
        this.repository = repository;
        this.converter = converter;
    }

    @Override
    public TokenBO findById(String id) throws TokenNotFoundServiceException {
        logger.info("Get token by id={}", id);
        try {
            TokenPO tokenPO = repository.findById(id);
            return converter.toTokenBO(tokenPO);
        } catch (TokenNotFoundDBException e) {
            logger.error(e.getMessage(), e);
            throw new TokenNotFoundServiceException(e.getMessage());
        }
    }

    @Override
    public TokenBO save(TokenBO token) {
        logger.info("Trying to save token {}", token);
        TokenPO po = converter.toTokenPO(token);
        TokenPO saved = repository.save(po);
        return converter.toTokenBO(saved);
    }

    @Override
    public void delete(String id) {
        logger.info("Deleting token by id={}", id);
        repository.delete(id);
    }
}
