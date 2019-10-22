package de.adorsys.psd2.oauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.oauth.config.BankConfig;
import de.adorsys.psd2.oauth.config.BankSettings;
import de.adorsys.psd2.oauth.exception.BankNotSupportedException;
import de.adorsys.psd2.oauth.model.StateTO;
import de.adorsys.psd2.oauth.service.TokenService;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.oauth.controller.AuthorizationController.AUTHORIZATION_CODE_ENDPOINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthorizationControllerTest {

    private static final String BANK_NAME = "adorsys";
    private static final String CLIENT_ID = "client-id";
    private static final String CODE_PARAMETER = "code";
    private static final String STATE_PARAMETER = "state";
    private static final String SERVER_HOST = "http://localhost:8080";
    private static final String REDIRECT_URI = SERVER_HOST + AUTHORIZATION_CODE_ENDPOINT.replace("{bank}", BANK_NAME);

    private MockMvc mockMvc;

    @InjectMocks
    private AuthorizationController controller;

    @Mock
    private TokenService tokenService;

    @Spy
    private BankConfig bankConfig = new BankConfig();

    private String endpoint = AUTHORIZATION_CODE_ENDPOINT.replace("{bank}", BANK_NAME);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Map<String, BankSettings> config = new HashMap<>();
        BankSettings bankProperty = new BankSettings(CLIENT_ID);
        config.put(BANK_NAME, bankProperty);
        bankConfig.setBankConfig(config);

        controller.setServerUrl(SERVER_HOST);
    }

    @Test
    public void getAuthorizationCode() throws Exception {
        String authCode = "my-code";
        TokenBO token = new TokenBO();
        String successUrl = "success-url";
        String state = encodeState(buildState());
        controller.setSuccessUrl(successUrl);

        when(tokenService.exchangeAuthCode(authCode, REDIRECT_URI, CLIENT_ID, state)).thenReturn(token);
        when(tokenService.save(token)).thenReturn(token);

        mockMvc.perform(
                get(endpoint)
                        .param(CODE_PARAMETER, authCode)
                        .param(STATE_PARAMETER, state)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(successUrl));

        verify(tokenService, times(1)).exchangeAuthCode(authCode, REDIRECT_URI, CLIENT_ID, state);
        verify(tokenService, times(1)).save(token);
    }

    @Test
    public void getAuthorizationCodeWithError() throws Exception {
        String errorUrl = "error-url";
        controller.setErrorUrl(errorUrl);

        mockMvc.perform(
                get(endpoint)
                        .param("error", "invalid_request")
                        .param(STATE_PARAMETER, encodeState(buildState()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(errorUrl));
    }

    @Test
    public void buildRedirectUri() throws BankNotSupportedException {
        String redirectUri = controller.buildRedirectUri(BANK_NAME);

        assertThat(redirectUri).isEqualTo(REDIRECT_URI);
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