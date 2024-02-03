package org.mvnsearch.spring.boot.nats;

public interface NatsContextAware {
    String getSubjectNameForAppInstance();
}
