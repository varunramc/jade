package com.redrock.jade.cloudMama.jobs;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.redrock.jade.cloudMama.jobs.exceptions.CallbackMethodException;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Copyright RedRock 2013-14
 */
public final class MethodSignature implements Serializable {
    private final String  methodName;
    private final Class[] argumentTypes;

    /**
     * Empty constructor for Jackson
     */
    public MethodSignature() {
        methodName = null;
        argumentTypes = null;
    }

    public MethodSignature(Method method) {
        Preconditions.checkNotNull(method);

        if (Modifier.isPrivate(method.getModifiers())) {
            throw new CallbackMethodException(String.format(
                    "Method '%s' is private. Only public callback methods are allowed",
                    method.getName()), null);
        }

        methodName = method.getName();
        argumentTypes = method.getParameterTypes();
    }

    public Method getMethod(Class clazz) throws NoSuchMethodException {
        Preconditions.checkState(!Strings.isNullOrEmpty(methodName));
        Preconditions.checkNotNull(argumentTypes);

        return clazz.getMethod(methodName, argumentTypes);
    }
}
