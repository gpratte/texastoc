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
import com.texastoc.domain.SeasonPlayer;
import com.texastoc.service.SeasonPlayerService;
import com.texastoc.service.SeasonService;

@Controller
public class SeasonPlayerController extends BaseController {

    @Autowired
    SeasonService seasonService;
    @Autowired
    SeasonPlayerService seasonPlayerService;

    @RequestMapping(value = "/admin/seasonplayer/{id}", method = RequestMethod.GET)
    public ModelAndView getPlayer(final HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestParam(value = "seasonId", required = true) Integer seasonId) {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        SeasonPlayer player = seasonPlayerService.findById(Integer.valueOf(id));
        ModelAndView mav = new ModelAndView("seasonplayer", "player", player);
        mav.addObject("seasonId", seasonId);

        return mav;
    }

    @RequestMapping(value = "/admin/seasonplayer/update", method = RequestMethod.POST)
    public ModelAndView updatePlayer(final HttpServletRequest request,
            @RequestParam(value = "id", required = true) Integer id,
            @RequestParam(value = "forfeit", required = true) Boolean forfeit,
            @RequestParam(value = "wsop", required = true) Boolean wsop) throws Exception {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        SeasonPlayer player = seasonPlayerService.findById(id);
        player.setForfeit(forfeit);
        player.setWsop(wsop);
        
        // Shouldn't have to do this but setting @RequestParam required = false
        // was not working
        String tie = request.getParameter("tie");
        if (StringUtils.isNotBlank(tie)) {
            player.setTie(Integer.valueOf(tie));
        } else {
            player.setTie(null);
        }
        seasonPlayerService.update(player);

        Season season = seasonService.findById(player.getSeasonId());
        ModelAndView mav = new ModelAndView("season", "season", season);
        mav.addObject("editing", new Boolean(false));
        return mav;
    }

}
