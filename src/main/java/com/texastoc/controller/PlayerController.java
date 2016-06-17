package com.texastoc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.common.CellPhoneCarrier;
import com.texastoc.domain.Game;
import com.texastoc.domain.Player;
import com.texastoc.domain.Season;
import com.texastoc.service.GameService;
import com.texastoc.service.PlayerService;
import com.texastoc.service.SeasonService;
import com.texastoc.service.mail.MailService;

@Controller
public class PlayerController extends BaseController {

    @Autowired
    MailService mailService;
    @Autowired
    GameService gameService;
    @Autowired
    SeasonService seasonService;
    @Autowired
    PlayerService playerService;

    @RequestMapping(value = "/admin/player/{id}", method = RequestMethod.GET)
    public ModelAndView getPlayer(final HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestParam(value="editing", required=false) Boolean editing) {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Player player = playerService.findById(Integer.valueOf(id));
        ModelAndView mav = new ModelAndView("player", "player", player);
        if (editing != null && editing) {
            mav.addObject("editing", new Boolean(true));
        } else {
            mav.addObject("editing", new Boolean(false));
        }
        mav.addObject("cellCarriers", CellPhoneCarrier.values());

        return mav;
    }

    @RequestMapping(value = "/mobile/player/{id}", method = RequestMethod.GET)
    public ModelAndView getMobilePlayer(final HttpServletRequest request,
            @PathVariable("id") String id) {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Player player = playerService.findById(Integer.valueOf(id));
        obfiscatePlayerEmail(player);
        obfiscatePlayerPhone(player);
        ModelAndView mav = new ModelAndView("mobileplayer", "player", player);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());

