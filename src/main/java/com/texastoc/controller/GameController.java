package com.texastoc.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.Game;
import com.texastoc.domain.Player;
import com.texastoc.exception.CannotFinalizeException;
import com.texastoc.exception.CannotRandomizeException;
import com.texastoc.exception.CannotSetFinishException;
import com.texastoc.exception.InvalidDateException;
import com.texastoc.service.GameService;
import com.texastoc.service.PlayerService;

@Controller
public class GameController extends BaseController {

    static final Logger logger = Logger.getLogger(GameController.class);

    @Autowired
    GameService gameService;
    @Autowired
    PlayerService playerService;

    @RequestMapping(value = "/admin/game/{id}", method = RequestMethod.GET)
    public ModelAndView getGame(final HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestParam(value = "editing", required = false) Boolean editing) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Game game = gameService.findById(Integer.valueOf(id));
        ModelAndView mav = new ModelAndView("game", "game", game);
        if (editing != null && editing) {
            mav.addObject("editing", new Boolean(true));
        } else {
            mav.addObject("editing", new Boolean(false));
        }
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        return mav;
    }

    @RequestMapping(value = "/admin/game/new/{id}", method = RequestMethod.GET)
    public ModelAndView newGame(final HttpServletRequest request,
            @PathVariable("id") Integer seasonId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Game game = new Game();
        game.setSeasonId(seasonId);
        ModelAndView mav = new ModelAndView("game", "game", game);

        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        return mav;
    }

    @RequestMapping(value = "/admin/game/add", method = RequestMethod.POST)
    public ModelAndView createGame(final HttpServletRequest request,
            @Valid Game game, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        if (game.isFinalized()) {
            // ;;!! Should move this into the Game validator. Can
            // check if id is not set which means it is not yet created
            errors.rejectValue("finalized", "bogusCode",
                    "Cannot create a game as finalized");
        }

        game.validate(game, errors);
        if (!errors.hasErrors()) {
            try {
                // Create a new game
                int id = gameService.create(game);

                // Get the new game
                game = gameService.findById(id);
            } catch (InvalidDateException e) {
                if (game.getGameDate() == e.getDate()) {
                    errors.rejectValue("gameDate", "bogusCode", e.getMessage());
                }
            }
        }
        ModelAndView mav = new ModelAndView("game", "game", game);
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        return mav;
    }

    @RequestMapping(value = "/admin/game/update", method = RequestMethod.POST)
    public ModelAndView updateGame(final HttpServletRequest request,
            @Valid Game game, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        ModelAndView mav = new ModelAndView();
        mav.setViewName("game");
        game.validate(game, errors);
        if (!errors.hasErrors()) {
            try {
                // Update the game
                gameService.update(game);

                // Get the updated game
                game = gameService.findById(game.getId());
            } catch (InvalidDateException e) {
                if (game.getGameDate() == e.getDate()) {
                    errors.rejectValue("gameDate", "bogusCode", e.getMessage());
                }
                mav.addObject("editing", new Boolean(true));
            } catch (CannotFinalizeException e) {
                errors.rejectValue("finalized", "bogusCode", e.getMessage());
                mav.addObject("editing", new Boolean(true));
            }
        } else {
            mav.addObject("editing", new Boolean(true));
        }
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        mav.addObject("game", game);
        return mav;
    }

    @RequestMapping(value = "/admin/game/random/{id}", method = RequestMethod.GET)
    public ModelAndView random(final HttpServletRequest request,
            @PathVariable("id") String id) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Game game = gameService.findById(Integer.valueOf(id));
        ModelAndView mav = new ModelAndView("random", "game", game);
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        return mav;
    }

    @RequestMapping(value = "/admin/game/randomize/{id}", method = RequestMethod.POST)
    public ModelAndView randomize(
            final HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestParam(value = "tables", required = true) Integer numTables,
            @RequestParam(value = "pertable", required = true) Integer numPlayersPerTables,
            @RequestParam(value = "players", required = false) Object playersWOBuyIn)
            throws CannotRandomizeException {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        List<String> playersWOBuyInList = null;
        if (playersWOBuyIn != null) {
            if (playersWOBuyIn instanceof String) {
                playersWOBuyInList = new ArrayList<String>();
                playersWOBuyInList.add((String) playersWOBuyIn);
            } else if (playersWOBuyIn instanceof String[]) {
                playersWOBuyInList = Arrays.asList((String[]) playersWOBuyIn);
            }
        }
        gameService.randomizeSeats(Integer.valueOf(id), numTables,
                numPlayersPerTables, playersWOBuyInList, null, null);
        Game game = gameService.findById(Integer.valueOf(id));
        ModelAndView mav = new ModelAndView("game", "game", game);
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        return mav;
    }

    @RequestMapping(value = "/mobile/game/randomize/{id}", method = RequestMethod.POST)
    public ModelAndView randomizeMobile(
            final HttpServletRequest request,
            @PathVariable("id") Integer id,
            @RequestParam(value = "tables", required = true) Integer numTables,
            @RequestParam(value = "pertable", required = true) Integer numPlayersPerTables,
            @RequestParam(value = "playersWOBuyIn", required = false) Object playersWOBuyIn,
            @RequestParam(value = "playersWBuyIn", required = false) Object playersWBuyIn,
            @RequestParam(value = "table1seat1", required = false) String table1seat1
            ) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        List<String> playersWOBuyInList = null;
        if (playersWOBuyIn != null) {
            if (playersWOBuyIn instanceof String) {
                playersWOBuyInList = new ArrayList<String>();
                playersWOBuyInList.add((String) playersWOBuyIn);
            } else if (playersWOBuyIn instanceof String[]) {
                playersWOBuyInList = Arrays.asList((String[]) playersWOBuyIn);
            }
        }

        List<String> playersWBuyInList = null;
        if (playersWBuyIn != null) {
            if (playersWBuyIn instanceof String) {
                playersWBuyInList = new ArrayList<String>();
                playersWBuyInList.add((String) playersWBuyIn);
            } else if (playersWBuyIn instanceof String[]) {
                playersWBuyInList = Arrays.asList((String[]) playersWBuyIn);
            }
        }

        Player firstPlayer = null;
        if (StringUtils.isNotBlank(table1seat1) && !StringUtils.equals(table1seat1, "#")) {
            int firstPlayerId = Integer.parseInt(table1seat1);
            firstPlayer = playerService.findById(firstPlayerId);
        }

        ModelAndView mav = new ModelAndView("mobilerandomseating");
        try {
            gameService.randomizeSeats(Integer.valueOf(id), numTables,
                    numPlayersPerTables, playersWOBuyInList, playersWBuyInList, firstPlayer);
        } catch (CannotRandomizeException e) {
            mav.addObject("randomErrorMsg", e.getMessage());
        } finally {
            Game game = gameService.findById(id);
            mav.addObject("game", game);
            List<Player> players = playerService.findAll();
            mav.addObject("players", players);
        }
        return mav;
    }

    @RequestMapping(value = "/mobile/game/clear_seating/{id}", method = RequestMethod.GET)
    public ModelAndView clearSeatingMobile(
            final HttpServletRequest request,
            @PathVariable("id") Integer id) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        gameService.clearSeats(id);
        Game game = gameService.findById(id);
        return new ModelAndView("mobilegame", "game", game);
    }

    @RequestMapping(value = "/mobile/game/{id}", method = RequestMethod.GET)
    public ModelAndView getMobileGame(final HttpServletRequest request,
            @PathVariable("id") Integer id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Game game = gameService.findById(id);
        return new ModelAndView("mobilegame", "game", game);
    }

    @RequestMapping(value = "/mobile/game/finalize/{id}", method = RequestMethod.GET)
    public ModelAndView finalizeMobileGame(final HttpServletRequest request,
            @PathVariable("id") Integer id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        ModelAndView mav = new ModelAndView("mobilegame");
        Game game = gameService.findById(id);
        game.setFinalized(true);
        try {
            gameService.update(game);
        } catch (CannotFinalizeException e) {
            mav.addObject("errorMsg", e.getMessage());
        } catch(CannotSetFinishException e) {
            mav.addObject("errorMsg", e.getMessage());
        }
        game = gameService.findById(id);
        mav.addObject("game", game);
        return mav;
    }

    @RequestMapping(value = "/mobile/game/addpayout/{id}", method = RequestMethod.GET)
    public ModelAndView addpayoutMobileGame(final HttpServletRequest request,
            @PathVariable("id") Integer id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        gameService.changePayout(id, 1);
        Game game = gameService.findById(id);
        ModelAndView mav = new ModelAndView("mobilegame", "game", game);
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        return mav;
    }

    @RequestMapping(value = "/mobile/game/removepayout/{id}", method = RequestMethod.GET)
    public ModelAndView removepayoutMobileGame(final HttpServletRequest request,
            @PathVariable("id") Integer id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        gameService.changePayout(id, -1);
        Game game = gameService.findById(id);
        ModelAndView mav = new ModelAndView("mobilegame", "game", game);
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        return mav;
    }

    // ///////////////////////
    // ///
    // /// RESTful APIs
    // ///
    // ///////////////////////
    @RequestMapping(value = "/game/message/{id}", method = RequestMethod.POST)
    public @ResponseBody
    String messageSeating(final HttpServletRequest request,
            @PathVariable("id") Integer id) {
        logger.debug("message endpoint entered");
        gameService.messageSeats(id);
        return "OK from message seats";
    }

}
