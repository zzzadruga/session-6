package ru.sbt.jschool.session6.JSONFormatter.types;

import ru.sbt.jschool.session6.JSONFormatter.JSONFormatter;
import ru.sbt.jschool.session6.JSONFormatter.JSONTypeFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarFormatter implements JSONTypeFormatter<Calendar> {
    @Override public String format(Calendar calendar, JSONFormatter formatter, int deep) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format((calendar).getTime());
    }
}
