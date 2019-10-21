package de.adorsys.psd2.oauth.repository.converter;

import de.adorsys.psd2.oauth.repository.model.TokenEntity;
import org.mapstruct.Mapper;
import de.adorsys.psd2.oauth.repository.model.TokenPO;

@Mapper(componentModel = "spring")
public interface TokenEntityConverter {

    TokenPO toTokenPO(TokenEntity entity);

    TokenEntity toTokenEntity(TokenPO po);
}
