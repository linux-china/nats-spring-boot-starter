package org.mvnsearch.spring.boot.nats;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * NATS properties
 *
 * @author linux_china
 */
@ConfigurationProperties(
        prefix = "nats"
)
public class NatsProperties {
    /**
     * NATS connection url, such as "nats://localhost:4222" or url list split by comma
     */
    private String url = "nats://localhost:4222";
    /**
     * jwt token to connect NATS
     */
    private String token;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
