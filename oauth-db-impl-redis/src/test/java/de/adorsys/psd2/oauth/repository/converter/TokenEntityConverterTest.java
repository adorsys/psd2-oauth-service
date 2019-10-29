package de.adorsys.psd2.oauth.repository.converter;

import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;
import de.adorsys.psd2.oauth.repository.model.TokenEntity;
import de.adorsys.psd2.oauth.repository.model.TokenPO;

import java.io.IOException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TokenEntityConverterTest {

    private TokenEntityConverter converter;
    private TokenEntity entity;
    private TokenPO po;

    @Before
    public void setUp() throws Exception {
        converter = Mappers.getMapper(TokenEntityConverter.class);
        entity = readYml(TokenEntity.class, "token-entity.yml");
        po = readYml(TokenPO.class, "token-po.yml");
    }

    @Test
    public void toTokenPO() {
        TokenPO actual = converter.toTokenPO(entity);

        assertThat(actual, is(po));
    }

    @Test
    public void toTokenEntity() {
        TokenEntity actual = converter.toTokenEntity(po);

        assertThat(actual, is(entity));
    }

    private <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(getClass(), fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}