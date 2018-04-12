package ru.sbt.jschool.session6.JSONFormatter.types;

import ru.sbt.jschool.session6.JSONFormatter.JSONFormatter;
import ru.sbt.jschool.session6.JSONFormatter.JSONTypeFormatter;

public class StringFormatter implements JSONTypeFormatter<String> {
    @Override
    public String format(String s, JSONFormatter formatter, int deep) {
        return s;
    }
}
