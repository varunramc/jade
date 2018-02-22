package com.redrock.jade.cloudMama.console;

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;

import static com.redrock.jade.cloudMama.console.JobParameterType.INTEGER;
import static com.redrock.jade.cloudMama.console.JobParameterType.STRING;

/**
 * Copyright RedRock 2013-14
 */
public final class JobParameterMetaData {
    private final Field            field;
    private final JobParameterType type;
    private final boolean          isRequired;
    private final String           defaultValue;

    public JobParameterMetaData(Field field) {
        Preconditions.checkNotNull(field);

        this.field = field;
        type = getParameterType(field, field.getType());

        Parameter parameterAnnotation = field.getAnnotation(Parameter.class);
        isRequired = parameterAnnotation.isRequired();
        defaultValue = parameterAnnotation.defaultValue();
    }

    private JobParameterType getParameterType(Field field, Class<?> fieldType) {
        if (fieldType.equals(String.class)) {
            return STRING;
        }
        else if (fieldType.equals(int.class)) {
            return INTEGER;
        }
        else {
            throw new IllegalArgumentException(String.format("The type of job parameter '%s' is not supported",
                                                             field.toString()));
        }
    }

    public void setValue(Object instance, String stringValue) throws IllegalAccessException {
        Object value;

        switch (type) {
            case STRING:
                value = stringValue;
                break;

            case INTEGER:
                value = Integer.parseInt(stringValue);
                break;

            default:
                throw new IllegalStateException("Unexpected parameter type: " + type);
        }

        field.set(instance, value);
    }

    public JobParameterType getType() {
        return type;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