        return mav;
    }

    @RequestMapping(value = "/mobile/player/changePassword", method = RequestMethod.GET)
    public ModelAndView showChangePassword(final HttpServletRequest request) {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        return new ModelAndView("mobileChangePassword");
    }

    @RequestMapping(value = "/mobile/player/updatePassword", method = RequestMethod.POST)
    public ModelAndView updatePassword(final HttpServletRequest request,
            @RequestParam(value="current", required=true) String currentPassword,
            @RequestParam(value="new", required=true) String newPassword,
            @RequestParam(value="confirm", required=true) String confirmNewPassword) {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        ArrayList<String> problems = new ArrayList<String>();
        if (StringUtils.isBlank(currentPassword)) {
            problems.add("Current password is required");
        }
        if (StringUtils.isBlank(newPassword)) {
            problems.add("New password is required");
        }
        if (StringUtils.isBlank(confirmNewPassword)) {
            problems.add("Confirm New password is required");
        }
        
        currentPassword = StringUtils.trim(currentPassword);
        newPassword = StringUtils.trim(newPassword);
        confirmNewPassword = StringUtils.trim(confirmNewPassword);

        String loggedInUserEmail = getLoggedIn(request);
        if (problems.size() == 0) {
            if (!playerService.isPasswordValid(loggedInUserEmail, currentPassword)) {
                problems.add("Current password is not correct");
            }
        }

        if (problems.size() == 0) {
            if (!StringUtils.equals(newPassword, confirmNewPassword)) {
                problems.add("New password and confirm password are not the same");
            }
        }
        
        if (problems.size() == 0) {
            Player player = playerService.findByEmail(loggedInUserEmail);
            playerService.updatePassword(player.getId(), newPassword);
            
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
            mav.addObject("passwordChanged",new Boolean(true));
            mav.addObject("readOnly", new Boolean(isReadOnly(request)));
            return mav;
        } else {
            ModelAndView mav = new ModelAndView("mobileChangePassword","problems",problems);
            mav.addObject("currentPassword",currentPassword);
            mav.addObject("newPassword",newPassword);
            mav.addObject("confirmNewPassword",confirmNewPassword);
            return mav;
        }
    }

    @RequestMapping(value = "/mobile/player/forgotPassword", method = RequestMethod.GET)
    public ModelAndView showForgotPassword(final HttpServletRequest request) {

        return new ModelAndView("mobileForgotPassword");
    }

    @RequestMapping(value = "/mobile/player/generatePassword", method = RequestMethod.POST)
    public ModelAndView generatePassword(final HttpServletRequest request,
            @RequestParam(value="email", required=true) String email) {

        ArrayList<String> problems = new ArrayList<String>();
        if (StringUtils.isBlank(email)) {
            problems.add("email is required");
        }

        Player player = null;
        if (problems.size() == 0) {
            player = playerService.findByEmail(email);
            if (player == null) {
                problems.add("No player found with that email");
            }
        }

        if (problems.size() == 0) {
            String password = RandomStringUtils.random(6, true, false);
            playerService.updatePassword(player.getId(), password);
            mailService.sendNewPassword(email, password);
            return new ModelAndView("mobilelogin");
        } else {
            ModelAndView mav = new ModelAndView("mobileForgotPassword","problems",problems);
            mav.addObject("email",email);
            return mav;
        }
    }

    @RequestMapping(value = "/mobile/player/{id}/text", method = RequestMethod.GET)
    public ModelAndView textMobilePlayer(final HttpServletRequest request,
            @PathVariable("id") String id) {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Player player = playerService.findById(Integer.valueOf(id));

        String result = "";
        if (StringUtils.isBlank(player.getPhone()) ||
                StringUtils.isBlank(player.getCellCarrier())) {
            result = "Phone and cell carrier must be set";
        } else {
            String textEmail = player.getPhone() + player.getCellCarrier();
            result = mailService.sendEmail(textEmail);
        }
        
        obfiscatePlayerEmail(player);
        obfiscatePlayerPhone(player);
        ModelAndView mav = new ModelAndView("mobileplayer", "player", player);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        mav.addObject("result", result);

        return mav;
    }

    @RequestMapping(value = "/mobile/player/{id}/email", method = RequestMethod.GET)
    public ModelAndView emailMobilePlayer(final HttpServletRequest request,
            @PathVariable("id") String id) {

        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Player player = playerService.findById(Integer.valueOf(id));

        String result = "";
        if (StringUtils.isBlank(player.getEmail())) {
            result = "Email must be set";
        } else {
            result = mailService.sendEmail(player.getEmail());
        }
        
        obfiscatePlayerEmail(player);
        obfiscatePlayerPhone(player);
        ModelAndView mav = new ModelAndView("mobileplayer", "player", player);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        mav.addObject("result", result);

        return mav;
    }

    @RequestMapping(value = "/json/admin/player/{id}", method = RequestMethod.GET)
    public @ResponseBody 
    Player getPlayerJson(final HttpServletRequest request,
            @PathVariable("id") String id) {

        if (this.isNotLoggedIn(request)) {
            return null;
        }

        return playerService.findById(Integer.valueOf(id));
    }

    @RequestMapping(value = "/admin/players", method = RequestMethod.GET)
    public ModelAndView getPlayers(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        List<Player> players = playerService.findAll();
        ModelAndView mav = new ModelAndView("players", "players", players);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }

    @RequestMapping(value = "/mobile/players", method = RequestMethod.GET)
    public ModelAndView getMobilePlayers(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        List<Player> players = playerService.findAll();
        for (Player player : players) {
            obfiscatePlayerEmail(player);
            obfiscatePlayerPhone(player);
        }
        ModelAndView mav = new ModelAndView("mobileplayers", "players", players);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }

    @RequestMapping(value = "/admin/player/new", method = RequestMethod.GET)
    public ModelAndView newPlayer(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        Player player = new Player();
        ModelAndView mav = new ModelAndView("player", "player", player);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }

   @RequestMapping(value = "/admin/player/add", method = RequestMethod.POST)
    public ModelAndView createPlayer(final HttpServletRequest request,
            @Valid Player player, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        //;;!! shouldn't have to do this. The validate(..) method 
        // in Player should be invoked to do this
        player.validate(player, errors);
        
        if (StringUtils.equals(player.getCellCarrier(), "#")) {
            player.setCellCarrier(null);
        }

        ModelAndView mav = null;
        if (!errors.hasErrors()) {
            int id = playerService.create(player);
            List<Player> players = playerService.findAll();
            mav = new ModelAndView("players", "players", players);
        } else {
            mav = new ModelAndView("player", "player", player);
            mav.addObject("editing", new Boolean(true));
        }
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }
    
    @RequestMapping(value = "/admin/player/update", method = RequestMethod.POST)
    public ModelAndView updatePlayer(final HttpServletRequest request,
            @Valid Player player, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        ModelAndView mav = new ModelAndView();
        mav.setViewName("player");
        
        player.validate(player, errors);
        
        if (StringUtils.equals(player.getCellCarrier(), "#")) {
            player.setCellCarrier(null);
        }

        if (!errors.hasErrors()) {
            // Update 
            playerService.update(player);
            // Get it
            player = playerService.findById(player.getId());
        } else {
            mav.addObject("editing", new Boolean(true));
        }
        mav.addObject("player", player);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }

    @RequestMapping(value = "/mobile/player/update", method = RequestMethod.POST)
    public ModelAndView updateMobilePlayer(final HttpServletRequest request,
            @Valid Player player, Errors errors) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        
        String updated = null;
        if (!errors.hasErrors()) {
            Player existingPlayer = playerService.findById(player.getId());
            
            if (StringUtils.isNotBlank(player.getEmail()) &&
                    player.getEmail().indexOf('*') == -1) {
                String newEmail = player.getEmail();
                newEmail = StringUtils.trim(newEmail);
                newEmail = StringUtils.lowerCase(newEmail);
                existingPlayer.setEmail(newEmail);
                updated = "email";
            }
            if (StringUtils.isNotBlank(player.getPhone()) &&
                    player.getPhone().indexOf('*') == -1) {
                existingPlayer.setPhone(player.getPhone());
                if (updated == null) {
                    updated = "phone";
                } else {
                    updated += ", phone";
                }
            }
            
            if (updated == null) {
                updated = " cell carrier";
            } else {
                updated += ", cell carrier";
            }
            updated += ", possible transporter, active, ptcg and possible host";
            existingPlayer.setCellCarrier(player.getCellCarrier());
            existingPlayer.setPossibleHost(player.isPossibleHost());
            existingPlayer.setTransporter(player.isTransporter());
            existingPlayer.setPtcg(player.isPtcg());
            existingPlayer.setTocBoard(player.isTocBoard());
            existingPlayer.setActive(player.isActive());
            
            if (StringUtils.equals(player.getCellCarrier(), "#")) {
                existingPlayer.setCellCarrier(null);
            }

            // Update 
            playerService.update(existingPlayer);
            player = playerService.findById(Integer.valueOf(player.getId()));
            obfiscatePlayerEmail(player);
            obfiscatePlayerPhone(player);
        }
        
        ModelAndView mav = new ModelAndView("mobileplayer", "player", player);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        if (updated != null) {
            mav.addObject("updated", updated);
        }
        return mav;
    }

    @RequestMapping(value = "/admin/player/delete/{id}", method = RequestMethod.GET)
    public ModelAndView deleteGamePlayer(final HttpServletRequest request,
            @PathVariable("id") Integer id) throws Exception {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        playerService.delete(id);
        List<Player> players = playerService.findAll();
        ModelAndView mav = new ModelAndView("players", "players", players);
        mav.addObject("cellCarriers", CellPhoneCarrier.values());
        return mav;
    }
    
    @RequestMapping(value = "/admin/gamebanker/{id}", method = RequestMethod.GET)
    public ModelAndView getGame(final HttpServletRequest request,
            @PathVariable("id") Integer id) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        List<Player> bankers = playerService.findBankersByGameId(id);
        ModelAndView mav = new ModelAndView("bankers", "bankers",
                bankers);
        mav.addObject("gameId", id);
        List<Player> players = playerService.findAll();
        mav.addObject("players", players);

        return mav;
    }

    @RequestMapping(value = "/admin/gamebanker/add/{id}", method = RequestMethod.POST)
    public ModelAndView createGameBanker(final HttpServletRequest request,
            @PathVariable("id") Integer gameId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        try {
            int playerId = Integer.parseInt(request.getParameter("newbanker"));
            playerService.addGameBanker(gameId, playerId);
        } catch(Exception e) {
            // do nothing
        }
        
        return this.getGame(request, gameId);
        
    }
    
    @RequestMapping(value = "/admin/gamebanker/delete/{id}", method = RequestMethod.GET)
    public ModelAndView deleteGameBanker(final HttpServletRequest request,
            @PathVariable("id") Integer gameId,
            @RequestParam(value="playerId", required=true) Integer playerId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        try {
            playerService.removeGameBanker(gameId, playerId);
        } catch(Exception e) {
            // do nothing
        }
        
        return this.getGame(request, gameId);
        
    }
    
    
    private void obfiscatePlayerEmail(Player player) {
        if (player.getEmail() != null && StringUtils.isNotBlank(player.getEmail())) {
            String email = player.getEmail();
            StringBuilder sb = new StringBuilder();
            
            int atIndex = email.indexOf('@');
            boolean shortName = false;
            if (atIndex != -1 && atIndex < 6) {
                shortName = true;
            }
            boolean dotted = false;
            for (int i = 0; i < email.length(); ++i) {
                if (email.charAt(i) == '@') {
                    sb.append(email.subSequence(i, email.length()));
                    break;
                } else if (!shortName && i+2 < email.length() && email.charAt(i+2) == '@') {
                    sb.append(email.charAt(i));
                } else if (!shortName && i+1 != email.length() && email.charAt(i+1) == '@') {
                    sb.append(email.charAt(i));
                } else if (!shortName && i == 1) {
                    sb.append(email.charAt(i));
                } else if (i == 0) {
                    sb.append(email.charAt(i));
                } else {
                    if (!dotted) {
                        sb.append('*');
                    }
                    dotted = true;
                }
            }
            player.setEmail(sb.toString());
        }
    }
    private void obfiscatePlayerPhone(Player player) {
        if (player.getPhone() != null && StringUtils.isNotBlank(player.getPhone())) {
            String phone = player.getPhone();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < phone.length(); ++i) {
                boolean processedChar = false;
                if (phone.length() > 7) {
                    if (i == 0 || i == 1 || i == 2 || i == 3) {
                        processedChar = true;
                        sb.append(phone.charAt(i));
                    }
                } else {
                    if (i == 0) {
                        processedChar = true;
                        sb.append(phone.charAt(i));
                    }
                }
                if (!processedChar) {
                    if (i == phone.length() - 1) {
                        sb.append(phone.charAt(i));
                    } else {
                        sb.append('*');
                    }
                }
            }
            player.setPhone(sb.toString());
        }
    }
}
    