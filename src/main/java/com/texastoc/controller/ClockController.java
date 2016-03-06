package com.texastoc.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.clock.Clock;
import com.texastoc.service.ClockService;
import com.texastoc.service.GameService;

@Controller
public class ClockController extends BaseController {

    static final Logger logger = Logger.getLogger(ClockController.class);

    @Autowired
    private GameService gameService;
    @Autowired
    private ClockService clockService;
    
    @RequestMapping(value = "/mobile/clock/{gameId}", method = RequestMethod.GET)
    public ModelAndView getClock(final HttpServletRequest request,
    		@PathVariable("gameId") Integer gameId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Clock clock = clockService.getClock(gameId);
        return new ModelAndView("mobileclock", "clock", clock);
    }

    @RequestMapping(value = "/mobile/clock", method = RequestMethod.POST)
    public @ResponseBody 
    Clock updateClock(final HttpServletRequest request,
            @RequestParam(value = "gameId", required = false) Integer gameId,
            @RequestParam(value = "activity", required = true) String activity,
            @RequestParam(value = "round", required = false) String round) {

        if ("play".equals(activity)) {
            Clock clock = clockService.getClock(gameId);
            if ("Round 1".equals(clock.getCurrentLevel().getRound()) && 20 == clock.getRemainingMinutes() &&
                    0 == clock.getRemainingSeconds()) {
                gameService.recordStartTime(gameId);
            }
            clock.go();
        } else if ("pause".equals(activity)) {
            clockService.getClock(gameId).stop();
        } else if ("reset".equals(activity)) {
            clockService.getClock(gameId).reset();;
        } else if ("next".equals(activity)) {
            clockService.getClock(gameId).goToNextLevel();
        } else if ("prev".equals(activity)) {
            clockService.getClock(gameId).goToPreviousLevel();
        } else if ("round".equals(activity)) {
            clockService.getClock(gameId).setRound(round);

        } else if ("downMinutes".equals(activity)) {
            clockService.getClock(gameId).updateMinute(-1);
        } else if ("dblDownMinutes".equals(activity)) {
            clockService.getClock(gameId).updateMinute(-20);
        } else if ("upMinutes".equals(activity)) {
            clockService.getClock(gameId).updateMinute(1);
        } else if ("dblUpMinutes".equals(activity)) {
            clockService.getClock(gameId).updateMinute(20);

        } else if ("downSeconds".equals(activity)) {
            clockService.getClock(gameId).updateSecond(-1);
        } else if ("dblDownSeconds".equals(activity)) {
            clockService.getClock(gameId).updateSecond(-60);
        } else if ("upSeconds".equals(activity)) {
            clockService.getClock(gameId).updateSecond(1);
        } else if ("dblUpSeconds".equals(activity)) {
            clockService.getClock(gameId).updateSecond(60);
        
        } else {
        	logger.warn("unknown activity: " + activity);
        }

        Clock clock = clockService.getClock(gameId);
        return clock;
    }

    @RequestMapping("/clock/{gameId}")
    public @ResponseBody 
    Clock getClockJson(final HttpServletRequest request,
    		@PathVariable("gameId") Integer gameId) {
        Clock clock = clockService.getClock(gameId);
        return clock;
    }

}
