package com.texastoc.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.Game;
import com.texastoc.service.GameService;

@Controller
public class LoginController extends BaseController {
    
    static final Logger logger = Logger.getLogger(LoginController.class);
    
    static int count = 0;

    @Autowired
    GameService gameService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login(final HttpServletRequest request) {
        if (this.isAdminLoggedIn(request)) {
            return new ModelAndView("adminhome");            
        }
        if (this.isMobileLoggedIn(request)) {
            Game game = gameService.findMostRecent();
            return new ModelAndView("mobilehome", "game", game);
        }
        return new ModelAndView("mobilelogin");
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView processLogin(final HttpServletRequest request,
            @RequestParam(value="user", required=true) String user,
            @RequestParam(value="password", required=true) String password) {
        request.getSession().removeAttribute(USER_LOGGED_IN);
        
        if (StringUtils.equals(user, "admin") && StringUtils.equals(password, "wsop2014")) {
            request.getSession().setAttribute(USER_LOGGED_IN, "admin");
            return new ModelAndView("adminhome");            
        }
        if (StringUtils.equals(user, "toc") && StringUtils.equals(password, "shipit")) {
            request.getSession().setAttribute(USER_LOGGED_IN, "mobile");
            logger.info(++count + " toc logins");
            Game game = gameService.findMostRecent();
            return new ModelAndView("mobilehome", "game", game);
        }
        return new ModelAndView("mobilelogin", "oops", "Try again");
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(final HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGGED_IN);
        return "mobilelogin";
    }
}
