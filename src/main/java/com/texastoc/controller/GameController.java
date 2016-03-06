package com.texastoc.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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

import com.texastoc.common.HomeGame;
import com.texastoc.domain.Game;
import com.texastoc.domain.Player;
import com.texastoc.domain.Season;
import com.texastoc.domain.Supply;
import com.texastoc.domain.SupplyType;
import com.texastoc.domain.clock.Clock;
import com.texastoc.exception.CannotFinalizeException;
import com.texastoc.exception.CannotRandomizeException;
import com.texastoc.exception.CannotSetFinishException;
import com.texastoc.exception.InvalidDateException;
import com.texastoc.service.ClockService;
import com.texastoc.service.GameService;
import com.texastoc.service.PlayerService;
import com.texastoc.service.ProcurementService;
import com.texastoc.service.SeasonService;
import com.texastoc.service.mail.MailService;

@Controller
public class GameController extends BaseController {

    static final Logger logger = Logger.getLogger(GameController.class);

    @Autowired
    private GameService gameService;
    @Autowired
    private MailService mailService;
    @Autowired
    private ClockService clockService;
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private ProcurementService procurementService;


    @RequestMapping(value = "/admin/game/{id}", method = RequestMethod.GET)
    public ModelAndView getGame(final HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestParam(value = "editing", required = false) Boolean editing,
            @RequestParam(value = "deleting", required = false) Boolean deleting
            ) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Game game = gameService.findById(Integer.valueOf(id));
        ModelAndView mav = null;
        if (deleting != null && deleting) {
        	gameService.delete(game.getId());
            Season season = seasonService.findById(Integer.valueOf(game.getSeasonId()));
            mav = new ModelAndView("season", "season", season);
            mav.addObject("editing", new Boolean(false));
            return mav;
        } else if (editing != null && editing) {
            mav = new ModelAndView("game", "game", game);
            mav.addObject("editing", new Boolean(true));
        } else {
            mav = new ModelAndView("game", "game", game);
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
        
        throw new RuntimeException("No longer supported");

//        List<String> playersWOBuyInList = null;
//        if (playersWOBuyIn != null) {
//            if (playersWOBuyIn instanceof String) {
//                playersWOBuyInList = new ArrayList<String>();
//                playersWOBuyInList.add((String) playersWOBuyIn);
//            } else if (playersWOBuyIn instanceof String[]) {
//                playersWOBuyInList = Arrays.asList((String[]) playersWOBuyIn);
//            }
//        }
//        gameService.randomizeSeats(id, numTables,
//                numPlayersPerTables, playersWOBuyInList, null, null);
//        Game game = gameService.findById(Integer.valueOf(id));
//        ModelAndView mav = new ModelAndView("game", "game", game);
//        List<Player> players = playerService.findAll();
//        mav.addObject("players", players);
//        return mav;
    }

    @RequestMapping(value = "/mobile/game/new", method = RequestMethod.GET)
    public ModelAndView newMobileGame(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        Season season = seasonService.getCurrent();
        Game game = new Game();
        game.setSeasonId(season.getId());
        ModelAndView mav = new ModelAndView("mobilenewgame", "game", game);

        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        return mav;
    }

