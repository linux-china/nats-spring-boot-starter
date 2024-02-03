package org.mvnsearch.spring.boot.nats.serialization;

import com.google.protobuf.Message;

public class SerializationUtil {
    private static final JsonSerialization json = new JsonSerialization();
    private static final ProtobufSerialization protobuf = new ProtobufSerialization();

    public static Object convert(byte[] bytes, Class<?> targetClass) throws Exception {
        if (targetClass.isAssignableFrom(Message.class)) {
            return protobuf.convert(bytes, targetClass);
        } else {
            return json.convert(bytes, targetClass);
        }
    }

    public static byte[] toBytes(Object object) throws Exception {
        if (object instanceof Message) {
            return protobuf.toBytes(object);
        } else {
            return json.toBytes(object);
        }
    }
}
