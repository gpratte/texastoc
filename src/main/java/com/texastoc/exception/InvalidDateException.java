package com.texastoc.exception;

import org.joda.time.LocalDate;

public class InvalidDateException extends Exception {

    private LocalDate date;
    
    public InvalidDateException(LocalDate date, String message) {
        super(message);
        this.date = date;
    }
    
    public LocalDate getDate() {
        return date;
    }
}
