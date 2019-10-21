package de.adorsys.psd2.oauth.service.converter;

import de.adorsys.xs2a.adapter.api.TokenResponseTO;
import org.mapstruct.Mapper;
import de.adorsys.psd2.oauth.repository.model.TokenPO;
import de.adorsys.psd2.oauth.service.model.TokenBO;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TokenBOConverter {
    TokenBO toTokenBO(TokenPO po);

    TokenPO toTokenPO(TokenBO bo);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "aspspId", source = "aspspId")
    TokenBO toTokenBO(TokenResponseTO to, String id, String aspspId);
}
