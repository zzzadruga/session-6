package ru.sbt.jschool.session6.JSONFormatter.types;

import org.apache.commons.lang3.StringUtils;
import ru.sbt.jschool.session6.JSONFormatter.JSONFormatter;
import ru.sbt.jschool.session6.JSONFormatter.JSONTypeFormatter;

import java.util.Map;

public class MapFormatter implements JSONTypeFormatter<Map> {
    @Override
    public String format(Map aMap, JSONFormatter formatter, int deep) throws IllegalAccessException {
        Map<Object, Object> map = aMap;
        if (map.size() == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[')
                .append('\n');
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            sb.append(sb.length() != 2 ? ",\n" : "")
                    .append(StringUtils.repeat(formatter.INDENT, deep + 1))
                    .append('"')
                    .append(formatter.marshall(entry.getKey(), deep + 1))
                    .append('"')
                    .append(":")
                    .append(' ')
                    .append(formatter.marshall(entry.getValue(), deep + 1));
        }
        return sb.append('\n')
                .append(StringUtils.repeat(formatter.INDENT, deep))
                .append(']')
                .toString();
    }
}
