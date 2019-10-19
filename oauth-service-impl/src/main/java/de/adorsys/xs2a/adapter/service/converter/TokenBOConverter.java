package de.adorsys.xs2a.adapter.service.converter;

import de.adorsys.xs2a.adapter.rest.psd2.model.TokenResponseTO;
import org.mapstruct.Mapper;
import de.adorsys.xs2a.adapter.repository.model.TokenPO;
import de.adorsys.xs2a.adapter.service.model.TokenBO;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TokenBOConverter {
    TokenBO toTokenBO(TokenPO po);

    TokenPO toTokenPO(TokenBO bo);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "aspspId", source = "aspspId")
    TokenBO toTokenBO(TokenResponseTO to, String id, String aspspId);
}
