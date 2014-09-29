package com.texastoc.exception;

import com.texastoc.domain.Quarter;

public class InvalidQuarterException extends Exception {

    private Quarter quarter;
    
    public InvalidQuarterException(Quarter quarter, String message) {
        super(message);
        this.quarter = quarter;
    }
    
    public Quarter getQuarter() {
        return quarter;
    }
}
