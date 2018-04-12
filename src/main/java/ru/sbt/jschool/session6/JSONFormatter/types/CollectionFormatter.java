package ru.sbt.jschool.session6.JSONFormatter.types;

import org.apache.commons.lang3.StringUtils;
import ru.sbt.jschool.session6.JSONFormatter.JSONFormatter;
import ru.sbt.jschool.session6.JSONFormatter.JSONTypeFormatter;

import java.util.Collection;

public class CollectionFormatter implements JSONTypeFormatter<Collection> {
    @Override public String format(Collection collection, JSONFormatter formatter, int deep) throws IllegalAccessException {
        if (collection.size() == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[').append('\n');
        for (Object element : collection) {
            sb.append(sb.length() != 2 ? ",\n" : "")
                    .append(StringUtils.repeat(formatter.INDENT, deep + 1))
                    .append(formatter.marshall(element, deep + 1));
        }
        sb.append('\n')
                .append(StringUtils.repeat(formatter.INDENT, deep))
                .append(']');
        return sb.toString();
    }
}
