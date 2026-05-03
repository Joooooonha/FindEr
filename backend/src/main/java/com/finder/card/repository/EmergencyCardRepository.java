package com.finder.card.repository;

import com.finder.card.domain.EmergencyCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 응급카드 저장소 */
public interface EmergencyCardRepository extends JpaRepository<EmergencyCard, Long> {
    Optional<EmergencyCard> findByToken(String token);
    boolean existsByToken(String token);
}
