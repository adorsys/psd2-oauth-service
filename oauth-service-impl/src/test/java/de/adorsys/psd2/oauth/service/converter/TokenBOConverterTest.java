package de.adorsys.psd2.oauth.service.converter;

import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;
import de.adorsys.psd2.oauth.repository.model.TokenPO;
import de.adorsys.psd2.oauth.service.model.TokenBO;

import java.io.IOException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TokenBOConverterTest {

    private TokenBOConverter converter;
    private TokenBO bo;
    private TokenPO po;

    @Before
    public void setUp() {
        converter = Mappers.getMapper(TokenBOConverter.class);
        bo = readYml(TokenBO.class, "token-bo.yml");
        po = readYml(TokenPO.class, "token-po.yml");
    }


    @Test
    public void toTokenBO() {
        TokenBO actual = converter.toTokenBO(po);

        assertThat(actual, is(bo));
    }

    @Test
    public void toTokenPO() {
        TokenPO actual = converter.toTokenPO(bo);

        assertThat(actual, is(po));
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