package de.adorsys.psd2.oauth.controller;

import de.adorsys.psd2.oauth.exception.ExceptionAdvisor;
import de.adorsys.psd2.oauth.model.AccessTokenTO;
import de.adorsys.psd2.oauth.service.TokenService;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pro.javatar.commons.reader.JsonReader;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TokenControllerTest {
    private static final String TOKEN_ID = "d766b4c7-a940-446d-9bd4-af70eacf9772";

    private MockMvc mockMvc;

    @InjectMocks
    private TokenController resource;

    @Mock
    private TokenService tokenService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders
                          .standaloneSetup(resource)
                          .setControllerAdvice(new ExceptionAdvisor())
                          .setMessageConverters(new MappingJackson2HttpMessageConverter())
                          .build();

    }

    @Test
    public void getById() throws Exception {
        TokenBO token = new TokenBO();
        token.setAccessToken("access-token");

        when(tokenService.findById(TOKEN_ID)).thenReturn(token);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .get("/oauth2/tokens/{id}", TOKEN_ID)
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        AccessTokenTO actual = deserialize(content, AccessTokenTO.class);

        assertThat(actual.getToken(), is(token.getAccessToken()));

        verify(tokenService, times(1)).findById(TOKEN_ID);
    }

    @Test
    public void getByIdNotFoundException() throws Exception {
        when(tokenService.findById(TOKEN_ID)).thenThrow(TokenNotFoundServiceException.class);

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/oauth2/tokens/{id}", TOKEN_ID)
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        verify(tokenService, times(1)).findById(TOKEN_ID);
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
