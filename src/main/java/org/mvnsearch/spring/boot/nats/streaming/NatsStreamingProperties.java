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
	private Duration connectWait;
	private Duration ackTimeout;
	private Integer maxPubAcksInFlight;

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public Duration getConnectWait() {
		return connectWait;
	}

	public void setConnectWait(Duration connectWait) {
		this.connectWait = connectWait;
	}

	public Duration getAckTimeout() {
		return ackTimeout;
	}

	public void setAckTimeout(Duration ackTimeout) {
		this.ackTimeout = ackTimeout;
	}

	public Integer getMaxPubAcksInFlight() {
		return maxPubAcksInFlight;
	}

	public void setMaxPubAcksInFlight(Integer maxPubAcksInFlight) {
		this.maxPubAcksInFlight = maxPubAcksInFlight;
	}
}
