package com.texastoc.service.mail;

import java.util.List;

import org.joda.time.LocalDate;

import com.texastoc.domain.Game;
import com.texastoc.domain.GamePayout;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Player;
import com.texastoc.domain.PlayerCount;
import com.texastoc.domain.QuarterlySeason;
import com.texastoc.domain.Season;
import com.texastoc.domain.Seat;

public interface MailService {

    void sendGameChangedMail(Game game);
    void sendSeatingMail(String to, Seat seat, Game game);
    void sendBlindsUpMail(Game game, String round, int small, int big, int ante);
    void sendOptIn(GamePlayer gamePlayer);
    void sendSeasonSummary(Player player, Game game, Season season, 
            QuarterlySeason qSeason, List<GamePayout> payouts,
            List<PlayerCount> hosts);
    void sendHostReminder(List<Player> possibleHosts, LocalDate monday, LocalDate friday);
    void sendTransportReminder(Player host, List<Player> possibleTransporters);
    void sendPtcg(Player host, List<Player> ptgcs);
    void sendRally(Player host, List<Player> actives, String message);

    String sendEmail(String address);
}
