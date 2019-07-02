package org.mvnsearch.spring.boot.nats.streaming;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * NATS Streaming properties
 *
 * @author wisni
 */
@ConfigurationProperties(prefix = "nats.streaming")
public class NatsStreamingProperties {

    private String clusterId;
    private String clientId;
    private boolean clientIdAutoGeneration;
    private Duration connectWait;
    private Duration ackTimeout;
    private Integer maxPubAcksInFlight;

    private String generatedClientId;

    public String getClusterId() {
        return this.clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClientId() {
        if (this.clientIdAutoGeneration && this.generatedClientId == null) {
            this.generatedClientId = this.clientId.concat("_").concat(NatsStreamingClientIdGenerator.getId());
            this.clientId = this.generatedClientId;
        }

        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isClientIdAutoGeneration() {
        return this.clientIdAutoGeneration;
    }

    public void setClientIdAutoGeneration(boolean clientIdAutoGeneration) {
        this.clientIdAutoGeneration = clientIdAutoGeneration;
    }

    public Duration getConnectWait() {
        return this.connectWait;
    }

    public void setConnectWait(Duration connectWait) {
        this.connectWait = connectWait;
    }

    public Duration getAckTimeout() {
        return this.ackTimeout;
    }

    public void setAckTimeout(Duration ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public Integer getMaxPubAcksInFlight() {
        return this.maxPubAcksInFlight;
    }

    public void setMaxPubAcksInFlight(Integer maxPubAcksInFlight) {
        this.maxPubAcksInFlight = maxPubAcksInFlight;
    }
}
