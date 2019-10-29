package de.adorsys.psd2.oauth.controller;

import de.adorsys.psd2.oauth.exception.TokenNotFoundRestException;
import de.adorsys.psd2.oauth.model.AccessTokenTO;
import de.adorsys.psd2.oauth.service.TokenService;
import de.adorsys.psd2.oauth.service.exception.RefreshTokenException;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@ConditionalOnProperty(value = "oauth.get-token-endpoint.enabled", havingValue = "true")
@RestController
public class TokenController {
    private static final String TOKEN_BY_ID_URI = "/oauth2/tokens/{id}";

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @ApiOperation("Get token by id")
    @GetMapping(TOKEN_BY_ID_URI)
    public AccessTokenTO getById(@PathVariable UUID id) throws RefreshTokenException {
        TokenBO token = null;
        try {
            token = tokenService.findById(id.toString());
        } catch (TokenNotFoundServiceException e) {
            throw new TokenNotFoundRestException(e.getMessage());
        }
        return new AccessTokenTO(token.getAccessToken());
    }
}
