package com.texastoc.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.common.Finish;
import com.texastoc.common.HomeGame;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Player;
import com.texastoc.domain.clock.Clock;
import com.texastoc.exception.DuplicatePlayerException;
import com.texastoc.service.ClockService;
import com.texastoc.service.GamePlayerService;
import com.texastoc.service.GameService;
import com.texastoc.service.PlayerService;

@Controller
public class GamePlayerController extends BaseController {

    static final Logger logger = Logger.getLogger(GamePlayerController.class);

    @Autowired
    GameService gameService;
    @Autowired
    ClockService clockService;
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
        if (game.getHomeGame() == HomeGame.TOC) {
            if (game.isDoubleBuyIn()) {
                mav.addObject("buyIn", new Integer(60));
                mav.addObject("reBuy", new Integer(40));
            } else {
                mav.addObject("buyIn", new Integer(30));
                mav.addObject("reBuy", new Integer(20));
            }
        } else {
            mav.addObject("buyIn", new Integer(20));
            mav.addObject("reBuy", new Integer(0));
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
        if (game.getHomeGame() == HomeGame.TOC) {
            if (game.isDoubleBuyIn()) {
                mav.addObject("buyIn", new Integer(60));
                mav.addObject("reBuy", new Integer(40));
            } else {
                mav.addObject("buyIn", new Integer(30));
                mav.addObject("reBuy", new Integer(20));
            }
        } else {
            mav.addObject("buyIn", new Integer(20));
            mav.addObject("reBuy", new Integer(0));
        }
        mav.addObject("finish", Finish.class);
        return mav;
    }

    @RequestMapping(value = "/admin/gameplayer/upload/{id}", method = RequestMethod.GET)
    public ModelAndView showUploadGamePlayers(final HttpServletRequest request,
            @PathVariable("id") Integer gameId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        ModelAndView mav = new ModelAndView("gamePlayerUpload", "gameId",
                gameId);
        return mav;
    }

    @RequestMapping(value = "/admin/gameplayer/upload/{id}", method = RequestMethod.POST)
    public ModelAndView handleFileUpload(@PathVariable("id") Integer gameId,
            @RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {

            List<String> errors = new ArrayList<String>();
            List<Player> added = new ArrayList<Player>();
            List<Player> participating = new ArrayList<Player>();
            List<Player> removed = new ArrayList<Player>();

            try {
                InputStream inputStream = file.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream));

                // Read the header line
                String line = bufferedReader.readLine();

                List<Player> players = playerService.findAll();

                // Now read the people
                while ((line = bufferedReader.readLine()) != null) {
                    String[] tokens = StringUtils.split(line, ",");

                    Player playerMatch = matchPlayer(tokens);

                    if (playerMatch != null) {
                        Game game = gameService.findById(gameId);
                        boolean playerAlreadyInGame = false;
                        for (GamePlayer existingGamePlayer : game.getPlayers()) {
                            if (existingGamePlayer.getPlayer().getId() == playerMatch
                                    .getId()) {
                                playerAlreadyInGame = true;
                                break;
                            }
                        }
                        
                        if (playerAlreadyInGame
                                && StringUtils.equals(tokens[2], "Yes")) {
                            participating.add(playerMatch);
                            continue;
                        }


                        if (!playerAlreadyInGame
                                && StringUtils.equals(tokens[2], "Yes")) {
                            GamePlayer gamePlayer = new GamePlayer();
                            gamePlayer.setPlayerId(playerMatch.getId());
                            gamePlayer.setGameId(gameId);
                            try {
                                gamePlayerService.create(gamePlayer);
                                added.add(playerMatch);
                                participating.add(playerMatch);
                            } catch (Exception e) {
                                errors.add("Could not create game player "
                                        + playerMatch.getFullName());
                                logger.warn("Could not create game player "
                                        + playerMatch.getFullName(), e);
                            }
                        }

                        if (playerAlreadyInGame
                                && !StringUtils.equals(tokens[2], "Yes")) {
                            // Sometimes a player has the same name more than
                            // once in the list. Make sure the player has
                            // not already been added.
                            
                            boolean alreadyParticipating = false;
                            for (Player alreadyParticipatingPlayer : participating) {
                                if (alreadyParticipatingPlayer.getId() == playerMatch.getId()) {
                                    alreadyParticipating = true;
                                    break;
                                }
                            }
                            if (alreadyParticipating) {
                                continue;
                            }
                            
                            try {
                                GamePlayer gamePlayer = gamePlayerService
                                        .findByPlayerId(playerMatch.getId(), gameId);
                                gamePlayerService.delete(gamePlayer.getId());
                                removed.add(playerMatch);
                            } catch (Exception e) {
                                errors.add("Could not remove game player "
                                        + playerMatch.getFullName());
                                logger.warn("Could not remove game player "
                                        + playerMatch.getFullName(), e);
                            }
                        }
                    } else {
                        if (StringUtils.equals(tokens[2], "Yes")) {
                            errors.add("Could not find a match for evite person "
                                    + tokens[0] + ", " + tokens[1]);
                            logger.info("Could not find a match for evite person "
                                    + tokens[0] + ", " + tokens[1]);
                        }
                    }
                }
            } catch (IOException e) {
                logger.warn(e);
            }

