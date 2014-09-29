package com.texastoc.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FaqController extends BaseController {
    
    @RequestMapping("/mobile/faq")
    public ModelAndView getFaq(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaq");
    }

    @RequestMapping("/mobile/faq/chop")
    public ModelAndView getFaqChop(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaqchop");
    }

}
