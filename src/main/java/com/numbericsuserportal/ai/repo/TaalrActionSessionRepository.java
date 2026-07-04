package com.numbericsuserportal.ai.repo;

import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TaalrActionSessionRepository extends JpaRepository<TaalrActionSessionEntity, Long> {

    Optional<TaalrActionSessionEntity> findFirstByUserIdAndIsActiveTrueAndExpiresAtAfterOrderByModifiedOnDesc(
            Long userId, LocalDateTime now);

    Optional<TaalrActionSessionEntity> findFirstByPhoneNumberAndIsActiveTrueAndExpiresAtAfterOrderByModifiedOnDesc(
            String phoneNumber, LocalDateTime now);
}
