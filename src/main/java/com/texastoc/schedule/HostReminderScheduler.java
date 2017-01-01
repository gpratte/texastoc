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
public class HostReminderScheduler {

    @Autowired
    MailService mailService;
    @Autowired
    SeasonService seasonService;
    @Autowired
    PlayerService playerService;
    
    @Scheduled(cron="0 0 9 * * ?")
    public void process() {
        GregorianCalendar cal = new GregorianCalendar();
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY || 
                cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY ||
                cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ||
                cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            
            LocalDate sunday = getSunday();
            
            // Check if there is a game for this week
            Season currentSeason = seasonService.getCurrent();
            currentSeason = seasonService.findById(currentSeason.getId());
            boolean found = false;
            for (Game game : currentSeason.getGames()) {
                if (game.getGameDate().isAfter(sunday)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                LocalDate monday = getMonday();
                LocalDate friday = getFriday();
                List<Player> possibleHosts = playerService.findActive();
                mailService.sendHostReminder(possibleHosts, monday, friday);
            }
        }
    }
    
    private LocalDate getSunday() {
        GregorianCalendar cal = new GregorianCalendar();
        int numDays = 0;
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            numDays = -1;
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            numDays = -2;
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            numDays = -3;
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            numDays = -4;
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            numDays = -5;
        }
        cal.add(Calendar.DAY_OF_MONTH, numDays);
        return new LocalDate(cal.getTimeInMillis());
    }
    private LocalDate getMonday() {
        GregorianCalendar cal = new GregorianCalendar();
        int numDays = 0;
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            numDays = -1;
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            numDays = -2;
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            numDays = -3;
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            numDays = -4;
        }
        cal.add(Calendar.DAY_OF_MONTH, numDays);
        return new LocalDate(cal.getTimeInMillis());
    }
    private LocalDate getFriday() {
        GregorianCalendar cal = new GregorianCalendar();
        int numDays = 0;
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            numDays = 4;
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            numDays = 3;
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            numDays = 3;
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            numDays = 1;
        }
        cal.add(Calendar.DAY_OF_MONTH, numDays);
        return new LocalDate(cal.getTimeInMillis());
    }
}
