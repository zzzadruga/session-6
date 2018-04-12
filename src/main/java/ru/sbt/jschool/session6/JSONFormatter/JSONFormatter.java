package ru.sbt.jschool.session6.JSONFormatter;

/**
 */
public interface JSONFormatter {
    String INDENT = "    ";
    /**
     * Marshall object to JSON string.
     *
     * @param obj Any object.
     * @return String in JSON format.
     */
    String marshall(Object obj) throws IllegalAccessException;

    /**
     * Marshall object to JSON string.
     *
     * @param obj Any object, deep - indent for print.
     * @return String in JSON format.
     */

    String marshall(Object obj, int deep) throws IllegalAccessException;

    /**
     * Add predefined type to formatter.
     *
     * @param clazz Class to add.
     * @param format Formatter for specified class.
     * @param <T> Formatted type.
     * @return True if class been previously known.
     */
    <T> boolean addType(Class<T> clazz, JSONTypeFormatter<T> format);
}
