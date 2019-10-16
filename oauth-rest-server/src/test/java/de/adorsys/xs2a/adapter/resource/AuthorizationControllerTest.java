package de.adorsys.xs2a.adapter.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import de.adorsys.xs2a.adapter.converter.TokenTOConverter;
import de.adorsys.xs2a.adapter.model.StateTO;
import de.adorsys.xs2a.adapter.rest.psd2.model.TokenResponseTO;
import de.adorsys.xs2a.adapter.service.TokenService;
import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Base64;

import static de.adorsys.xs2a.adapter.resource.AuthorizationController.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthorizationControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AuthorizationController controller;

    @Mock
    private TokenService tokenService;

    @Mock
    private Oauth2Client oauth2Client;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        controller.setObjectMapper(new ObjectMapper());
        controller.setTokenTOConverter(Mappers.getMapper(TokenTOConverter.class));
    }

    @Test
    public void getAuthorizationCode() throws Exception {
        String successUrl = "success-url";
        controller.setSuccessUrl(successUrl);

        when(oauth2Client.getToken(anyMap(), anyMap())).thenReturn(new TokenResponseTO());

        mockMvc.perform(
                get(AUTHORIZATION_CODE_ENDPOINT)
                        .param(CODE_PARAMETER, "my-code")
                        .param(STATE_PARAMETER, encodeState(buildState()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(successUrl));
    }

    @Test
    public void getAuthorizationCodeWithError() throws Exception {
        String errorUrl = "error-url";
        controller.setErrorUrl(errorUrl);

        mockMvc.perform(
                get(AUTHORIZATION_CODE_ENDPOINT)
                        .param(ERROR_PARAMETER, "invalid_request")
                        .param(STATE_PARAMETER, encodeState(buildState()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(errorUrl));
    }

    private String encodeState(StateTO state) throws JsonProcessingException {
        String stateJson = new ObjectMapper().writeValueAsString(state);
        return Base64.getEncoder().encodeToString(stateJson.getBytes());
    }

    private StateTO buildState() {
        StateTO state = new StateTO();
        state.setAdapterId("9d95becd-3981-4fd1-8a8b-106fc60606e9");
        state.setClientId("ce4160dc-5d98-4d26-b05a-2b346462a50b");
        return state;
    }
}