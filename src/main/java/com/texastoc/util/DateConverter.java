package com.texastoc.util;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("MM/dd/yyyy");
    private static final DateTimeFormatter SQL_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    
    public static String getDateAsString(LocalDate date) {
        return date.toString(FORMATTER);
    }
    
    public static LocalDate getStringAsDate(String str) {
        return LocalDate.parse(str, FORMATTER);
    }

    public static String getDateAsSQLString(LocalDate date) {
        return date.toString(SQL_FORMATTER);
    }
    
}