            ModelAndView mav = new ModelAndView("gamePlayerUpload", "gameId",
                    gameId);
            mav.addObject("added", added);
            mav.addObject("removed", removed);
            mav.addObject("errors", errors);
            return mav;
        } else {
            ModelAndView mav = new ModelAndView("gamePlayerUpload", "gameId",
                    gameId);
            mav.addObject("error", "No file choosen");
            return mav;
        }
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
            if (game.getHomeGame() == HomeGame.TOC) {
                if (game.isDoubleBuyIn()) {
                    mav.addObject("buyIn", new Integer(60));
                    mav.addObject("reBuy", new Integer(40));
                } else {
                    mav.addObject("buyIn", new Integer(30));
                    mav.addObject("reBuy", new Integer(20));
                }
            } else {
                mav.addObject("buyIn", new Integer(20));
                mav.addObject("reBuy", new Integer(0));
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
                gamePlayerService.update(gamePlayer, null);
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
        if (game.getHomeGame() == HomeGame.TOC) {
            if (game.isDoubleBuyIn()) {
                mav.addObject("buyIn", new Integer(60));
                mav.addObject("reBuy", new Integer(40));
            } else {
                mav.addObject("buyIn", new Integer(30));
                mav.addObject("reBuy", new Integer(20));
            }
        } else {
            mav.addObject("buyIn", new Integer(20));
            mav.addObject("reBuy", new Integer(0));
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

        if (game.getHomeGame() == HomeGame.TOC) {
            if (game.isDoubleBuyIn()) {
                mav.addObject("buyIn", new Integer(60));
                mav.addObject("reBuy", new Integer(40));
            } else {
                mav.addObject("buyIn", new Integer(30));
                mav.addObject("reBuy", new Integer(20));
            }
        } else {
            mav.addObject("buyIn", new Integer(20));
            mav.addObject("reBuy", new Integer(0));
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
                if (game.getHomeGame() == HomeGame.TOC) {
                    if (game.isDoubleBuyIn()) {
                        gamePlayer.setBuyIn(60);
                    } else {
                        gamePlayer.setBuyIn(30);
                    }
                } else {
                    gamePlayer.setBuyIn(20);
                }
            }

            gamePlayerService.create(gamePlayer);
        }

        game = gameService.findById(gameId);
        ModelAndView mav = new ModelAndView("mobilegame", "game", game);
        mav.addObject("host", playerService.findById(game.getHostId()));
        Clock clock = clockService.getClock(gameId);
        clock.sync();
        mav.addObject("clock", clock);
        return mav;
    }

    @RequestMapping(value = "/mobile/gameplayer/update/{id}", method = RequestMethod.POST)
    public ModelAndView updateMobileGamePlayers(
            final HttpServletRequest request, @PathVariable("id") Integer id)
            throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        GamePlayer gamePlayer = gamePlayerService.findById(id);

        String toc = request.getParameter("toc");
        String qtoc = request.getParameter("qtoc");
        String buyin = request.getParameter("buyin");
        String rebuy = request.getParameter("rebuy");
        String chop = request.getParameter("chop");
        String place = request.getParameter("place");
        String optIn = request.getParameter("optIn");
        String emailOptIn = request.getParameter("emailOptIn");
        String knockedOut = request.getParameter("knockedOut");
        String banker = request.getParameter("banker");
        String hostId = request.getParameter("hostId");

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

        if (StringUtils.isNotBlank(emailOptIn)) {
            gamePlayer.setEmailOptIn(true);
        } else {
            gamePlayer.setEmailOptIn(false);
        }

        if (StringUtils.isNotBlank(knockedOut)) {
            gamePlayer.setKnockedOut(true);
        } else {
            gamePlayer.setKnockedOut(false);
        }

        Game game = gameService.findById(gamePlayer.getGameId());
        if (StringUtils.isNotBlank(buyin)) {
            if (game.getHomeGame() == HomeGame.TOC) {
                if (game.isDoubleBuyIn()) {
                    gamePlayer.setBuyIn(60);
                } else {
                    gamePlayer.setBuyIn(30);
                }
            } else {
                gamePlayer.setBuyIn(20);
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

        gamePlayerService.update(gamePlayer, getLoggedIn(request));

        if (StringUtils.isNotBlank(banker)) {
            playerService.updateGameBanker(gamePlayer.getGameId(), gamePlayer.getPlayerId(), null);
        } else {
            playerService.updateGameBanker(gamePlayer.getGameId(), null, gamePlayer.getPlayerId());
        }

        if (StringUtils.isNotBlank(hostId)) {
            game.setHostId(Integer.valueOf(hostId));
            gameService.update(game);
        }

        game = gameService.findById(gamePlayer.getGameId());

        ModelAndView mav = new ModelAndView("mobilegame", "game", game);
        mav.addObject("host", playerService.findById(game.getHostId()));
        Clock clock = clockService.getClock(game.getId());
        clock.sync();
        mav.addObject("clock", clock);
        return mav;
    }

    @RequestMapping(value = "/mobile/gameplayer/addfng/{id}", method = RequestMethod.POST)
    public ModelAndView addMobileFnNewGuy(final HttpServletRequest request,
            @PathVariable("id") Integer gameId) throws Exception {
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
            throw new IllegalArgumentException(
                    "Either first name or last name must set");
        }

        Player player = new Player();
        if (StringUtils.isNotBlank(firstName)) {
            player.setFirstName(firstName);
        }
        if (StringUtils.isNotBlank(lastName)) {
            player.setLastName(lastName);
        }
        if (StringUtils.isNotBlank(email)) {
            email = StringUtils.trim(email);
            email = StringUtils.lowerCase(email);
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
            if (game.getHomeGame() == HomeGame.TOC) {
                if (game.isDoubleBuyIn()) {
                    gamePlayer.setBuyIn(60);
                } else {
                    gamePlayer.setBuyIn(30);
                }
            } else {
                gamePlayer.setBuyIn(20);
            }
        } else {
            gamePlayer.setBuyIn(null);
        }

        gamePlayerService.create(gamePlayer);
        game = gameService.findById(gameId);

        ModelAndView mav = new ModelAndView("mobilegame", "game", game);
        mav.addObject("host", playerService.findById(game.getHostId()));
        Clock clock = clockService.getClock(game.getId());
        clock.sync();
        mav.addObject("clock", clock);
        return mav;
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
        mav.addObject("game", game);

        int numPlaces = Math.min(10, game.getPlayers().size());
        List<Integer> places = new ArrayList<Integer>(numPlaces);
        for (int i = 1; i <= numPlaces; ++i) {
            places.add(i);
        }

        for (GamePlayer gamePlayer : game.getPlayers()) {

            if (gPlayer.getId() != gamePlayer.getId()) {
                if (gamePlayer.getFinish() != null) {
                    places.remove(gamePlayer.getFinish());
                }
            }
        }

        mav.addObject("places", places);

        if (game.getHomeGame() == HomeGame.TOC) {
            if (game.isDoubleBuyIn()) {
                mav.addObject("buyIn", new Integer(60));
                mav.addObject("reBuy", new Integer(40));
            } else {
                mav.addObject("buyIn", new Integer(30));
                mav.addObject("reBuy", new Integer(20));
            }
        } else {
            mav.addObject("buyIn", new Integer(20));
            mav.addObject("reBuy", new Integer(0));
        }
        return mav;
    }

    @RequestMapping(value = "/mobile/gameplayer/delete/{id}", method = RequestMethod.GET)
    public ModelAndView deleteMobileGamePlayers(
            final HttpServletRequest request, @PathVariable("id") Integer id)
            throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        GamePlayer gamePlayer = gamePlayerService.findById(id);
        gamePlayerService.delete(id);
        Game game = gameService.findById(gamePlayer.getGameId());
        ModelAndView mav = new ModelAndView("mobilegame", "game", game);
        mav.addObject("host", playerService.findById(game.getHostId()));
        Clock clock = clockService.getClock(game.getId());
        clock.sync();
        mav.addObject("clock", clock);
        return mav;
    }

    // Token is as follows:
    // token[0] name
    // token[1] email
    private Player matchPlayer(String[] tokens) {

        Player playerMatch = null;
        List<Player> players = playerService.findAll();

        if (tokens != null && tokens.length > 0) {

            // Split the first token into first and last name
            String[] names = StringUtils.split(tokens[0], " ");

            if (names != null && names.length > 0) {
                boolean firstNameFound = false;
                boolean lastNameFound = false;
                for (Player player : players) {
                    for (String name : names) {
                        if (StringUtils.equals(name, player.getFirstName())) {
                            firstNameFound = true;
                        }
                        if (StringUtils.equals(name, player.getLastName())) {
                            lastNameFound = true;
                        }
                    }

                    if (firstNameFound && lastNameFound) {
                        playerMatch = player;
                        break;
                    }
                }
            }

            if (playerMatch == null && StringUtils.isNotBlank(tokens[1])) {
                // Check email
                for (Player player : players) {
                    if (StringUtils.equals(tokens[1], player.getEmail())) {
                        playerMatch = player;
                        break;
                    }
                }
            }
        }

        return playerMatch;
    }
}
