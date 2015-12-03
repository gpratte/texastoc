package com.texastoc.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.Season;
import com.texastoc.domain.SeasonHistoryEntry;
import com.texastoc.domain.SeasonPlayer;
import com.texastoc.service.SeasonHistoryService;
import com.texastoc.service.SeasonService;

@Controller
public class SeasonHistoryController extends BaseController {

    @Autowired
    SeasonService seasonService;
    @Autowired
    SeasonHistoryService seasonHistoryService;

    @RequestMapping(value = "/admin/seasonhistory/{playerId}", method = RequestMethod.GET)
    public ModelAndView getPlayer(final HttpServletRequest request,
            @PathVariable("playerId") Integer playerId,
            @RequestParam(value = "seasonId", required = true) Integer seasonId) {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        SeasonHistoryEntry entry = seasonHistoryService.findById(seasonId, playerId);
        ModelAndView mav = new ModelAndView("seasonhistory", "entry", entry);
        mav.addObject("seasonId", seasonId);

        return mav;
    }

    @RequestMapping(value = "/admin/seasonhistory/update", method = RequestMethod.POST)
    public ModelAndView updatePlayer(final HttpServletRequest request,
            @RequestParam(value = "playerId", required = true) Integer playerId,
            @RequestParam(value = "seasonId", required = true) Integer seasonId,
            @RequestParam(value = "wsop", required = true) Boolean wsop) throws Exception {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        SeasonHistoryEntry entry = seasonHistoryService.findById(seasonId, playerId);
        entry.setWsop(wsop);
        seasonHistoryService.update(entry);

        Season season = seasonService.findById(seasonId);
        ModelAndView mav = new ModelAndView("season", "season", season);
        mav.addObject("editing", new Boolean(false));
        return mav;
    }

}
