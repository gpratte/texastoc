package com.texastoc.configure;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

// http://docs.spring.io/spring/docs/3.1.0.M1/spring-framework-reference/html/validation.html#beans-beans
public final class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {

    public void registerCustomEditors(PropertyEditorRegistry registry) {

        // it is expected that new PropertyEditor instances are created
        //registry.registerCustomEditor(ExoticType.class, new ExoticTypeEditor());
    }
}