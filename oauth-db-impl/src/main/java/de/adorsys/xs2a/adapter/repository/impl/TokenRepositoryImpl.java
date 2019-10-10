package de.adorsys.xs2a.adapter.repository.impl;

import de.adorsys.xs2a.adapter.repository.TokenRepository;
import de.adorsys.xs2a.adapter.repository.converter.TokenEntityConverter;
import de.adorsys.xs2a.adapter.repository.exception.TokenNotFoundDBException;
import de.adorsys.xs2a.adapter.repository.model.TokenEntity;
import de.adorsys.xs2a.adapter.repository.model.TokenPO;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TokenRepositoryImpl implements TokenRepository {
    static final String KEY = "tokenKey";

    private final TokenEntityConverter converter;

    private final HashOperations<String, String, TokenEntity> hashOperations;

    public TokenRepositoryImpl(TokenEntityConverter converter, RedisTemplate<String, TokenEntity> redisTemplate) {
        this.converter = converter;
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public TokenPO findById(String id) throws TokenNotFoundDBException {
        TokenEntity tokenEntity = hashOperations.get(KEY, id);
        Optional<TokenEntity> optional = Optional.ofNullable(tokenEntity);
        return optional
                       .map(converter::toTokenPO)
                       .orElseThrow(() -> new TokenNotFoundDBException("Token with id=" + id + " not found"));
    }

    @Override
    public TokenPO save(TokenPO token) {
        TokenEntity entity = converter.toTokenEntity(token);
        hashOperations.put(KEY, token.getId(), entity);
        return token;
    }

    @Override
    public void delete(String id) {
        hashOperations.delete(KEY, id);
    }
}
