package com.texastoc.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.Game;
import com.texastoc.domain.Season;
import com.texastoc.domain.SeasonChampion;
import com.texastoc.domain.clock.Level;
import com.texastoc.service.ClockService;
import com.texastoc.service.GameService;
import com.texastoc.service.SeasonService;

@Controller
public class HomeController extends BaseController {
    
    static final Logger logger = Logger.getLogger(HomeController.class);

    @Autowired
    GameService gameService;
    @Autowired
    ClockService clockService;
    @Autowired
    SeasonService seasonService;

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

        Boolean allowStartNewGame = false;
        Boolean allowGoToCurrentGame = false;
        Season currentSeason = seasonService.getCurrent();
        if (! currentSeason.isFinalized()) {
            Game currentGame = gameService.findMostRecent();
            if (currentGame.isFinalized()) {
                allowStartNewGame = true;
            } else {
                allowGoToCurrentGame = true;
            }
        }
        ModelAndView mav = new ModelAndView("mobilehome");
        mav.addObject("allowStartNewGame", allowStartNewGame);
        mav.addObject("allowGoToCurrentGame", allowGoToCurrentGame);
        return mav;
    }
    
    @RequestMapping("/mobile/numbers")
    public ModelAndView getMobileNumbers(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilenumbers");
    }

    @RequestMapping("/mobile/blinds")
    public ModelAndView getMobileBlinds(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        List<Level> levels = clockService.getLevels();
        return new ModelAndView("mobileblinds", "levels", levels);
    }


}
