package de.adorsys.psd2.oauth.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import de.adorsys.psd2.oauth.repository.model.TokenEntity;

import java.util.UUID;

@Repository
public interface TokenJpaRepository extends PagingAndSortingRepository<TokenEntity, UUID> {
}
