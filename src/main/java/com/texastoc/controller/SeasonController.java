package com.texastoc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.Game;
import com.texastoc.domain.QuarterlySeason;
import com.texastoc.domain.Season;
import com.texastoc.exception.InvalidDateException;
import com.texastoc.service.GameService;
import com.texastoc.service.QuarterlySeasonService;
import com.texastoc.service.SeasonService;

@Controller
public class SeasonController extends BaseController {

    @Autowired
    private HttpServletRequest context;

    @Autowired
    SeasonService seasonService;
    @Autowired
    QuarterlySeasonService qSeasonService;
    @Autowired
    GameService gameService;

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
        return new ModelAndView("mobileseasons", "seasons", seasons);
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

    @RequestMapping(value = "/mobile/season/summary/{id}", method = RequestMethod.GET)
    public ModelAndView showMobileSummary(final HttpServletRequest request,
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
        
        ModelAndView mav = new ModelAndView("mobilesummary");
        mav.addObject("season", season);
        mav.addObject("quarterly", qSeason);
        mav.addObject("games", games);
        mav.addObject("game", game);
        return mav;
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
        
        ModelAndView mav = new ModelAndView("summary");
        mav.addObject("season", shortenedSeason);
        mav.addObject("quarterly", qSeason);
        mav.addObject("games", games);
        mav.addObject("game", game);
        return mav;
    }

    @RequestMapping(value = "/mobile/season/summary-by-game", method = RequestMethod.POST)
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
                games.add(existingGame);
            }
        }

        Season shortenedSeason = seasonService.getUpToGame(gameId);
        QuarterlySeason qSeason = null;
        if (shortenedSeason.getQuarterlies() != null && shortenedSeason.getQuarterlies().size() > 0) {
            qSeason = shortenedSeason.getQuarterlies().get(shortenedSeason.getQuarterlies().size() - 1);
        }
        
        ModelAndView mav = new ModelAndView("mobilesummary");
        mav.addObject("season", shortenedSeason);
        mav.addObject("quarterly", qSeason);
        mav.addObject("games", games);
        mav.addObject("game", game);
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
}
