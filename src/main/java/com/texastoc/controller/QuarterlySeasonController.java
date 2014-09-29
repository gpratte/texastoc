package com.texastoc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.QuarterlySeason;
import com.texastoc.domain.Season;
import com.texastoc.exception.InvalidDateException;
import com.texastoc.exception.InvalidQuarterException;
import com.texastoc.service.QuarterlySeasonService;
import com.texastoc.service.SeasonService;

@Controller
public class QuarterlySeasonController extends BaseController {

    @Autowired
    private HttpServletRequest context;

    @Autowired
    SeasonService seasonService;
    @Autowired
    QuarterlySeasonService quarterlySeasonService;

    // /////////////////////////
    //
    // Admin website
    //
    // /////////////////////////
    @RequestMapping(value = "/admin/quarterly/{id}", method = RequestMethod.GET)
    public ModelAndView getQuarterlySeason(final HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestParam(value = "editing", required = false) Boolean editing) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        QuarterlySeason quarterly = quarterlySeasonService.findById(Integer
                .valueOf(id));
        ModelAndView mav = new ModelAndView("quarterly", "quarterlySeason", quarterly);
        if (editing != null && editing) {
            mav.addObject("editing", new Boolean(true));
        } else {
            mav.addObject("editing", new Boolean(false));
        }
        return mav;
    }

    @RequestMapping(value = "/admin/quarterly/new/{id}", method = RequestMethod.GET)
    public ModelAndView newQuarterlySeason(final HttpServletRequest request,
            @PathVariable("id") Integer seasonId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        QuarterlySeason quarterly = new QuarterlySeason();
        quarterly.setSeasonId(seasonId);
        return new ModelAndView("quarterly", "quarterlySeason", quarterly);
    }

    @RequestMapping(value = "/admin/quarterly/add", method = RequestMethod.POST)
    public ModelAndView createQuarterlySeason(final HttpServletRequest request,
            QuarterlySeason quarterly, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        quarterly.validate(quarterly, errors);
        if (!errors.hasErrors()) {
            try {
                // Create a new quarterly season
                int id = quarterlySeasonService.create(quarterly);

                // Return to the season
                Season season = seasonService.findById(quarterly.getSeasonId());
                return new ModelAndView("season", "season", season);
            } catch(InvalidDateException e) {
                if (quarterly.getStartDate() == e.getDate()) {
                    errors.rejectValue("startDate", "bogusCode", e.getMessage());
                } else {
                    errors.rejectValue("endDate", "bogusCode", e.getMessage());
                }
            } catch(InvalidQuarterException e) {
                errors.rejectValue("quarter", "bogusCode", e.getMessage());
            }
        }
        return new ModelAndView("quarterly", "quarterlySeason", quarterly);
    }

    @RequestMapping(value = "/admin/quarterly/update", method = RequestMethod.POST)
    public ModelAndView updateQuarterlySeason(final HttpServletRequest request,
            QuarterlySeason quarterly,
            Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        boolean noErrors = true;
        quarterly.validate(quarterly, errors);
        if (!errors.hasErrors()) {
            try {
                quarterlySeasonService.update(quarterly);
            } catch(InvalidDateException e) {
                if (quarterly.getStartDate() == e.getDate()) {
                    errors.rejectValue("startDate", "bogusCode", e.getMessage());
                } else {
                    errors.rejectValue("endDate", "bogusCode", e.getMessage());
                }
                noErrors = false;
            } catch(InvalidQuarterException e) {
                errors.rejectValue("quarter", "bogusCode", e.getMessage());
                noErrors = false;
            }
        } else {
            noErrors = false;
        }
        
        if (noErrors) {
            Season season = seasonService.findById(quarterly.getSeasonId());
            return new ModelAndView("season", "season", season);
        } else {
            ModelAndView mav = new ModelAndView();
            mav.setViewName("quarterly");
            mav.addObject("editing", new Boolean(true));
            mav.addObject("quarterlySeason", quarterly);
            return mav;
        }
    }

//    @InitBinder
//    protected void initBinder(WebDataBinder binder) {
//        binder.setValidator(new QuarterlySeason());
//    }
}
