package com.texastoc.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SystemController extends BaseController {
    
    private static final String FONT = "font";
    
    @RequestMapping("/system/font")
    public String changeFont(final HttpServletRequest request,
            @RequestParam(value="increase", required=true) Boolean increase) {
        if (this.isNotLoggedIn(request)) {
            return "login";
        }
        Integer fontIncrease = (Integer)request.getSession().getAttribute(FONT);
        if (fontIncrease == null) {
            if (increase) 
                fontIncrease = new Integer(1);
            else
                fontIncrease = new Integer(-1);
        } else {
            if (increase) 
                ++fontIncrease;
            else
                --fontIncrease;
        }
        request.getSession().setAttribute(FONT, fontIncrease);
        return "home";
    }
}
