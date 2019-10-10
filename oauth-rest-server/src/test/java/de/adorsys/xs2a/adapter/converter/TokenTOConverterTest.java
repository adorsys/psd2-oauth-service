package de.adorsys.xs2a.adapter.converter;

import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;
import de.adorsys.xs2a.adapter.model.TokenTO;
import de.adorsys.xs2a.adapter.service.model.TokenBO;

import java.io.IOException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TokenTOConverterTest {

    private TokenTOConverter converter;
    private TokenBO bo;
    private TokenTO to;

    @Before
    public void setUp() {
        converter = Mappers.getMapper(TokenTOConverter.class);
        bo = readYml(TokenBO.class, "token-bo.yml");
        to = readYml(TokenTO.class, "token-to.yml");
    }

    @Test
    public void toTokenTO() {
        TokenTO actual = converter.toTokenTO(bo);

        assertThat(actual, is(to));
    }

    @Test
    public void toTokenBO() {
        TokenBO actual = converter.toTokenBO(to);

        assertThat(actual, is(bo));
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