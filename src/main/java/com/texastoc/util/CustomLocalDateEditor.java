package com.texastoc.util;

import java.beans.PropertyEditorSupport;

public class CustomLocalDateEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        if (getValue() == null)
            return "";

        return "";
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
    }
}
