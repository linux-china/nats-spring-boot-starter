package org.mvnsearch.spring.boot.nats.serialization;

import java.lang.reflect.Method;

public class ProtobufSerialization implements ObjectSerialization {

    public Object convert(byte[] bytes, Class<?> targetClass) throws Exception {
        Method parseFromMethod = targetClass.getMethod("parseFrom", byte[].class);
        return parseFromMethod.invoke(null, (Object) bytes);
    }

    public byte[] toBytes(Object object) throws Exception {
        Method toByteArrayMethod = object.getClass().getMethod("toByteArray");
        return (byte[]) toByteArrayMethod.invoke(object);
    }
}
