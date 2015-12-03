package com.texastoc.controller;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.Game;
import com.texastoc.domain.GamePayout;
import com.texastoc.domain.Player;
import com.texastoc.domain.PlayerCount;
import com.texastoc.domain.QuarterlySeason;
import com.texastoc.domain.Season;
import com.texastoc.domain.SeasonChampion;
import com.texastoc.domain.SeasonPayout;
import com.texastoc.domain.SeasonTopTen;
import com.texastoc.exception.InvalidDateException;
import com.texastoc.service.GameService;
import com.texastoc.service.PlayerService;
import com.texastoc.service.QuarterlySeasonService;
import com.texastoc.service.SeasonService;
import com.texastoc.service.mail.MailService;

@Controller
public class SeasonController extends BaseController {
    
    private static final String PLACE = "place";
    private static final String AMOUNT = "amount";
    private static final String DESC = "desc";
    private static final String DELETE = "delete";

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private GameService gameService;
    @Autowired
    private MailService mailService;
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private QuarterlySeasonService qSeasonService;

    // /////////////////////////
    //
    // Admin website
    //
    // /////////////////////////
    @RequestMapping(value = "/admin/seasons", method = RequestMethod.GET)
    public ModelAndView getSeasons(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        List<Season> seasons = seasonService.findAll();
        return new ModelAndView("seasons", "seasons", seasons);
    }

    @RequestMapping(value = "/mobile/seasons", method = RequestMethod.GET)
    public ModelAndView getMobileSeasons(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        List<Season> seasons = seasonService.findAll();
        List<SeasonChampion> champions = seasonService.findAllChampions();
        ModelAndView mav = new ModelAndView("mobileseasons", "seasons", seasons);
        mav.addObject("champions", champions);
        return mav;
    }

