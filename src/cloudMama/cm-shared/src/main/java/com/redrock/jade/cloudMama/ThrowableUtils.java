package com.redrock.jade.cloudMama;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Copyright RedRock 2013-14
 */
public class ThrowableUtils {
    public static String getFullDescription(Throwable throwable) {
        StringBuilder descBuilder = new StringBuilder();

        ThrowableUtils.getThrowableDescriptionRecursively(throwable, descBuilder, 0);

        return descBuilder.toString();
    }

    private static void getThrowableDescriptionRecursively(Throwable throwable, StringBuilder descBuilder, int depth) {
        String message = Strings.isNullOrEmpty(throwable.getMessage()) ? "N/A" : throwable.getMessage();

        descBuilder.append("--------------------- ").append(depth).append(" ------------------------").append('\n');
        descBuilder.append("Type: ").append(throwable.getClass().getName()).append('\n');
        descBuilder.append("Message: ").append(message).append('\n');
        descBuilder.append("Stack trace: ").append('\n').append(Throwables.getStackTraceAsString(throwable)).append('\n');
        descBuilder.append("-------------------------------------------------").append('\n');

        if (throwable.getCause() != null) {
            ThrowableUtils.getThrowableDescriptionRecursively(throwable.getCause(), descBuilder, depth + 1);
        }
    }
}
