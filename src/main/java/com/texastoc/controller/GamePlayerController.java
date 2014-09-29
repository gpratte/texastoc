package com.texastoc.controller;

import java.util.ArrayList;
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
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.common.Finish;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Player;
import com.texastoc.exception.DuplicatePlayerException;
import com.texastoc.service.GamePlayerService;
import com.texastoc.service.GameService;
import com.texastoc.service.PlayerService;

@Controller
public class GamePlayerController extends BaseController {

    static final Logger logger = Logger.getLogger(GamePlayerController.class);

    @Autowired
    GameService gameService;
    @Autowired
    GamePlayerService gamePlayerService;
    @Autowired
    PlayerService playerService;

    @RequestMapping(value = "/admin/gameplayer/{id}", method = RequestMethod.GET)
    public ModelAndView getGame(final HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestParam(value = "editing", required = false) Boolean editing) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        GamePlayer gamePlayer = gamePlayerService.findById(Integer.valueOf(id));
        ModelAndView mav = new ModelAndView("gameplayer", "gamePlayer",
                gamePlayer);
        if (editing != null && editing) {
            mav.addObject("editing", new Boolean(true));
        } else {
            mav.addObject("editing", new Boolean(false));
        }
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);

        Game game = gameService.findById(gamePlayer.getGameId());
        if (game.isDoubleBuyIn()) {
            mav.addObject("buyIn", new Integer(60));
            mav.addObject("reBuy", new Integer(40));
        } else {
            mav.addObject("buyIn", new Integer(30));
            mav.addObject("reBuy", new Integer(20));
        }
        mav.addObject("finish", Finish.class);
        return mav;
    }

    @RequestMapping(value = "/admin/gameplayer/new/{id}", method = RequestMethod.GET)
    public ModelAndView newGamePlayer(final HttpServletRequest request,
            @PathVariable("id") Integer gameId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setGameId(gameId);
        ModelAndView mav = new ModelAndView("gameplayer", "gamePlayer",
                gamePlayer);
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        Game game = gameService.findById(gamePlayer.getGameId());
        if (game.isDoubleBuyIn()) {
            mav.addObject("buyIn", new Integer(60));
            mav.addObject("reBuy", new Integer(40));
        } else {
            mav.addObject("buyIn", new Integer(30));
            mav.addObject("reBuy", new Integer(20));
        }
        mav.addObject("finish", Finish.class);
        return mav;
    }

    @RequestMapping(value = "/admin/gameplayer/add", method = RequestMethod.POST)
    public ModelAndView createGamePlayer(final HttpServletRequest request,
            @Valid GamePlayer gamePlayer, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        try {
            int id = gamePlayerService.create(gamePlayer);
            Game game = gameService.findById(gamePlayer.getGameId());
            ModelAndView mav = new ModelAndView("game", "game", game);
            List<Player> players = playerService.findAll();
            mav.addObject("players", players);
            return mav;
        } catch (DuplicatePlayerException e) {
            errors.rejectValue("player", "bogusCode", e.getMessage());
            ModelAndView mav = new ModelAndView("gameplayer", "gamePlayer",
                    gamePlayer);
            List<Player> players = playerService.findAll();
            mav.addObject("players", players);
            Game game = gameService.findById(gamePlayer.getGameId());
            if (game.isDoubleBuyIn()) {
                mav.addObject("buyIn", new Integer(60));
                mav.addObject("reBuy", new Integer(40));
            } else {
                mav.addObject("buyIn", new Integer(30));
                mav.addObject("reBuy", new Integer(20));
            }
            mav.addObject("finish", Finish.class);
            return mav;
        }

    }

    @RequestMapping(value = "/admin/gameplayer/update", method = RequestMethod.POST)
    public ModelAndView updateGamePlayer(final HttpServletRequest request,
            @Valid GamePlayer gamePlayer, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        ModelAndView mav = new ModelAndView();
        mav.setViewName("gameplayer");
        if (!errors.hasErrors()) {
            try {
                // Update
                gamePlayerService.update(gamePlayer);
                // Get it
                gamePlayer = gamePlayerService.findById(gamePlayer.getId());
            } catch (DuplicatePlayerException e) {
                errors.rejectValue("playerId", "bogusCode", e.getMessage());
                mav.addObject("editing", new Boolean(true));
            }
        } else {
            mav.addObject("editing", new Boolean(true));
        }
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        mav.addObject("gamePlayer", gamePlayer);
        Game game = gameService.findById(gamePlayer.getGameId());
        if (game.isDoubleBuyIn()) {
            mav.addObject("buyIn", new Integer(60));
            mav.addObject("reBuy", new Integer(40));
        } else {
            mav.addObject("buyIn", new Integer(30));
            mav.addObject("reBuy", new Integer(20));
        }
        mav.addObject("finish", Finish.class);
        return mav;
    }

    @RequestMapping(value = "/admin/gameplayer/delete/{id}", method = RequestMethod.GET)
    public ModelAndView deleteGamePlayer(final HttpServletRequest request,
            @PathVariable("id") Integer id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        GamePlayer gamePlayer = gamePlayerService.findById(id);
        gamePlayerService.delete(id);
        Game game = gameService.findById(gamePlayer.getGameId());
        ModelAndView mav = new ModelAndView("game", "game", game);
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);
        return mav;
    }

    @RequestMapping(value = "/mobile/gameplayer/add/{id}", method = RequestMethod.GET)
    public ModelAndView showMobileGamePlayers(final HttpServletRequest request,
            @PathVariable("id") Integer gameId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        ModelAndView mav = new ModelAndView("mobilegameplayers");

        mav.addObject("gameId", gameId);

        List<Player> players = playerService.findAll();
        Game game = gameService.findById(gameId);
        
        // Remove players already in game.
        ArrayList<Player> playersNotInGame = new ArrayList<Player>();
        for (Player player : players) {
            boolean found = false;
            for (GamePlayer gamePlayer : game.getPlayers()) {
                if (player.getId() == gamePlayer.getPlayerId()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                playersNotInGame.add(player);
            }
        }
        
        mav.addObject("players", playersNotInGame);

        if (game.isDoubleBuyIn()) {
            mav.addObject("buyIn", new Integer(60));
            mav.addObject("reBuy", new Integer(40));
        } else {
            mav.addObject("buyIn", new Integer(30));
            mav.addObject("reBuy", new Integer(20));
        }
        return mav;
    }

    @RequestMapping(value = "/mobile/gameplayer/addfng/{id}", method = RequestMethod.GET)
    public ModelAndView addMobileFNG(final HttpServletRequest request,
            @PathVariable("id") Integer gameId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        ModelAndView mav = new ModelAndView("mobilefng", "gameId", gameId);

        return mav;
    }

    @RequestMapping(value = "/mobile/gameplayer/add", method = RequestMethod.POST)
    public ModelAndView addMobileGamePlayers(final HttpServletRequest request,
            @RequestParam(value = "gameId", required = true) Integer gameId)
            throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Game game = gameService.findById(gameId);

        for (int i = 1; i <= 5; ++i) {
            String playerId = request.getParameter("playerId--" + i);
            if (StringUtils.equals(playerId, "0")) {
                continue;
            }

            String toc = request.getParameter("toc--" + i);
            String qtoc = request.getParameter("qtoc--" + i);
            String buyin = request.getParameter("buyin--" + i);

            GamePlayer gamePlayer = new GamePlayer();
            gamePlayer.setGameId(gameId);
            gamePlayer.setPlayerId(Integer.parseInt(playerId));

            if (StringUtils.isNotBlank(toc)) {
                gamePlayer.setAnnualTocPlayer(true);
            }

            if (StringUtils.isNotBlank(qtoc)) {
                gamePlayer.setQuarterlyTocPlayer(true);
            }

            if (StringUtils.isNotBlank(buyin)) {
                if (game.isDoubleBuyIn()) {
                    gamePlayer.setBuyIn(60);
                } else {
                    gamePlayer.setBuyIn(30);
                }
            }

            gamePlayerService.create(gamePlayer);
        }

        game = gameService.findById(gameId);
        return new ModelAndView("mobilegame", "game", game);
    }

    @RequestMapping(value = "/mobile/gameplayer/update/{id}", method = RequestMethod.POST)
    public ModelAndView updateMobileGamePlayers(
            final HttpServletRequest request, @PathVariable("id") Integer id)
            throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        GamePlayer gamePlayer = gamePlayerService.findById(id);

        String playerId = request.getParameter("playerId");
        String toc = request.getParameter("toc");
        String qtoc = request.getParameter("qtoc");
        String buyin = request.getParameter("buyin");
        String rebuy = request.getParameter("rebuy");
        String chop = request.getParameter("chop");
        String place = request.getParameter("place");
        String optIn = request.getParameter("optIn");
        
        gamePlayer.setPlayerId(Integer.parseInt(playerId));

        if (StringUtils.isNotBlank(toc)) {
            gamePlayer.setAnnualTocPlayer(true);
        } else {
            gamePlayer.setAnnualTocPlayer(false);
        }

        if (StringUtils.isNotBlank(qtoc)) {
            gamePlayer.setQuarterlyTocPlayer(true);
        } else {
            gamePlayer.setQuarterlyTocPlayer(false);
        }

        if (StringUtils.isNotBlank(optIn)) {
            gamePlayer.setOptIn(true);
        } else {
            gamePlayer.setOptIn(false);
        }

        Game game = gameService.findById(gamePlayer.getGameId());
        if (StringUtils.isNotBlank(buyin)) {
            if (game.isDoubleBuyIn()) {
                gamePlayer.setBuyIn(60);
            } else {
                gamePlayer.setBuyIn(30);
            }
        } else {
            gamePlayer.setBuyIn(null);
        }

        if (StringUtils.isNotBlank(rebuy)) {
            if (game.isDoubleBuyIn()) {
                gamePlayer.setReBuyIn(40);
            } else {
                gamePlayer.setReBuyIn(20);
            }
        } else {
            gamePlayer.setReBuyIn(null);
        }

        if (StringUtils.isNotBlank(chop)) {
            gamePlayer.setChop(Integer.parseInt(chop));
        } else {
            gamePlayer.setChop(null);
        }

        if (StringUtils.isNotBlank(place) && !StringUtils.equals(place, "0")) {
            gamePlayer.setFinish(Integer.parseInt(place));
        } else {
            gamePlayer.setFinish(null);
        }

        gamePlayerService.update(gamePlayer);
        game = gameService.findById(gamePlayer.getGameId());

        return new ModelAndView("mobilegame", "game", game);
    }

    @RequestMapping(value = "/mobile/gameplayer/addfng/{id}", method = RequestMethod.POST)
    public ModelAndView addMobileFnNewGuy(
            final HttpServletRequest request, @PathVariable("id") Integer gameId)
            throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Game game = gameService.findById(gameId);

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String toc = request.getParameter("toc");
        String qtoc = request.getParameter("qtoc");
        String buyin = request.getParameter("buyin");
        
        if (StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName)) {
            throw new IllegalArgumentException("Either first name or last name must set");
        }
        
        Player player = new Player();
        if (StringUtils.isNotBlank(firstName)) {
            player.setFirstName(firstName);
        }
        if (StringUtils.isNotBlank(lastName)) {
            player.setLastName(lastName);
        }
        if (StringUtils.isNotBlank(email)) {
            player.setEmail(email);
        }
        int playerId = playerService.create(player);
        
        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setPlayerId(playerId);
        gamePlayer.setGameId(gameId);
        
        if (StringUtils.isNotBlank(toc)) {
            gamePlayer.setAnnualTocPlayer(true);
        } else {
            gamePlayer.setAnnualTocPlayer(false);
        }

        if (StringUtils.isNotBlank(qtoc)) {
            gamePlayer.setQuarterlyTocPlayer(true);
        } else {
            gamePlayer.setQuarterlyTocPlayer(false);
        }

        if (StringUtils.isNotBlank(buyin)) {
            if (game.isDoubleBuyIn()) {
                gamePlayer.setBuyIn(60);
            } else {
                gamePlayer.setBuyIn(30);
            }
        } else {
            gamePlayer.setBuyIn(null);
        }

        gamePlayerService.create(gamePlayer);
        game = gameService.findById(gameId);

        return new ModelAndView("mobilegame", "game", game);
    }

    @RequestMapping(value = "/mobile/gameplayer/edit/{id}", method = RequestMethod.GET)
    public ModelAndView editMobileGamePlayers(final HttpServletRequest request,
            @PathVariable("id") String id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        GamePlayer gPlayer = gamePlayerService.findById(Integer.parseInt(id));
        ModelAndView mav = new ModelAndView("mobilegameplayer", "gamePlayer",
                gPlayer);

        Game game = gameService.findById(gPlayer.getGameId());
        
        int numPlaces = Math.min(10, game.getPlayers().size());
        List<Integer> places = new ArrayList<Integer>(numPlaces);
        for (int i = 1; i <= numPlaces; ++i) {
            places.add(i);
        }
        
        List<Player> players = playerService.findAll();
        for (GamePlayer gamePlayer : game.getPlayers()) {
            
            if (gPlayer.getId() != gamePlayer.getId()) {
                if (gamePlayer.getFinish() != null) {
                    places.remove(gamePlayer.getFinish());
                }
            }
            
            int i = 0;
            for (Player player : players) {
                if (gamePlayer.getId() == gPlayer.getId()) {
                    continue;
                }
                if (gamePlayer.getPlayerId() == player.getId()) {
                    players.remove(i);
                    break;
                }
                ++i;
            }
        }

        mav.addObject("players", players);
        mav.addObject("places", places);

        if (game.isDoubleBuyIn()) {
            mav.addObject("buyIn", new Integer(60));
            mav.addObject("reBuy", new Integer(40));
        } else {
            mav.addObject("buyIn", new Integer(30));
            mav.addObject("reBuy", new Integer(20));
        }
        return mav;
    }

    @RequestMapping(value = "/mobile/gameplayer/delete/{id}", method = RequestMethod.GET)
    public ModelAndView deleteMobileGamePlayers(final HttpServletRequest request,
            @PathVariable("id") Integer id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        GamePlayer gamePlayer = gamePlayerService.findById(id);
        gamePlayerService.delete(id);
        Game game = gameService.findById(gamePlayer.getGameId());
        return new ModelAndView("mobilegame", "game", game);
    }
}
