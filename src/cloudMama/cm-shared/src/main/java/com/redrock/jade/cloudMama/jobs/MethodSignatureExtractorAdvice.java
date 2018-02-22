package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.cloudMama.StreamUtils;
import com.redrock.jade.cloudMama.jobs.exceptions.MethodSignatureNotFound;
import jodd.proxetta.ProxyAdvice;
import jodd.proxetta.ProxyTarget;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Copyright RedRock 2013-14
 */
final class MethodSignatureExtractorAdvice implements ProxyAdvice {
    public MethodSignature _sig8b1a1a2928eb47a78e08f700e0f0f7d8;

    /**
     * Extracts the signature of the any invoked method
     * NOTE: Due to some limitations of the proxy library, this method
     * cannot be refactored into smaller methods.
     *
     * @return Null
     * @throws Exception
     */
    @Override
    public Object execute() throws Exception {
        String currentMethodName = ProxyTarget.targetMethodName();
        Class[] currentMethodParameterTypes = ProxyTarget.createArgumentsClassArray();

        Method[] targetClassMethods = ProxyTarget.targetClass().getMethods();
        Method targetMethod = null;
        for (Method method : targetClassMethods) {
            if (!method.getName().equals(currentMethodName)) {
                continue;
            }

            Class[] methodParameterTypes = method.getParameterTypes();
            if (methodParameterTypes.length != currentMethodParameterTypes.length) {
                continue;
            }

            boolean found = true;
            for (int i = 0; i < methodParameterTypes.length; i++) {
                if (!methodParameterTypes[i].equals(currentMethodParameterTypes[i])) {
                    found = false;
                    break;
                }
            }

            if (found) {
                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            throw new MethodSignatureNotFound(String.format("Could not find method '%s' in class '%s'",
                                                            ProxyTarget.targetMethodSignature(),
                                                            ProxyTarget.targetClass().getCanonicalName()));
        }
        _sig8b1a1a2928eb47a78e08f700e0f0f7d8 = new MethodSignature(targetMethod);
        return null;
    }

    public static MethodSignature getExtractedMethodSignature(Object proxy) throws IllegalAccessException {
        String signatureFieldName = StreamUtils.stream(MethodSignatureExtractorAdvice.class.getFields())
                                               .filter(field -> field.getType().equals(MethodSignature.class))
                                               .findFirst()
                                               .get()
                                               .getName();

        Field signatureField = StreamUtils.stream(proxy.getClass().getFields())
                                          .filter(field -> field.getName().contains(signatureFieldName))
                                          .findFirst()
                                          .get();
        return (MethodSignature) signatureField.get(proxy);
    }
}
