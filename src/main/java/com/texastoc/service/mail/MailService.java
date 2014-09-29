package com.texastoc.service.mail;

import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Seat;

public interface MailService {

    void sendGameChangedMail(Game game);
    void sendSeatingMail(String to, Seat seat, Game game);
    void sendBlindsUpMail(Game game, String round, int small, int big, int ante);
    void sendOptIn(GamePlayer gamePlayer);

    String sendEmail(String address);
}
