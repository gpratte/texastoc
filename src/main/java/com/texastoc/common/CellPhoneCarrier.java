package com.texastoc.common;

public enum CellPhoneCarrier {
    Alltel ("Alltel", "@message.alltel.com"),
    ATT ("AT&T", "@txt.att.net"),
    BoostMobile ("Boost Mobile", "@myboostmobile.com"),
    Cricket ("Cricket", "@sms.mycricket.com"),
    MetroPCS ("Metro PCS", "@mymetropcs.com"),
    Nextel ("Nextel", "@messaging.nextel.com"),
    Ptel ("Ptel", "@ptel.com"),
    Qwest ("Qwest", "@qwestmp.com"),
    Sprint ("Sprint", "@messaging.sprintpcs.com"),
    Suncom ("Suncom", "@tms.suncom.com"),
    TMobile ("T-Mobile", "@tmomail.net"),
    Tracfone ("Tracfone", "@mmst5.tracfone.com"),
    Verizon ("Verizon", "@vtext.com"),
    VirginMobile ("Virgin Mobile", "@vmobl.com"),
    USCellular ("U.S. Cellular", "@email.uscc.net");
    
    private String name;
    private String carrier;
    CellPhoneCarrier(String name, String carrier) {
        this.name = name;
        this.carrier = carrier;
    }
    
    public String getName() {
        return name;
    }
    public String getCarrier() {
        return carrier;
    }
}