    @RequestMapping(value = "/mobile/game/add", method = RequestMethod.POST)
    public ModelAndView createMobileGame(final HttpServletRequest request,
            @Valid Game game, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        int seasonId = game.getSeasonId();

        game.validate(game, errors);
        if (!errors.hasErrors()) {
            try {
                // Create a new game
                gameService.create(game);
            } catch (InvalidDateException e) {
                if (game.getGameDate() == e.getDate()) {
                    errors.rejectValue("gameDate", "bogusCode", e.getMessage());
                }
            }
        }

        if (errors.hasErrors()) {
            game = new Game();
            game.setSeasonId(seasonId);
            ModelAndView mav = new ModelAndView("mobilenewgame", "game", game);

            List<Player> players = playerService.findAll();
            mav.addObject("players", players);
            mav.addObject("errors", errors);
            return mav;
        } else {
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
    }

    @RequestMapping(value = "/mobile/game/randomize/{id}", method = RequestMethod.POST)
    public ModelAndView randomizeMobile(
            final HttpServletRequest request,
            @PathVariable("id") Integer id,
            @RequestParam(value = "numTable1", required = true) Integer numTable1,
            @RequestParam(value = "numTable2", required = false) Integer numTable2,
            @RequestParam(value = "numTable3", required = false) Integer numTable3,
            @RequestParam(value = "numTable4", required = false) Integer numTable4,
            @RequestParam(value = "numTable5", required = false) Integer numTable5,
            @RequestParam(value = "playersWOBuyIn", required = false) Object playersWOBuyIn,
            @RequestParam(value = "playersWBuyIn", required = false) Object playersWBuyIn,
            @RequestParam(value = "table1seat", required = false) String table1seat
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
        if (StringUtils.isNotBlank(table1seat) && !StringUtils.equals(table1seat, "#")) {
            int firstPlayerId = Integer.parseInt(table1seat);
            firstPlayer = playerService.findById(firstPlayerId);
        }

        ModelAndView mav = new ModelAndView("mobilerandomseating");
        try {
            List<Integer> playersPerTable = new ArrayList<Integer>();
            playersPerTable.add(numTable1);
            if (numTable2 != null && numTable2 > 0) {
                playersPerTable.add(numTable2);
            }
            if (numTable3 != null && numTable3 > 0) {
                playersPerTable.add(numTable3);
            }
            if (numTable4 != null && numTable4 > 0) {
                playersPerTable.add(numTable4);
            }
            if (numTable5 != null && numTable5 > 0) {
                playersPerTable.add(numTable5);
            }
            gameService.randomizeSeats(Integer.valueOf(id), playersWOBuyInList, 
                    playersWBuyInList, firstPlayer, playersPerTable);
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

    @RequestMapping(value = "/mobile/game/assignseating/{id}", method = RequestMethod.POST)
    public ModelAndView assignSeatingMobile(
            final HttpServletRequest request,
            @PathVariable("id") Integer id,
            @RequestParam(value = "playersWOBuyIn", required = false) Object playersWOBuyIn,
            @RequestParam(value = "playersWBuyIn", required = false) Object playersWBuyIn
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

        ModelAndView mav = new ModelAndView("mobilegame");
        try {
            gameService.assignSeats(id, 
                    playersWBuyInList, 
                    playersWOBuyInList);
        } catch (CannotRandomizeException e) {
            logger.warn("Could not assign seats", e);
            mav.addObject("seatingErrorMsg", e.getMessage());
        }
        Game game = gameService.findById(id);
        mav.addObject("game", game);
        mav.addObject("host", playerService.findById(game.getHostId()));
        Clock clock = clockService.getClock(id);
        mav.addObject("clock", clock);
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
        ModelAndView mav = new ModelAndView("mobilegame", "game", game);
        mav.addObject("host", playerService.findById(game.getHostId()));
        Clock clock = clockService.getClock(id);
        mav.addObject("clock", clock);
        return mav;
    }

    @RequestMapping(value = "/mobile/game/current", method = RequestMethod.GET)
    public ModelAndView getMobileCurrentGame(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Game game = gameService.findMostRecent();
        return this.getMobileGame(request, response, game.getId(), null);

    }

    
    @RequestMapping(value = "/mobile/game/{id}", method = RequestMethod.GET)
    public ModelAndView getMobileGame(final HttpServletRequest request,
    		final HttpServletResponse response,
            @PathVariable("id") Integer id,
            @RequestParam(value = "deleting", required = false) Boolean deleting) 
            		throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Game game = gameService.findById(id);

        if (deleting != null && deleting) {
        	gameService.delete(game.getId());
        	response.sendRedirect("/toc/mobile/seasons");
        	return null;
        }
        
        ModelAndView mav = new ModelAndView("mobilegame", "game", game);
        Clock clock = clockService.getClock(id);
        mav.addObject("clock", clock);
        mav.addObject("host", playerService.findById(game.getHostId()));
        return mav;
    }

    @RequestMapping(value = "/mobile/game/finalize/{id}", method = RequestMethod.GET)
    public ModelAndView finalizeMobileGame(final HttpServletRequest request,
            @PathVariable("id") Integer id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        ModelAndView mav = new ModelAndView("mobilegame");
        boolean problem = false;
        try {
            gameService.finalize(id);
        } catch (CannotFinalizeException e) {
            mav.addObject("errorMsg", e.getMessage());
            problem = true;
        } catch(CannotSetFinishException e) {
            mav.addObject("errorMsg", e.getMessage());
            problem = true;
        }
        
        Game game = gameService.findById(id);

        if (problem) {
            mav.addObject("game", game);
            Clock clock = clockService.getClock(id);
            mav.addObject("clock", clock);
            return mav;
        }

        List<Player> players = playerService.findAll();
        for (Player player : players) {
            if ("Pratte".equals(player.getLastName()) || 
                    "Lendeckyy".equals(player.getLastName())) {
            	if (game.getHomeGame() == HomeGame.TOC) {
                    seasonService.emailSeasonSummary(id, player.getId());
            	}
            }
            if ("Pratte".equals(player.getLastName()) ||
            		"Lyons".equals(player.getLastName())) {
            	if (game.getHomeGame() == HomeGame.CPPL) {
                    seasonService.emailSeasonSummary(id, player.getId());
            	}
            }
        }

        clockService.endClock(id);

        mav = new ModelAndView("mobilehome", "game", game);
        Season season = seasonService.findById(game.getSeasonId());
        mav.addObject("season", season);
        mav.addObject("homegames", HomeGame.values());
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
        mav.addObject("host", playerService.findById(game.getHostId()));
        Clock clock = clockService.getClock(id);
        mav.addObject("clock", clock);
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
        mav.addObject("host", playerService.findById(game.getHostId()));
        Clock clock = clockService.getClock(id);
        mav.addObject("clock", clock);
        return mav;
    }

    @RequestMapping(value = "/mobile/game/procure/{id}", method = RequestMethod.GET)
    public ModelAndView procure(final HttpServletRequest request,
            @PathVariable("id") Integer id) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        Game game = gameService.findById(id);
        List<Supply> supplies = procurementService.getSuppliesForGame(id);
        
        ModelAndView mav = new ModelAndView("mobilegameprocurement");
        mav.addObject("gameId", id);
        mav.addObject("supplies", supplies);
        mav.addObject("types", SupplyType.values());
        return mav;
    }

    @RequestMapping(value = "/mobile/game/procure/{id}", method = RequestMethod.POST)
    public ModelAndView procurement(final HttpServletRequest request,
            @PathVariable("id") Integer id,
            @RequestParam(value = "supplytype", required = false) String supplyType,
            @RequestParam(value = "potamount", required = false) String potAmount,
            @RequestParam(value = "tocamount", required = false) String tocAmount,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "delete", required = false) List<Integer> deletes) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        List<String> errors = new ArrayList<String>();
        if (!StringUtils.isBlank(potAmount) && !NumberUtils.isDigits(potAmount)) {
            errors.add("Pot Amount must be a dollar amount");
        }
        if (!StringUtils.isBlank(tocAmount) && !NumberUtils.isDigits(tocAmount)) {
            errors.add("TOC Amount must be a dollar amount");
        }

