package de.adorsys.psd2.oauth.repository.impl;

import de.adorsys.psd2.oauth.repository.converter.TokenEntityConverter;
import de.adorsys.psd2.oauth.repository.exception.TokenNotFoundDBException;
import de.adorsys.psd2.oauth.repository.model.TokenEntity;
import de.adorsys.psd2.oauth.repository.model.TokenPO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TokenRepositoryImplTest {
    private static final String TOKEN_ID = "d766b4c7-a940-446d-9bd4-af70eacf9772";

    private TokenRepositoryImpl repository;


    @Mock
    private RedisTemplate<String, TokenEntity> redisTemplate;

    private TokenEntityConverter converter;

    private TokenEntity entity;
    @Mock
    private HashOperations hashOperations;
    private TokenPO po;

    @Before
    public void setUp() {
        converter = Mappers.getMapper(TokenEntityConverter.class);
        entity = readYml(TokenEntity.class, "token-entity.yml");
        po = readYml(TokenPO.class, "token-po.yml");

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        repository = new TokenRepositoryImpl(converter, redisTemplate);

    }

    @Test
    public void findById() throws TokenNotFoundDBException {

        when(hashOperations.get(TokenRepositoryImpl.KEY, TOKEN_ID)).thenReturn(entity);

        TokenPO actual = repository.findById(TOKEN_ID);

        assertThat(actual, is(po));

        verify(hashOperations, times(1)).get(TokenRepositoryImpl.KEY, TOKEN_ID);
    }

    @Test(expected = TokenNotFoundDBException.class)
    public void findByIdNotFoundException() throws TokenNotFoundDBException {

        when(hashOperations.get(TokenRepositoryImpl.KEY, TOKEN_ID)).thenReturn(null);

        repository.findById(TOKEN_ID);
    }

    @Test
    public void save() {

        doNothing().when(hashOperations).put(TokenRepositoryImpl.KEY, TOKEN_ID, entity);

        TokenPO actual = repository.save(po);

        assertThat(actual, is(po));

        verify(hashOperations, times(1)).put(TokenRepositoryImpl.KEY, TOKEN_ID, entity);
    }

    @Test
    public void delete() {

       when(hashOperations.delete(TokenRepositoryImpl.KEY, TOKEN_ID)).thenReturn(new Random().nextLong());

        repository.delete(TOKEN_ID);

        verify(hashOperations, times(1)).delete(TokenRepositoryImpl.KEY, TOKEN_ID);
    }

    private <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(TokenEntityConverter.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}