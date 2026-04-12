package com.numbericsuserportal.ai.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Maps each portal user to one Claude Managed Agents session ({@code sesn_...}).
 */
@Entity
@Table(name = "taalr_agent_sessions")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class TaalrAgentSessionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** Anthropic session id, e.g. sesn_01... */
    @Column(name = "anthropic_session_id", nullable = false, length = 128)
    private String anthropicSessionId;

    /** Agent id snapshot at session creation (e.g. agent_01...) */
    @Column(name = "agent_id", length = 128)
    private String agentId;
}
