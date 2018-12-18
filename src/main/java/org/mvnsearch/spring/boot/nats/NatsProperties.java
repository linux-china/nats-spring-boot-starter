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
     * jwt token
     */
    private String jwtToken;
    /**
     * nkey token
     */
    private String nkeyToken;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getNkeyToken() {
        return nkeyToken;
    }

    public void setNkeyToken(String nkeyToken) {
        this.nkeyToken = nkeyToken;
    }
}
