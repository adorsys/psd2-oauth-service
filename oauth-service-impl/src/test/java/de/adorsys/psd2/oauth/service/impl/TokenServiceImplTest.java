package de.adorsys.psd2.oauth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.oauth.repository.TokenRepository;
import de.adorsys.psd2.oauth.repository.exception.TokenNotFoundDBException;
import de.adorsys.psd2.oauth.repository.model.TokenPO;
import de.adorsys.psd2.oauth.service.converter.TokenBOConverter;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.OauthStateBO;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

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
    private TokenBOConverter converter;

    @Mock
    private Oauth2Client oauth2Client;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private TokenPO po;
    private TokenBO bo;

    @Before
    public void setUp() {
        po = mock(TokenPO.class);
        bo = mock(TokenBO.class);
    }

    @Test
    public void findById() throws TokenNotFoundDBException, TokenNotFoundServiceException {

        when(repository.findById(TOKEN_ID)).thenReturn(po);
        when(converter.toTokenBO(po)).thenReturn(bo);

        TokenBO actual = tokenService.findById(TOKEN_ID);

        assertThat(actual, is(bo));

        verify(repository, times(1)).findById(TOKEN_ID);
        verify(converter, times(1)).toTokenBO(po);
    }

    @Test(expected = TokenNotFoundServiceException.class)
    public void findByIdWithTokenNotFoundServiceException() throws TokenNotFoundDBException, TokenNotFoundServiceException {
        String id = "12134";

        when(repository.findById(id)).thenThrow(TokenNotFoundDBException.class);

        tokenService.findById(id);
    }

    @Test
    public void save() {
        when(converter.toTokenPO(bo)).thenReturn(po);
        when(repository.save(po)).thenReturn(po);
        when(converter.toTokenBO(po)).thenReturn(bo);

        TokenBO actual = tokenService.save(bo);

        assertThat(actual, is(bo));

        verify(converter, times(1)).toTokenPO(bo);
        verify(repository, times(1)).save(po);
        verify(converter, times(1)).toTokenBO(po);
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
}