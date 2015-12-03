package com.texastoc.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FaqController extends BaseController {
    
    @RequestMapping("/mobile/faq")
    public ModelAndView getFaq(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaq");
    }

    @RequestMapping("/mobile/faq/chop")
    public ModelAndView getFaqChop(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaqchop");
    }

    @RequestMapping("/mobile/faq/stack")
    public ModelAndView getFaqStack(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaqstack");
    }

    @RequestMapping("/mobile/faq/tocchop")
    public ModelAndView getFaqTocChop(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaqtocchop");
    }

    @RequestMapping("/mobile/faq/prizepotdistributed")
    public ModelAndView getFaqPrizePotDistributed(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaqprizepotdist");
    }

    @RequestMapping("/mobile/faq/prizepotdetermined")
    public ModelAndView getFaqPrizePotDetermined(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaqprizepotdeterm");
    }

    @RequestMapping("/mobile/faq/bonuschip")
    public ModelAndView getFaqBonusChip(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaqbonuschip");
    }

    @RequestMapping("/mobile/faq/minrequirements")
    public ModelAndView getFaqMinRequirements(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaqminrequirements");
    }

    @RequestMapping("/mobile/faq/agerequirements")
    public ModelAndView getFaqAgeRequirements(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaqagerequirements");
    }

    @RequestMapping("/mobile/faq/voterequirements")
    public ModelAndView getFaqVoteRequirements(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("mobilefaqvoterequirements");
    }

}
