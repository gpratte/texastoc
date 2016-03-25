package com.texastoc.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.common.HomeGame;
import com.texastoc.domain.Game;
import com.texastoc.domain.Player;
import com.texastoc.service.GameService;
import com.texastoc.service.PlayerService;
import com.texastoc.service.mail.MailService;

@Controller
public class EmailController extends BaseController {

    static final Logger logger = Logger.getLogger(EmailController.class);
    
    private static final String DGS = "dgs";
    private static final String TOURNY = "tourny";
    private static final String DGS_AND_TOURNY = "dgstourny";
    private static final String HOSTS = "hosts";
    private static final String TRANSPORT = "transport";
    private static final String TOC_BOARD = "tocboard";

    @Autowired
    private GameService gameService;
    @Autowired
    private MailService mailService;
    @Autowired
    private PlayerService playerService;


    @RequestMapping(value = "/mobile/emails", method = RequestMethod.GET)
    public ModelAndView getEmailList (
            final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        ModelAndView mav = new ModelAndView("mobileemails");
        return mav;
    }

    @RequestMapping(value = "/mobile/email", method = RequestMethod.GET)
    public ModelAndView rally (
            final HttpServletRequest request,
            @RequestParam(value = "group", required = true) String group) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        ModelAndView mav = new ModelAndView("mobileemail", "group", group);
        return mav;
    }

    @RequestMapping(value = "/mobile/email", method = RequestMethod.POST)
    public ModelAndView sendToGroup (
            final HttpServletRequest request,
            @RequestParam(value = "subject", required = true) String subject,
            @RequestParam(value = "group", required = true) String group,
            @RequestParam(value = "textbody", required = true) String body) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        List<Player> recipients = null;
        if (StringUtils.equals(group, DGS)) {
            recipients = playerService.findPtcg();
        }
        if (StringUtils.equals(group, TOURNY)) {
            recipients = playerService.findActive();
        }
        if (StringUtils.equals(group, DGS_AND_TOURNY)) {
            recipients = playerService.findActive();
            List<Player> dgs = playerService.findPtcg();
            for (Player dg : dgs) {
                boolean found = false;
                for (Player active : recipients) {
                    if (active.getId() == dg.getId()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    recipients.add(dg);
                }
            }
        }
        if (StringUtils.equals(group, HOSTS)) {
            recipients = playerService.findPossibleHosts();
        }
        if (StringUtils.equals(group, TRANSPORT)) {
            recipients = playerService.findPossibleTransporters();
        }
        if (StringUtils.equals(group, TOC_BOARD)) {
            recipients = playerService.findTocBoard();
        }

//        StringBuilder sb = new StringBuilder();
//        for (Player player : recipients) {
//            sb.append(player.getFullName() + " ");
//        }
//        System.out.println(sb.toString());

        String loggedInUserEmail = getLoggedIn(request);
        Player fromPlayer = playerService.findByEmail(loggedInUserEmail);

        ModelAndView mav = null;
        if (recipients == null) {
            mav = new ModelAndView("mobileemails");
        } else {
            mailService.sendToGroup(fromPlayer, recipients, subject, body);
            mav = new ModelAndView("mobileemails", "sent", true);
        }
        
        return mav;
    }

}
