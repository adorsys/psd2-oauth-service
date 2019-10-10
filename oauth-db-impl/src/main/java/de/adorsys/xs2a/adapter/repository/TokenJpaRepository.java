package de.adorsys.xs2a.adapter.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import de.adorsys.xs2a.adapter.repository.model.TokenEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenJpaRepository extends PagingAndSortingRepository<TokenEntity, UUID> {
}
