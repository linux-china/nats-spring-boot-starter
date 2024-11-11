package org.mvnsearch.spring.boot.nats.demo.component;

import org.mvnsearch.spring.boot.nats.annotation.NatsDurableComponent;
import org.mvnsearch.spring.boot.nats.annotation.NatsKeyWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@NatsDurableComponent
public class OnlineUserComponent {
    private static final Logger logger = LoggerFactory.getLogger(OnlineUserComponent.class);
    private int onlineUserCount;

    @NatsKeyWatcher(bucket = "bucket", key = "online.user.count")
    public void setOnlineUserCount(int count) {
      logger.info("Online user count: {}", count);
        this.onlineUserCount = count;
    }
}
