package com.redrock.jade.cloudMama;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Copyright RedRock 2013-14
 */
public class StreamUtils {
    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> Stream<T> stream(T[] array) {
        return Arrays.asList(array).stream();
    }
}
