package org.mvnsearch.spring.boot.nats.serialization;

public interface ObjectSerialization {
    Object convert(byte[] bytes, Class<?> targetClass) throws Exception;

    byte[] toBytes(Object object) throws Exception;
}
