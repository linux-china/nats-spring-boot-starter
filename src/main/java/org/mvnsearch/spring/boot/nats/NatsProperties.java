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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
