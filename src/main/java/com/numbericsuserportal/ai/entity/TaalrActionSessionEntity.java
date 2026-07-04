package com.numbericsuserportal.ai.entity;

import com.numbericsuserportal.ai.action.TaalrActionChannel;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stores in-progress Taalr actions (receipt confirm, invoice draft) per user or WhatsApp phone.
 */
@Entity
@Table(name = "taalr_action_sessions", indexes = {
        @Index(name = "idx_taalr_action_user", columnList = "user_id"),
        @Index(name = "idx_taalr_action_phone", columnList = "phone_number")
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class TaalrActionSessionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 16)
    private TaalrActionChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "pending_action", length = 32)
    private TaalrPendingAction pendingAction;

    @Column(name = "context_json", columnDefinition = "LONGTEXT")
    private String contextJson;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
