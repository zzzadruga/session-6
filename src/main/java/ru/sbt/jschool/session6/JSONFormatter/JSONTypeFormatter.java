package ru.sbt.jschool.session6.JSONFormatter;

/**
 */
@FunctionalInterface
public interface JSONTypeFormatter<T> {
    String format(T t, JSONFormatter formatter, int deep) throws IllegalAccessException;
}
