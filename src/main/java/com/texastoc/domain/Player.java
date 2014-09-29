package com.texastoc.domain;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class Player implements Validator {
    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String cellCarrier;
    private String address;
    private String note;
    private boolean active;
    
    private transient String fullName;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        fullName = null;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
        fullName = null;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getCellCarrier() {
        return cellCarrier;
    }
    public void setCellCarrier(String cellCarrier) {
        this.cellCarrier = cellCarrier;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public String getFullName() {
        if (fullName != null) {
            return fullName;
        }
        
        StringBuilder name = null;
        if (! StringUtils.isEmpty(firstName)) {
            name = new StringBuilder();
            name.append(firstName);
            if (! StringUtils.isEmpty(lastName)) {
                name.append(" " + lastName);
            }
        } else if (! StringUtils.isEmpty(lastName)) {
            name = new StringBuilder();
            name.append(lastName);
        } 

        if (name != null) {
            fullName = name.toString();
        }
        
        return fullName;
    }
    
    /////////////////////
    // Validation stuff
    /////////////////////
     public boolean supports(Class clazz) {
         return Player.class.equals(clazz);
     }

     public void validate(Object obj, Errors errors) {

         //;;!! not getting invoked
         Player player = (Player)obj;
         if (StringUtils.isEmpty(player.getFirstName()) && 
                 StringUtils.isEmpty(player.getLastName())) {
             errors.rejectValue("firstName", "firstOrLastName",
                     "Must have either a first name or a last name");
         }
     }

}
