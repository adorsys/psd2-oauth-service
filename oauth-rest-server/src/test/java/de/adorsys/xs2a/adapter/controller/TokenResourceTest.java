package de.adorsys.xs2a.adapter.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pro.javatar.commons.reader.JsonReader;
import pro.javatar.commons.reader.YamlReader;
import de.adorsys.xs2a.adapter.converter.TokenTOConverter;
import de.adorsys.xs2a.adapter.model.TokenTO;
import de.adorsys.xs2a.adapter.exception.ExceptionAdvisor;
import de.adorsys.xs2a.adapter.service.TokenService;
import de.adorsys.xs2a.adapter.service.model.TokenBO;
import de.adorsys.xs2a.adapter.service.exception.TokenNotFoundServiceException;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TokenResourceTest {
    private static final String TOKEN_ID = "d766b4c7-a940-446d-9bd4-af70eacf9772";
    private static final UUID ID = UUID.fromString(TOKEN_ID);

    private MockMvc mockMvc;

    @InjectMocks
    private TokenController resource;

    @Mock
    private TokenService tokenService;

    @Mock
    private TokenTOConverter converter;
    private TokenBO bo;
    private TokenTO to;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders
                          .standaloneSetup(resource)
                          .setControllerAdvice(new ExceptionAdvisor())
                          .setMessageConverters(new MappingJackson2HttpMessageConverter())
                          .build();

        bo = readYml(TokenBO.class, "token-bo.yml");
        to = readYml(TokenTO.class, "token-to.yml");
    }

    @Test
    public void getById() throws Exception {
        when(tokenService.findById(TOKEN_ID)).thenReturn(bo);
        when(converter.toTokenTO(bo)).thenReturn(to);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .get("/tokens/{id}", TOKEN_ID)
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        TokenTO actual = deserialize(content, TokenTO.class);

        assertThat(actual, is(to));

        verify(tokenService, times(1)).findById(TOKEN_ID);
        verify(converter, times(1)).toTokenTO(bo);
    }

    @Test
    public void getByIdNotFoundException() throws Exception {
        when(tokenService.findById(TOKEN_ID)).thenThrow(TokenNotFoundServiceException.class);

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/tokens/{id}", TOKEN_ID)
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        verify(tokenService, times(1)).findById(TOKEN_ID);
    }

    private <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(TokenTOConverter.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T> T deserialize(String source, Class<T> tClass) {
        try {
            return JsonReader.getInstance().getObjectFromString(source, tClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Can't deserialize object", e);
        }
    }
}