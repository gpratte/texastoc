package com.texastoc.domain;

import org.apache.commons.lang.StringUtils;

public enum SupplyType {
    CARDS("Cards"), CHAIR("Chair(s)"), CHIPS("Chips"), 
    TABLE("Table(s)"), TEXASTOC("texastoc.com"), OTHER("Other");

    private String text;

    SupplyType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
    
    public static SupplyType fromString(String value) {
        SupplyType[] supplyTypes = SupplyType.values();
        for(SupplyType supplyType : supplyTypes) {
            if (StringUtils.equals(supplyType.getText(), value)) {
                return supplyType;
            }
        }
        
        throw new IllegalArgumentException("Unknown supply type: " + value);
    }
}
