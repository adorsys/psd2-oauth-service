package de.adorsys.psd2.oauth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import de.adorsys.psd2.oauth.repository.TokenRepository;
import de.adorsys.psd2.oauth.repository.exception.TokenNotFoundDBException;
import de.adorsys.psd2.oauth.repository.model.TokenPO;
import de.adorsys.psd2.oauth.service.converter.TokenBOConverter;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceImplTest {
    private static final String TOKEN_ID = "d766b4c7-a940-446d-9bd4-af70eacf9772";

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Mock
    private TokenRepository repository;

    @Mock
    private TokenBOConverter converter;

    @Mock
    private Oauth2Client oauth2Client;

    @InjectMocks
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
}