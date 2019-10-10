package de.adorsys.xs2a.adapter.repository.converter;

import org.mapstruct.Mapper;
import de.adorsys.xs2a.adapter.repository.model.TokenEntity;
import de.adorsys.xs2a.adapter.repository.model.TokenPO;

@Mapper(componentModel = "spring")
public interface TokenEntityConverter {

    TokenPO toTokenPO(TokenEntity entity);

    TokenEntity toTokenEntity(TokenPO po);
}
