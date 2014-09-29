package com.texastoc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.common.CellPhoneCarrier;
import com.texastoc.domain.Player;
import com.texastoc.service.PlayerService;
import com.texastoc.service.mail.MailService;

@Controller
public class EmailTextController extends BaseController {
    
    @Autowired
    MailService mailService;
    @Autowired
    PlayerService playerService;

    @RequestMapping("/admin/emailtext")
    public ModelAndView getEmailText(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        List<Player> players = playerService.findAll();
        ModelAndView mav = new ModelAndView("emailtext", "players", players);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }

    @RequestMapping("/json/admin/email")
    public @ResponseBody 
    Map<String,String> sendEmail(final HttpServletRequest request,
            @RequestParam(value = "email", required = true) String email) {
        
        Map<String, String> result = new HashMap<String, String>();
        if (this.isNotLoggedIn(request)) {
            result.put("result", "Not logged in");
            return result;
        }
        result.put("result", mailService.sendEmail(email));
        return result;
    }

    @RequestMapping("/json/admin/text")
    public @ResponseBody 
    Map<String,String> sendText(final HttpServletRequest request,
            @RequestParam(value = "email", required = true) String email) {
        return sendEmail(request, email);
    }

}
