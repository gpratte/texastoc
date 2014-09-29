package com.texastoc.common;


public enum Finish {
    FIRST(1, "1st"), SECOND(2, "2nd"), THIRD(3, "3rd"), 
    FOURTH(4, "4th"), FIFTH(5, "5th"), SIXTH(6, "6th"),
    SEVENTH(7, "7th"), EIGHTH(8, "8th"), NINTH(9, "9th"),
    TENTH(10, "10th");

    private int value;
    private String text;

    Finish(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }
    public String getText() {
        return text;
    }

}