        if (errors.size() == 0 && 
                (!StringUtils.isBlank(tocAmount) || !StringUtils.isBlank(potAmount))) {
            Supply supply = new Supply();
            supply.setGameId(id);
            supply.setType(SupplyType.fromString(supplyType));
            if (!StringUtils.isBlank(potAmount)) {
                supply.setPrizePotAmount(new Integer(potAmount));
            }
            if (!StringUtils.isBlank(tocAmount)) {
                supply.setAnnualTocAmount(new Integer(tocAmount));
            }
            if (!StringUtils.isBlank(description)) {
                supply.setDescription(description);
            }
            procurementService.procure(supply);
        }
        
        if (deletes != null) {
            for (Integer supplyId : deletes) {
                procurementService.delete(supplyId);
            }
        }
        
        Game game = gameService.findById(id);
        List<Supply> supplies = procurementService.getSuppliesForGame(id);
        
        ModelAndView mav = new ModelAndView("mobilegameprocurement");
        mav.addObject("gameId", id);
        mav.addObject("supplies", supplies);
        mav.addObject("types", SupplyType.values());
        mav.addObject("errors", errors);
        return mav;
    }

    @RequestMapping(value = "/mobile/game/transport/{id}", method = RequestMethod.GET)
    public ModelAndView updateTransport (
            final HttpServletRequest request,
            @PathVariable("id") Integer id,
            @RequestParam(value = "required", required = true) Boolean required) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        gameService.updateTransport(id, required);
        Game game = gameService.findById(id);
        ModelAndView mav = new ModelAndView("mobilegame", "game", game);
        mav.addObject("host", playerService.findById(game.getHostId()));
        Clock clock = clockService.getClock(id);
        mav.addObject("clock", clock);
        return mav;
    }

    @RequestMapping(value = "/mobile/game/rally/{id}", method = RequestMethod.GET)
    public ModelAndView rally (
            final HttpServletRequest request,
            @PathVariable("id") Integer id) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Game game = gameService.findById(id);
        ModelAndView mav = new ModelAndView("mobilerally", "game", game);
        
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        return mav;
    }

    @RequestMapping(value = "/mobile/game/sendrally/{id}", method = RequestMethod.POST)
    public ModelAndView sendRally (
            final HttpServletRequest request,
            @PathVariable("id") Integer id,
            @RequestParam(value = "playerId", required = false) Integer playerId,
            @RequestParam(value = "textbody", required = false) String body) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Game game = gameService.findById(id);
        if (StringUtils.isNotBlank(body)) {
            List<Player> activePlayers = playerService.findActive();

            Player host = null;
            if (game.getHostId() != null) {
                host = playerService.findById(game.getHostId());
            }

            String loggedInUserEmail = getLoggedIn(request);
            Player fromPlayer = playerService.findByEmail(loggedInUserEmail);

            mailService.sendRally(fromPlayer, host, activePlayers, body, game.getGameDate());
        }
        
        ModelAndView mav = new ModelAndView("mobilegame", "game", game);
        mav.addObject("host", playerService.findById(game.getHostId()));
        Clock clock = clockService.getClock(id);
        mav.addObject("clock", clock);
        return mav;
    }



    // ///////////////////////
    // ///
    // /// RESTful APIs
    // ///
    // ///////////////////////
    @RequestMapping(value = "/game/message/{id}", method = RequestMethod.POST)
    public @ResponseBody
    String messageSeating(@PathVariable("id") Integer id) {
        logger.debug("message endpoint entered");
        gameService.messageSeats(id);
        gameService.seated(id);
        return "OK from message seats";
    }

    @RequestMapping(value = "/game/homegame", method = RequestMethod.POST)
    public @ResponseBody 
    String updateHomeGame(@RequestParam(value = "gameId", required = true) Integer gameId,
    		@RequestParam(value = "marker", required = true) Integer marker) {

        gameService.updateHomeGame(gameId, marker);
        return "OK from update home game";    
    }
    
}
