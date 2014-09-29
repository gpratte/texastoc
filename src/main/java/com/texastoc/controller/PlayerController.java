package com.texastoc.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.common.CellPhoneCarrier;
import com.texastoc.domain.Player;
import com.texastoc.service.PlayerService;

@Controller
public class PlayerController extends BaseController {

    @Autowired
    PlayerService playerService;

    @RequestMapping(value = "/admin/player/{id}", method = RequestMethod.GET)
    public ModelAndView getPlayer(final HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestParam(value="editing", required=false) Boolean editing) {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Player player = playerService.findById(Integer.valueOf(id));
        ModelAndView mav = new ModelAndView("player", "player", player);
        if (editing != null && editing) {
            mav.addObject("editing", new Boolean(true));
        } else {
            mav.addObject("editing", new Boolean(false));
        }
        mav.addObject("cellCarriers", CellPhoneCarrier.values());

        return mav;
    }

    @RequestMapping(value = "/json/admin/player/{id}", method = RequestMethod.GET)
    public @ResponseBody 
    Player getPlayerJson(final HttpServletRequest request,
            @PathVariable("id") String id) {

        if (this.isNotLoggedIn(request)) {
            return null;
        }

        return playerService.findById(Integer.valueOf(id));
    }

    @RequestMapping(value = "/admin/players", method = RequestMethod.GET)
    public ModelAndView getPlayers(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        List<Player> players = playerService.findAll();
        ModelAndView mav = new ModelAndView("players", "players", players);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }

    @RequestMapping(value = "/admin/player/new", method = RequestMethod.GET)
    public ModelAndView newPlayer(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Player player = new Player();
        ModelAndView mav = new ModelAndView("player", "player", player);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }

    @RequestMapping(value = "/admin/player/add", method = RequestMethod.POST)
    public ModelAndView createPlayer(final HttpServletRequest request,
            @Valid Player player, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        //;;!! shouldn't have to do this. The validate(..) method 
        // in Player should be invoked to do this
        player.validate(player, errors);
        
        if (StringUtils.equals(player.getCellCarrier(), "#")) {
            player.setCellCarrier(null);
        }
        
        ModelAndView mav = null;
        if (!errors.hasErrors()) {
            int id = playerService.create(player);
            List<Player> players = playerService.findAll();
            mav.addObject("cellCarriers", CellPhoneCarrier.values());
            mav = new ModelAndView("players", "players", players);
        } else {
            mav = new ModelAndView("player", "player", player);
            mav.addObject("editing", new Boolean(true));
        }
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }
    
    @RequestMapping(value = "/admin/player/update", method = RequestMethod.POST)
    public ModelAndView updatePlayer(final HttpServletRequest request,
            @Valid Player player, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        ModelAndView mav = new ModelAndView();
        mav.setViewName("player");
        
        player.validate(player, errors);
        
        if (StringUtils.equals(player.getCellCarrier(), "#")) {
            player.setCellCarrier(null);
        }

        if (!errors.hasErrors()) {
            // Update 
            playerService.update(player);
            // Get it
            player = playerService.findById(player.getId());
        } else {
            mav.addObject("editing", new Boolean(true));
        }
        mav.addObject("player", player);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }

    @RequestMapping(value = "/admin/player/delete/{id}", method = RequestMethod.GET)
    public ModelAndView deleteGamePlayer(final HttpServletRequest request,
            @PathVariable("id") Integer id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        playerService.delete(id);
        List<Player> players = playerService.findAll();
        ModelAndView mav = new ModelAndView("players", "players", players);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }
}
    