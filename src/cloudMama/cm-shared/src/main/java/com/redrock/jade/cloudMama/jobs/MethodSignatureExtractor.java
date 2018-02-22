package com.redrock.jade.cloudMama.jobs;

import jodd.proxetta.*;
import jodd.proxetta.impl.ProxyProxetta;
import jodd.proxetta.impl.ProxyProxettaBuilder;
import jodd.proxetta.pointcuts.MethodAnnotationPointcut;

import java.util.function.Consumer;

/**
 * Copyright RedRock 2013-14
 */
final class MethodSignatureExtractor {
    private MethodSignatureExtractor() {
    }

    public static synchronized <T> MethodSignature extract(Class<T> targetClass,
                                                           Consumer<T> methodInvoker) throws IllegalAccessException {
        T proxy = createTargetClassProxy(targetClass);
        methodInvoker.accept(proxy);
        return MethodSignatureExtractorAdvice.getExtractedMethodSignature(proxy);
    }

    private static <T> T createTargetClassProxy(Class<T> targetClass) {
        // TODO: Cache proxies or remove after use
        ProxyAspect aspect = new ProxyAspect(MethodSignatureExtractorAdvice.class,
                                             new MethodAnnotationPointcut(JobCallback.class));
        ProxyProxetta proxetta = ProxyProxetta.withAspects(aspect);
        proxetta.setVariableClassName(true);

        ProxyProxettaBuilder builder = proxetta.builder(targetClass);
        return (T) builder.newInstance();
    }
}
