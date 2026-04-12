package com.numbericsuserportal.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Anthropic Messages API — key from env ANTHROPIC_API_KEY (never commit real keys).
 */
@ConfigurationProperties(prefix = "anthropic.api")
public class AnthropicProperties {

    /**
     * API key; prefer env ANTHROPIC_API_KEY.
     */
    private String key = "";

    private String model = "claude-sonnet-4-6";

    private int maxTokens = 8192;

    /** Anthropic API version header */
    private String version = "2023-06-01";

    /** Optional extra system instructions (Messages API fallback only). */
    private String systemPromptExtra = "";

    /**
     * If true and agent-id + environment-id are set, chat uses Claude Managed Agents (Console prompt) via Sessions API.
     * Otherwise falls back to Messages API.
     */
    private boolean managedAgentsEnabled = false;

    /** Taalr agent id from Console, e.g. agent_01... */
    private String agentId = "";

    /** Environment id from Console → Environments, e.g. env_01... */
    private String environmentId = "";

    /** Beta header for Managed Agents API (see Anthropic docs). */
    private String managedAgentsBeta = "managed-agents-2026-04-01";

    /**
     * Messages API fallback only: max stored turns (user + assistant rows) sent per request; oldest dropped.
     * Typical chat onboarding needs ~10–30 exchanges; default 80 leaves headroom.
     */
    private int messagesHistoryMaxMessages = 80;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSystemPromptExtra() {
        return systemPromptExtra;
    }

    public void setSystemPromptExtra(String systemPromptExtra) {
        this.systemPromptExtra = systemPromptExtra;
    }

    public boolean isManagedAgentsEnabled() {
        return managedAgentsEnabled;
    }

    public void setManagedAgentsEnabled(boolean managedAgentsEnabled) {
        this.managedAgentsEnabled = managedAgentsEnabled;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getManagedAgentsBeta() {
        return managedAgentsBeta;
    }

    public void setManagedAgentsBeta(String managedAgentsBeta) {
        this.managedAgentsBeta = managedAgentsBeta;
    }

    public int getMessagesHistoryMaxMessages() {
        return messagesHistoryMaxMessages;
    }

    public void setMessagesHistoryMaxMessages(int messagesHistoryMaxMessages) {
        this.messagesHistoryMaxMessages = messagesHistoryMaxMessages;
    }

    /** Managed Agents mode is fully configured (Sessions API). */
    public boolean isManagedAgentsReady() {
        return managedAgentsEnabled
                && agentId != null && !agentId.isBlank()
                && environmentId != null && !environmentId.isBlank();
    }
}
