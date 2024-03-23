package org.mvnsearch.spring.boot.nats.client;

import io.nats.client.Connection;

import java.lang.reflect.Proxy;

public class NatsExchangeProxyFactory {

    public static  <T> T buildStub(Connection nc, Class<T> clazz) {
        NatsServiceInvocationHandler handler = new NatsServiceInvocationHandler(nc, clazz);
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
    }
}
