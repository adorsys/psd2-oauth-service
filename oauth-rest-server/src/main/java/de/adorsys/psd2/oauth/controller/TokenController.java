package de.adorsys.psd2.oauth.controller;

import de.adorsys.psd2.oauth.exception.NotFoundRestException;
import de.adorsys.psd2.oauth.exception.RestException;
import de.adorsys.psd2.oauth.model.AccessTokenTO;
import de.adorsys.psd2.oauth.service.TokenService;
import de.adorsys.psd2.oauth.service.exception.TokenNotFoundServiceException;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TokenController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
    private static final String TOKEN_BY_ID_URI = "/oauth2/tokens/{id}";

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    @ApiOperation("Get token by id")
    @GetMapping(TOKEN_BY_ID_URI)
    public AccessTokenTO getById(@PathVariable UUID id) throws RestException {
        logger.info("Get token by id={}", id);
        try {
            TokenBO token = tokenService.findById(id.toString());
            //todo: implement refresh token functionality. issue #[XS2AAD-46]
            return new AccessTokenTO(token.getAccessToken());
        } catch (TokenNotFoundServiceException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }
}
