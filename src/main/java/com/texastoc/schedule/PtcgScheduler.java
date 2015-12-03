package com.texastoc.schedule;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.texastoc.domain.Game;
import com.texastoc.domain.Player;
import com.texastoc.domain.Season;
import com.texastoc.service.GameServiceImpl;
import com.texastoc.service.PlayerService;
import com.texastoc.service.SeasonService;
import com.texastoc.service.mail.MailService;

@Service
public class PtcgScheduler {

    static final Logger logger = Logger.getLogger(PtcgScheduler.class);

    @Autowired
    SeasonService seasonService;
    @Autowired
    MailService mailService;
    @Autowired
    PlayerService playerService;
    
    @Scheduled(cron="0 0 11 * * ?")
    public void process() {
        GregorianCalendar cal = new GregorianCalendar();
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            // Check if there is a game for this week
            Season currentSeason = seasonService.getCurrent();
            currentSeason = seasonService.findById(currentSeason.getId());
            boolean found = false;
            Game currentGame = null;
            for (Game game : currentSeason.getGames()) {
                if (!game.isFinalized()) {
                    found = true;
                    currentGame = game;
                    break;
                }
            }

            if (currentGame != null) {
                Player host = playerService.findById(currentGame.getHostId());
                if (host != null) {
                    List<Player> ptcgs = playerService.findPtcg();
                    mailService.sendPtcg(host, ptcgs, currentGame.getGameDate());
                }
            }
        }
    }
    
}
