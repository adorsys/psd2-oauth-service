package de.adorsys.xs2a.adapter.resource;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import de.adorsys.xs2a.adapter.model.TokenTO;
import de.adorsys.xs2a.adapter.service.TokenService;
import de.adorsys.xs2a.adapter.service.model.TokenBO;
import de.adorsys.xs2a.adapter.converter.TokenTOConverter;
import de.adorsys.xs2a.adapter.exception.RestException;
import de.adorsys.xs2a.adapter.exception.NotFoundRestException;
import de.adorsys.xs2a.adapter.service.exception.TokenNotFoundServiceException;

import java.util.Random;
import java.util.UUID;


@RestController
@RequestMapping
public class TokenResource {

    private static final Logger logger = LoggerFactory.getLogger(TokenResource.class);
    public static final String TOKEN_BY_ID_URI = "/tokens/{id}";

    private final TokenService tokenService;
    private final TokenTOConverter converter;

    public TokenResource(TokenService tokenService, TokenTOConverter converter) {
        this.tokenService = tokenService;
        this.converter = converter;
    }


    @ApiOperation("Get token by id")
    @GetMapping(TOKEN_BY_ID_URI)
    public TokenTO getById(@PathVariable UUID id) throws RestException {
        logger.info("Get token by id={}", id);
        try {
            TokenBO token = tokenService.findById(id.toString());
            return converter.toTokenTO(token);
        } catch (TokenNotFoundServiceException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @GetMapping("/new")
    public TokenTO newToken() throws RestException {
            TokenBO token = new TokenBO();
            token.setId(UUID.randomUUID().toString());
            token.setAccessToken("my.access.token");
            token.setExpiresInSeconds(3600L);
            token.setRefreshToken("my.refresh.token");
            token.setScope("AIS PIS");
            token.setAdapterId("adorsys-integ-adapter");
            token.setTokenType("Bearer");
            tokenService.save(token);
            return converter.toTokenTO(token);
    }
}