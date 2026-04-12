package com.numbericsuserportal.ai.repo;

import com.numbericsuserportal.ai.entity.TaalrChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaalrChatMessageRepository extends JpaRepository<TaalrChatMessageEntity, Long> {

    List<TaalrChatMessageEntity> findByUserIdOrderByIdAsc(Long userId);

    void deleteByUserId(Long userId);
}
