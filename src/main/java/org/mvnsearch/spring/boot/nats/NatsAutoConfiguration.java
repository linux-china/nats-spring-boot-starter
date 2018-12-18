package org.mvnsearch.spring.boot.nats;

import io.nats.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * NATS auto configuration
 *
 * @author linux_china
 */
@Configuration
@EnableConfigurationProperties(NatsProperties.class)
public class NatsAutoConfiguration {
    @Autowired
    private NatsProperties properties;

    @Bean(destroyMethod = "close")
    public Connection nats() throws Exception {
        Options.Builder builder = new Options.Builder();
        builder.server(properties.getUrl());
        if (properties.getJwtToken() != null && properties.getNkeyToken() != null) {
            builder.authHandler(new AuthHandler() {
                @Override
                public byte[] sign(byte[] nonce) {
                    try {
                        char[] keyChars = this.getNkeyChars();
                        NKey nkey = NKey.fromSeed(keyChars);
                        byte[] sig = nkey.sign(nonce);
                        nkey.clear();
                        return sig;
                    } catch (Exception exp) {
                        throw new IllegalStateException("problem signing nonce", exp);
                    }
                }

                @Override
                public char[] getID() {
                    try {
                        char[] keyChars = this.getNkeyChars();
                        NKey nkey = NKey.fromSeed(keyChars);
                        char[] pubKey = nkey.getPublicKey();
                        nkey.clear();
                        return pubKey;
                    } catch (Exception exp) {
                        throw new IllegalStateException("problem getting public key", exp);
                    }
                }

                @Override
                public char[] getJWT() {
                    return properties.getJwtToken().toCharArray();
                }

                private char[] getNkeyChars() throws IOException {
                    return properties.getNkeyToken().toCharArray();
                }
            });
        }
        return Nats.connect(builder.build());
    }

    @Bean
    public NatsSubscriberAnnotationBeanPostProcessor natsSubscriberAnnotationBeanPostProcessor() {
        return new NatsSubscriberAnnotationBeanPostProcessor();
    }

    @Bean
    public NatsEndpoint natsEndpoint() {
        return new NatsEndpoint();
    }

    @Bean
    public NatsHealthIndicator natsHealthIndicator() {
        return new NatsHealthIndicator();
    }
}
