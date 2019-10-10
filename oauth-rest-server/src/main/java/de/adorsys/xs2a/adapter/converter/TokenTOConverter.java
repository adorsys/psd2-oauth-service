package de.adorsys.xs2a.adapter.converter;

import org.mapstruct.Mapper;
import de.adorsys.xs2a.adapter.model.TokenTO;
import de.adorsys.xs2a.adapter.service.model.TokenBO;

@Mapper(componentModel = "spring")
public interface TokenTOConverter {

    TokenTO toTokenTO(TokenBO bo);

    TokenBO toTokenBO(TokenTO to);
}