    @RequestMapping(value = "/admin/season/{id}", method = RequestMethod.GET)
    public ModelAndView getSeason(final HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestParam(value="editing", required=false) Boolean editing) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Season season = seasonService.findById(Integer.valueOf(id));
        ModelAndView mav = new ModelAndView("season", "season", season);
        if (editing != null && editing) {
            mav.addObject("editing", new Boolean(true));
        } else {
            mav.addObject("editing", new Boolean(false));
        }
        return mav;
    }

    @RequestMapping(value = "/admin/season/{id}/payouts", method = RequestMethod.GET)
    public ModelAndView getSeasonPayouts(final HttpServletRequest request,
            @PathVariable("id") String id) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Season season = seasonService.findById(Integer.valueOf(id));
        ModelAndView mav = new ModelAndView("seasonpayouts", "season", season);
        return mav;
    }

    @RequestMapping(value = "/admin/season/payouts", method = RequestMethod.POST)
    public ModelAndView updateSeasonPayouts(final HttpServletRequest request,
            @RequestParam(value="seasonId", required=true) Integer seasonId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Season season = seasonService.findById(Integer.valueOf(seasonId));
        
        Map<String,SeasonPayout> map = new HashMap<String,SeasonPayout>();
        Set<String> deleteSet = new HashSet<String>();
        
        Enumeration enm = request.getParameterNames();
        while(enm.hasMoreElements()) {
            String name = (String)enm.nextElement();

            if (StringUtils.equals(name, PLACE)) {
                SeasonPayout seasonPayout = getFromMap("0",map);
                seasonPayout.setPlace(request.getParameter(name));
            } else if (StringUtils.startsWith(name, PLACE)) {
                String id = StringUtils.substringAfter(name, "_");
                SeasonPayout seasonPayout = getFromMap(id,map);
                seasonPayout.setPlace(request.getParameter(name));
            }

            if (StringUtils.equals(name, AMOUNT)) {
                SeasonPayout seasonPayout = getFromMap("0",map);
                String param = request.getParameter(name);
                try {
                    int amount = Integer.valueOf(param);
                    seasonPayout.setAmount(amount);
                } catch(Exception e) {
                    // Do nothing
                }
            } else if (StringUtils.startsWith(name, AMOUNT)) {
                String id = StringUtils.substringAfter(name, "_");
                SeasonPayout seasonPayout = getFromMap(id,map);
                String param = request.getParameter(name);
                try {
                    int amount = Integer.valueOf(param);
                    seasonPayout.setAmount(amount);
                } catch(Exception e) {
                    // Do nothing
                }
            }
            
            if (StringUtils.equals(name, DESC)) {
                SeasonPayout seasonPayout = getFromMap("0",map);
                seasonPayout.setDescription(request.getParameter(name));
            } else if (StringUtils.startsWith(name, DESC)) {
                String id = StringUtils.substringAfter(name, "_");
                SeasonPayout seasonPayout = getFromMap(id,map);
                seasonPayout.setDescription(request.getParameter(name));
            }

            if (StringUtils.startsWith(name, DELETE)) {
                String id = StringUtils.substringAfter(name, "_");
                deleteSet.add(id);
            }
        }
        
        // Remove the payouts that are to be deleted from the list 
        // of payouts to create or update.
        for (String key : deleteSet) {
            if (map.containsKey(key)) {
                map.remove(key);
            }
        }

        // Set the id and the season id for the payouts
        int asteriskCount = 0;
        for(Map.Entry<String, SeasonPayout> entry : map.entrySet()) {
            String id = entry.getKey();
            SeasonPayout payout = entry.getValue();
            payout.setSeasonId(seasonId);
            if (! StringUtils.equals("0", id)) {
                payout.setId(Integer.valueOf(id));
            }

            // trim strings
            payout.setPlace(payout.getPlace().trim());
            if (payout.getDescription() != null) {
                payout.setDescription(payout.getDescription().trim());
            }
            
            if ("*".equals(payout.getPlace())) {
                ++asteriskCount;
            }
        }
        
        ModelAndView mav = new ModelAndView("seasonpayouts");
        if (asteriskCount > 1) {
            mav.addObject("error", "Can only have one place with an asterisk");
        } else {
            // Change the ids from strings into integers
            Set<Integer> deleteIds = new HashSet<Integer>();
            for (String key : deleteSet) {
                deleteIds.add(new Integer(key));
            }

            if (map.size() > 0) {
                seasonService.updatePayouts(seasonId, 
                        new ArrayList<SeasonPayout>(map.values()),
                        deleteIds);
            }
        }
        
        season = seasonService.findById(Integer.valueOf(seasonId));
        mav.addObject("season", season);
        return mav;
    }

    @RequestMapping(value = "/admin/season/new", method = RequestMethod.GET)
    public ModelAndView newSeason(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("season", "season", new Season());
    }

    @RequestMapping(value = "/admin/season/add", method = RequestMethod.POST)
    public ModelAndView createSeason(final HttpServletRequest request,
            Season season, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        season.validate(season, errors);
        if (!errors.hasErrors()) {
            try {
                // Create a new season
                int id = seasonService.create(season);
                
                // Get the new season
                season = seasonService.findById(id);
            } catch(InvalidDateException e) {
                if (season.getStartDate() == e.getDate()) {
                    errors.rejectValue("startDate", "bogusCode", e.getMessage());
                } else if (season.getEndDate() == e.getDate()) {
                    errors.rejectValue("endDate", "bogusCode", e.getMessage());
                }
            }
        }
        return new ModelAndView("season", "season", season);
    }

    @RequestMapping(value = "/admin/season/update", method = RequestMethod.POST)
    public ModelAndView updateSeason(final HttpServletRequest request,
            Season season, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        ModelAndView mav = new ModelAndView();
        mav.setViewName("season");
        season.validate(season, errors);
        if (!errors.hasErrors()) {
            try {
                // Update the season
                seasonService.update(season);
                
                // Get the updated season
                season = seasonService.findById(season.getId());
            } catch(InvalidDateException e) {
                if (season.getStartDate() == e.getDate()) {
                    errors.rejectValue("startDate", "bogusCode", e.getMessage());
                } else {
                    errors.rejectValue("endDate", "bogusCode", e.getMessage());
                }
                mav.addObject("editing", new Boolean(true));
            }
        } else {
            mav.addObject("editing", new Boolean(true));
        }
        mav.addObject("season", season);
        return mav;
    }

    @RequestMapping(value = "/admin/season/summary/{id}", method = RequestMethod.GET)
    public ModelAndView showSummary(final HttpServletRequest request,
            @PathVariable("id") String id) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Season season = seasonService.findById(Integer.valueOf(id));

        Game game = null;
        List<Game> games = new ArrayList<Game>();
        if (season.getGames() != null && season.getGames().size() > 0) {
            List<Game> existingGames = season.getGames();
            
            // Get the last finalized game
            for (Game existingGame : existingGames) {
                if (!existingGame.isFinalized()) {
                    continue;
                }
                game = existingGame;
                games.add(existingGame);
            }
        }
        
        QuarterlySeason qSeason = null;
        if (season.getQuarterlies() != null && season.getQuarterlies().size() > 0) {
            qSeason = season.getQuarterlies().get(season.getQuarterlies().size() - 1);
        }
        
        ModelAndView mav = new ModelAndView("summary");
        mav.addObject("season", season);
        mav.addObject("quarterly", qSeason);
        mav.addObject("games", games);
        mav.addObject("game", game);
        return mav;
    }

    @RequestMapping(value = "/admin/season/printable", method = RequestMethod.GET)
    public ModelAndView showPrintable(final HttpServletRequest request,
            @RequestParam(value="seasonId", required=true) Integer seasonId,
            @RequestParam(value="gameId", required=true) Integer gameId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Game game = gameService.findById(gameId);
        Season season = seasonService.findById(game.getSeasonId());

        QuarterlySeason qSeason = null;
        if (season.getQuarterlies() != null && season.getQuarterlies().size() > 0) {
            qSeason = season.getQuarterlies().get(season.getQuarterlies().size() - 1);
        }
        
        ModelAndView mav = new ModelAndView("printable");
        mav.addObject("season", season);
        mav.addObject("quarterly", qSeason);
        mav.addObject("game", game);
        return mav;
    }

    @RequestMapping(value = "/mobile/season/email", method = RequestMethod.GET)
    public ModelAndView showEmailSeason(final HttpServletRequest request,
            @RequestParam(value="gameId", required=true) Integer gameId) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        List<Player> playersWithEmails = new ArrayList<Player>();
        List<Player> players = playerService.findAll();
        
        for (Player player : players) {
            if (StringUtils.isNotBlank(player.getEmail())) {
                playersWithEmails.add(player);
            }
        }

        ModelAndView mav = new ModelAndView("mobileemailsummary");
        mav.addObject("players", playersWithEmails);
        mav.addObject("gameId", new Integer(gameId));
        return mav;
    }

    @RequestMapping(value = "/mobile/season/email", method = RequestMethod.POST)
    public ModelAndView emailSeason(final HttpServletRequest request,
            @RequestParam(value="gameId", required=true) Integer gameId,
            @RequestParam(value="playerId", required=true) Integer playerId) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        seasonService.emailSeasonSummary(gameId, playerId);
        
        return showMobileSummaryUpToGame(request, gameId);
    }

    @RequestMapping(value = "/mobile/season/summary/{id}", method = RequestMethod.GET)
    public ModelAndView showMobileSummary(final HttpServletRequest request,
            @PathVariable("id") String id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Season season = seasonService.findById(Integer.valueOf(id));
        
        if (season.isUseHistoricalData()) {
            ModelAndView mav = new ModelAndView("mobilehistoricalsummary");
            mav.addObject("season", season);
            return mav;
        }

        Game game = null;
        if (season.getGames() != null && season.getGames().size() > 0) {
            List<Game> existingGames = season.getGames();
            // Get the last finalized game
            for (Game existingGame : existingGames) {
                if (!existingGame.isFinalized()) {
                    continue;
                }
                game = existingGame;
            }
        }

        if (game != null) {
            return showMobileSummaryUpToGame(request, game.getId());
        } else {
            ModelAndView mav = new ModelAndView("mobilesummary");
            mav.addObject("season", season);
            mav.addObject("quarterly", null);
            mav.addObject("games", null);
            mav.addObject("game", game);
            mav.addObject("players", null);
            mav.addObject("payouts", null);
            mav.addObject("hosts", null);
            return mav;
        }
    }

    @RequestMapping(value = "/admin/season/summary-by-game", method = RequestMethod.POST)
    public ModelAndView showSummaryUpToGame(final HttpServletRequest request,
            @RequestParam(value="gameId", required=true) Integer gameId) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Game game = gameService.findById(gameId);
        Season season = seasonService.findById(game.getSeasonId());

        List<Game> games = new ArrayList<Game>();
        if (season.getGames() != null && season.getGames().size() > 0) {
            List<Game> existingGames = season.getGames();
            for (Game existingGame : existingGames) {
                games.add(existingGame);
            }
        }

        Season shortenedSeason = seasonService.getUpToGame(gameId);
        QuarterlySeason qSeason = null;
        if (shortenedSeason.getQuarterlies() != null && shortenedSeason.getQuarterlies().size() > 0) {
            qSeason = shortenedSeason.getQuarterlies().get(shortenedSeason.getQuarterlies().size() - 1);
        }

        List<Player> players = playerService.findAll();

        ModelAndView mav = new ModelAndView("summary");
        mav.addObject("season", shortenedSeason);
        mav.addObject("quarterly", qSeason);
        mav.addObject("games", games);
        mav.addObject("game", game);
        mav.addObject("players", players);
        return mav;
    }

    @RequestMapping(value = "/mobile/season/summary-by-game", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView showMobileSummaryUpToGame(final HttpServletRequest request,
            @RequestParam(value="gameId", required=true) Integer gameId) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Game game = gameService.findById(gameId);
        Season season = seasonService.findById(game.getSeasonId());

        List<Game> games = new ArrayList<Game>();
        if (season.getGames() != null && season.getGames().size() > 0) {
            List<Game> existingGames = season.getGames();
            for (Game existingGame : existingGames) {
                if (existingGame.isFinalized()) {
                    games.add(existingGame);
                }
            }
        }

        Season shortenedSeason = seasonService.getUpToGame(gameId);
        QuarterlySeason qSeason = null;
        if (shortenedSeason.getQuarterlies() != null && shortenedSeason.getQuarterlies().size() > 0) {
            qSeason = shortenedSeason.getQuarterlies().get(shortenedSeason.getQuarterlies().size() - 1);
        }
        
        List<Player> players = playerService.findAll();
        List<GamePayout> payouts = gameService.findPayoutsByGameId(gameId);
        List<PlayerCount> hosts = seasonService.getHosts(game, games);
        List<PlayerCount> bankers = seasonService.getBankers(game, games);

        ModelAndView mav = new ModelAndView("mobilesummary");
        mav.addObject("season", shortenedSeason);
        mav.addObject("quarterly", qSeason);
        mav.addObject("games", games);
        mav.addObject("game", game);
        mav.addObject("players", players);
        mav.addObject("payouts", payouts);
        mav.addObject("hosts", hosts);
        mav.addObject("bankers", bankers);
        return mav;
    }

    @RequestMapping(value = "/mobile/season/topten-by-game", method = RequestMethod.GET)
    public ModelAndView showMobileTopTenUpToGame(final HttpServletRequest request,
            @RequestParam(value="gameId", required=true) Integer gameId) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Game game = gameService.findById(gameId);
        Season season = seasonService.findById(game.getSeasonId());

        SeasonTopTen topTen = seasonService.getTopTen(game, season);

        ModelAndView mav = new ModelAndView("mobileseasontopten");
        mav.addObject("season", season);
        mav.addObject("game", game);
        mav.addObject("topTen", topTen);
        return mav;
    }

    // /////////////////////////
    //
    // REST requests
    //
    // /////////////////////////
//    @RequestMapping("/api/seasons")
//    public @ResponseBody
//    List<Season> getSeasons(
//            @RequestParam(value = "version", defaultValue = "v1", required = false) String version) {
//        return seasonService.findAll();
//    }
    
    
//    @InitBinder
//    protected void initBinder(WebDataBinder binder) {
//        binder.setValidator(new Season());
//    }
    
    private SeasonPayout getFromMap(String key, Map<String,SeasonPayout> map) {
        SeasonPayout seasonPayout = map.get(key);
        if (seasonPayout == null) {
            seasonPayout = new SeasonPayout();
            map.put(key, seasonPayout);
        }
        return seasonPayout;
    }
}
