package com.zhoga.akka.utils;

public class Validations {
    /**
     * Simple check implementation for methods input (in order to not
     * add heavy dependencies to project)
     * @param predicate boolean value is checked and exception is thrown in case of false
     * @param message to setup exception
     */
    public static void require(final boolean predicate, final String message) {
        if (!predicate) {
            throw new IllegalArgumentException(message);
        }
    }
}
