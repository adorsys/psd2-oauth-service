package de.adorsys.psd2.oauth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.oauth.repository.TokenRepository;
import de.adorsys.psd2.oauth.repository.exception.TokenNotFoundDBException;
import de.adorsys.psd2.oauth.repository.model.TokenPO;
import de.adorsys.psd2.oauth.service.converter.TokenBOConverter;
import de.adorsys.psd2.oauth.service.exception.RefreshTokenException;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.OauthStateBO;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import de.adorsys.xs2a.adapter.api.TokenResponseTO;
import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

import static de.adorsys.psd2.oauth.service.impl.TokenServiceImpl.STATE_PARAMETER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceImplTest {
    private static final String TOKEN_ID = "d766b4c7-a940-446d-9bd4-af70eacf9772";
    private static final String SCA_OAUTH_LINK = "http://localhost:8082/oauth2/authorization-code?code=1234";
    private static final String ENCODED_STATE = "eyJjbGllbnRJZCI6IjExMTEiLCJhc3BzcElkIjoiMjIyMiJ9";
    private static final String STATE = STATE_PARAMETER + ENCODED_STATE;
    private static final String PSU_ID = "1111";
    private static final String ASPSP_ID = "2222";


    @InjectMocks
    private TokenServiceImpl tokenService;

    @Mock
    private TokenRepository repository;

    @Mock
    private Oauth2Client oauth2Client;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private TokenPO po;
    private TokenBO bo;
    private TokenResponseTO tokenResponse;

    @Before
    public void setUp() {
        tokenService.setConverter(Mappers.getMapper(TokenBOConverter.class));
        po = readYml(TokenPO.class, "token-po.yml");
        bo = readYml(TokenBO.class, "token-bo.yml");
        tokenResponse = readYml(TokenResponseTO.class, "token-response-to.yml");
    }

    @Test
    public void findByIdWhenRefreshTokenIsDisabled() throws TokenNotFoundDBException, TokenNotFoundServiceException, RefreshTokenException {

        when(repository.findById(TOKEN_ID)).thenReturn(po);

        TokenBO actual = tokenService.findById(TOKEN_ID);

        assertThat(actual, is(bo));

        verify(repository, times(1)).findById(TOKEN_ID);
    }

    @Test
    public void findByIdWhenRefreshTokenIsEnabled() throws TokenNotFoundDBException, TokenNotFoundServiceException, RefreshTokenException, IOException {
        tokenService.setRefreshTokenImplicitlyEnabled(true);

        when(repository.findById(TOKEN_ID)).thenReturn(po);
        when(oauth2Client.getToken(anyMap(), anyMap())).thenReturn(tokenResponse);
        when(repository.save(any())).thenReturn(po);

        TokenBO actual = tokenService.findById(TOKEN_ID);

        assertThat(actual, is(bo));

        verify(repository, times(1)).findById(TOKEN_ID);
        verify(oauth2Client, times(1)).getToken(anyMap(), anyMap());
    }


    @Test(expected = TokenNotFoundServiceException.class)
    public void findByIdWithTokenNotFoundServiceException() throws TokenNotFoundDBException, TokenNotFoundServiceException, RefreshTokenException {

        when(repository.findById(TOKEN_ID)).thenThrow(TokenNotFoundDBException.class);

        tokenService.findById(TOKEN_ID);
    }

    @Test(expected = RefreshTokenException.class)
    public void findByIdWithRefreshTokenException() throws TokenNotFoundDBException, TokenNotFoundServiceException, RefreshTokenException, IOException {
        String id = "12134";
        tokenService.setRefreshTokenImplicitlyEnabled(true);

        when(repository.findById(TOKEN_ID)).thenReturn(po);
        when(oauth2Client.getToken(anyMap(), anyMap())).thenThrow(new IOException());

        TokenBO actual = tokenService.findById(TOKEN_ID);

        assertThat(actual, is(bo));

        verify(repository, times(1)).findById(TOKEN_ID);
        verify(oauth2Client, times(1)).getToken(anyMap(), anyMap());
    }

    @Test
    public void save() {
        when(repository.save(po)).thenReturn(po);

        TokenBO actual = tokenService.save(bo);

        assertThat(actual, is(bo));

        verify(repository, times(1)).save(po);
    }

    @Test
    public void delete() {
        doNothing().when(repository).delete(TOKEN_ID);

        tokenService.delete(TOKEN_ID);

        verify(repository, times(1)).delete(TOKEN_ID);
    }

    @Test
    public void encodeState() {
        OauthStateBO state = new OauthStateBO(PSU_ID, ASPSP_ID);

        assertThat(tokenService.encodeState(state), is(ENCODED_STATE));
    }

    @Test
    public void attachState() {
        // case when state is not present in the link

        String link = tokenService.attachState(SCA_OAUTH_LINK, ASPSP_ID, PSU_ID);

        assertThat(link.contains(STATE), is(true));

        // case when state is not present in the link
        String scaOAuthLink = SCA_OAUTH_LINK + STATE_PARAMETER + "old-state";
        link = tokenService.attachState(scaOAuthLink, ASPSP_ID, PSU_ID);

        assertThat(link.contains(STATE), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void attachStateWithError() {
        tokenService.attachState(SCA_OAUTH_LINK, "", PSU_ID);
    }

    @Test
    public void refreshToken() throws RefreshTokenException, IOException {

        when(oauth2Client.getToken(anyMap(), anyMap())).thenReturn(tokenResponse);

        tokenService.refreshToken(bo);

        verify(oauth2Client, times(1)).getToken(anyMap(), anyMap());
    }

    @Test(expected = RefreshTokenException.class)
    public void refreshTokenWithException() throws RefreshTokenException, IOException {

        when(oauth2Client.getToken(anyMap(), anyMap())).thenThrow(new IOException());

        tokenService.refreshToken(bo);

        verify(oauth2Client, times(1)).getToken(anyMap(), anyMap());
    }

    private <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(TokenBOConverter.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
