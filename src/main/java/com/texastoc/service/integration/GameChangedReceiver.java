package com.texastoc.service.integration;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.texastoc.domain.Game;
import com.texastoc.service.mail.MailService;

@Component
public class GameChangedReceiver {

    static final Logger logger = Logger.getLogger(GameChangedReceiver.class);

    @Autowired
    MailService mailService;

    public void gameChanged(Game game) {
        if (!game.isFinalized()) {
            mailService.sendGameChangedMail(game);
        }
    }
}
