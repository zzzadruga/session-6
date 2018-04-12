package ru.sbt.jschool.session6.JSONFormatter.types;

import ru.sbt.jschool.session6.JSONFormatter.JSONFormatter;
import ru.sbt.jschool.session6.JSONFormatter.JSONTypeFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class NumberFormatter implements JSONTypeFormatter<Number> {
    @Override
    public String format(Number number, JSONFormatter formatter, int deep) throws IllegalAccessException {
        if (number instanceof Float || number instanceof Double) {
            DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
            DecimalFormat decimalFormat = new DecimalFormat("###,##.00");
            formatSymbols.setDecimalSeparator('.');
            decimalFormat.setDecimalFormatSymbols(formatSymbols);
            return decimalFormat.format(number);
        } else {
            return number.toString();
        }
    }
}
