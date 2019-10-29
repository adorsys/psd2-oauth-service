package de.adorsys.psd2.oauth.service.converter;

import de.adorsys.psd2.oauth.repository.model.TokenPO;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import de.adorsys.xs2a.adapter.api.TokenResponseTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface TokenBOConverter {
    TokenBO toTokenBO(TokenPO po);

    TokenPO toTokenPO(TokenBO bo);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "aspspId", source = "aspspId")
    @Mapping(target = "expirationDate", source = "expirationDate")
    @Mapping(target = "clientId", source = "clientId")
    TokenBO toTokenBO(TokenResponseTO to, String id, String aspspId, LocalDateTime expirationDate, String clientId);
}
