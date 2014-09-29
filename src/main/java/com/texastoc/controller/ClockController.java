package com.texastoc.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.clock.Clock;
import com.texastoc.service.ClockService;

@Controller
public class ClockController extends BaseController {

    static final Logger logger = Logger.getLogger(ClockController.class);

    @Autowired
    private ClockService clockService;
    
    @RequestMapping(value = "/mobile/clock", method = RequestMethod.GET)
    public ModelAndView getClock(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Clock clock = clockService.getClock();
        clock.sync();
        return new ModelAndView("mobileclock", "clock", clock);
    }

    @RequestMapping(value = "/mobile/clock", method = RequestMethod.POST)
    public ModelAndView updateClock(final HttpServletRequest request,
            @RequestParam(value = "activity", required = true) String activity,
            @RequestParam(value = "round", required = false) String round,
            @RequestParam(value = "minutes", required = false) Integer minutes,
            @RequestParam(value = "seconds", required = false) Integer seconds) {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        if ("play".equals(activity)) {
            clockService.getClock().start(round, minutes, seconds);
        } else if ("pause".equals(activity)) {
            clockService.getClock().pause();
        } else if ("reset".equals(activity)) {
            clockService.getClock().reset();;
        } else if ("next".equals(activity)) {
            clockService.getClock().goToNextLevel(round);
        } else if ("prev".equals(activity)) {
            clockService.getClock().goToPreviousLevel(round);
        }

        Clock clock = clockService.getClock();
        clock.sync();
        return new ModelAndView("mobileclock", "clock", clock);
    }

    @RequestMapping("/clock")
    public @ResponseBody 
    Clock getClockJson(final HttpServletRequest request) {
        Clock clock = clockService.getClock();
        clock.sync();
        return clock;
    }

}
