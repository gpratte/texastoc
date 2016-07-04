package com.texastoc.schedule;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.texastoc.domain.Game;
import com.texastoc.domain.Player;
import com.texastoc.domain.Season;
import com.texastoc.service.PlayerService;
import com.texastoc.service.SeasonService;
import com.texastoc.service.mail.MailService;

@Service
public class TransportRequiredScheduler {

    @Autowired
    MailService mailService;
    @Autowired
    SeasonService seasonService;
    @Autowired
    PlayerService playerService;
    
    @Scheduled(cron="0 0 10 * * ?")
    public void process() {

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

        if (found && currentGame.isTransportRequired()) {
            Integer hostId = currentGame.getHostId();
            Player host = null;
            if (hostId != null) {
                host = playerService.findById(hostId);
            }
            
            List<Player> possibleTransporters = null;
            GregorianCalendar cal = new GregorianCalendar();
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY || 
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY || 
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
                possibleTransporters = playerService.findActive();
            } else {
                possibleTransporters = playerService.findPossibleTransporters();
            }
            
            mailService.sendTransportReminder(host, possibleTransporters);
        }
    }
    
}
