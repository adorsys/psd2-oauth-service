package de.adorsys.xs2a.adapter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import de.adorsys.xs2a.adapter.config.BankConfig;
import de.adorsys.xs2a.adapter.model.StateTO;
import de.adorsys.xs2a.adapter.rest.psd2.model.TokenResponseTO;
import de.adorsys.xs2a.adapter.service.TokenService;
import de.adorsys.xs2a.adapter.service.model.TokenBO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static de.adorsys.xs2a.adapter.controller.AuthorizationController.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
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

    private BankConfig bankConfig = new BankConfig();

    private String endpoint = AUTHORIZATION_CODE_ENDPOINT.replace("{bank}", "adorsys");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        controller.setObjectMapper(new ObjectMapper());
        HashMap<String, Map<String, String>> config = new HashMap<>();
        HashMap<String, String> adorsysConfig = new HashMap<>();
        adorsysConfig.put(BankConfig.CLIENT_ID, "client-id");
        adorsysConfig.put(BankConfig.REDIRECT_URI, "redirect-uri");
        config.put("adorsys", adorsysConfig);
        bankConfig.setBankConfig(config);
        controller.setBankConfig(bankConfig);
    }

    @Test
    public void getAuthorizationCode() throws Exception {
        TokenBO token = new TokenBO();
        String successUrl = "success-url";
        controller.setSuccessUrl(successUrl);

        when(oauth2Client.getToken(anyMap(), anyMap())).thenReturn(new TokenResponseTO());
        when(tokenService.save(any())).thenReturn(token);


        mockMvc.perform(
                get(endpoint)
                        .param(CODE_PARAMETER, "my-code")
                        .param(STATE_PARAMETER, encodeState(buildState()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(successUrl));

        verify(oauth2Client, times(1)).getToken(anyMap(), anyMap());
        verify(tokenService, times(1)).save(any());
    }

    @Test
    public void getAuthorizationCodeWithError() throws Exception {
        String errorUrl = "error-url";
        controller.setErrorUrl(errorUrl);

        mockMvc.perform(
                get(endpoint)
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
        state.setAspspId("5861cb21-1aca-41bc-b652-f73be6f042dc");
        state.setClientId("b4e24074-5294-481d-8ef8-4b17b6a136c5");
        return state;
    }
}