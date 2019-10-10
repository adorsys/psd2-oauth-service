package de.adorsys.xs2a.adapter.service.converter;

import org.mapstruct.Mapper;
import de.adorsys.xs2a.adapter.repository.model.TokenPO;
import de.adorsys.xs2a.adapter.service.model.TokenBO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TokenBOConverter {
    TokenBO toTokenBO(TokenPO po);

    TokenPO toTokenPO(TokenBO bo);
}
