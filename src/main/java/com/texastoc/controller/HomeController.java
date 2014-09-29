package com.texastoc.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.Game;
import com.texastoc.service.GameService;

@Controller
public class HomeController extends BaseController {
    
    @Autowired
    GameService gameService;

    @RequestMapping("/")
    public String index(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) 
            return "login";

        return "home";
    }
    
    @RequestMapping("/admin/home")
    public String getHome(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) 
            return "login";
        return "adminhome";
    }

    @RequestMapping("/mobile")
    public ModelAndView goToMobile(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) 
            return new ModelAndView("login");
        Game game = gameService.findMostRecent();
        return new ModelAndView("mobilehome", "game", game);
    }
}
