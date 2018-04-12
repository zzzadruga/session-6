package ru.sbt.jschool.session6.JSONFormatter;

import ru.sbt.jschool.session6.JSONFormatter.types.*;
import ru.sbt.jschool.session6.User;

import java.util.*;

/**
 * @author NIzhikov
 */
public class JSONFormatterImpl implements JSONFormatter {
    private Map<Class, JSONTypeFormatter> types = new HashMap<>();

    public JSONFormatterImpl() {
        types.put(Date.class, new DateFormatter());
        types.put(Calendar.class, new CalendarFormatter());
        types.put(Collection.class, new CollectionFormatter());
        types.put(Map.class, new MapFormatter());
        types.put(Number.class, new NumberFormatter());
        types.put(String.class, new StringFormatter());
        types.put(Object.class, new ObjectFormatter());
    }
    @Override
    public String marshall(Object obj){
        return marshall(obj, 0);
    }

    @Override
    public String marshall(Object obj, int deep) {
        if (obj == null)
            return "";
        Map<String, Object> ctx = new HashMap<>();
        try {
            if (types.containsKey(obj.getClass())) {
                return types.get(obj.getClass()).format(obj, this, deep);
            }
            if (obj instanceof Date) {
                return types.get(Date.class).format(obj, this, deep);
            }
            if (obj instanceof Number) {
                return types.get(Number.class).format(obj, this, deep);
            }
            if (obj instanceof Calendar) {
                return types.get(Calendar.class).format(obj, this, deep);
            }
            if (obj instanceof Collection || obj.getClass().isArray()) {
                return types.get(Collection.class).format(obj.getClass().isArray() ? Arrays.asList(obj) : obj, this, deep);
            }
            if (obj instanceof Map) {
                return types.get(Map.class).format(obj, this, deep);
            }
            return types.get(Object.class).format(obj, this, deep);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public <T> boolean addType(Class<T> clazz, JSONTypeFormatter<T> format) {
        return types.put(clazz, format) != null;
    }
}
