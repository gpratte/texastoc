package com.texastoc.service.mail;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.texastoc.dao.PlayerDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePayout;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Player;
import com.texastoc.domain.Seat;

@Component
public class MailServiceImpl implements MailService {

    static final Logger logger = Logger.getLogger(MailServiceImpl.class);

    @Autowired
    PlayerDao playerDao;
    
    @Value( "${send.email}" )
    private String sendEmail;

    @Override
    public void sendSeatingMail(String to, Seat seat, Game game) {
        
        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }
        
        if (to == null) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + to + "',");
        sb.append("Subject: 'Seating',");
        sb.append("HtmlBody: '");
        sb.append(seat.getPlayer().getFullName() 
                + "  Your Table " + seat.getTable() 
                + "  Your Seat " + seat.getPosition());

        sb.append("               Other Players at your table");
        for (Seat gamePlayerSeat : game.getSeating()) {
            Player player = gamePlayerSeat.getPlayer();
            if (gamePlayerSeat.getTable() == seat.getTable()) {
                sb.append("  ==>" + player.getFullName() + " " 
                        + gamePlayerSeat.getPosition() + " ");
            }
        }

        sb.append("'");
        sb.append("}");
        
        send(sb.toString());
    }

    @Override
    public void sendGameChangedMail(Game game) {

        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("HtmlBody: '");

        sb.append("Date: " + game.getGameDateText());
        int toc = game.getTotalAnnualToc() + game.getTotalQuarterlyToc();
        sb.append("<br>TOC money : $" + toc);
        int pot = game.getTotalBuyIn() + game.getTotalReBuy();
        sb.append("<br>Pot to payout: $" + pot);
        
        sb.append("<table>");
        sb.append("<tr><td colspan=\"9\" align=\"center\"><h2>Players</h2></td></tr>");
        sb.append("<tr>");
        sb.append("<th>Finish</th>");
        sb.append("<th>Name</th>");
        sb.append("<th>Points</th>");
        sb.append("<th>Chop</th>");
        sb.append("<th>&nbsp;&nbsp;TOC&nbsp;&nbsp;</th>");
        sb.append("<th>&nbsp;&nbsp;QTOC&nbsp;&nbsp;</th>");
        sb.append("<th>Buy-in</th>");
        sb.append("<th title=\"Re-buy or Add-on\">Option</th>");
        sb.append("<th>Notes</th>");
        sb.append("</tr>");
    
        for (GamePlayer player : game.getPlayers()) {
            sb.append("<tr>");
            if (player.getFinish() != null) {
                sb.append("<td align=\"center\">" + player.getFinish() + "</td>");
            } else {
                sb.append("<td></td>");
            }
            sb.append("<td>");
            sb.append(player.getPlayer().getFullName());
            sb.append("</td>");

            if (player.getPoints() != null) {
                sb.append("<td align=\"center\">" + player.getPoints() + "</td>");
            } else {
                sb.append("<td></td>");
            }
                    
            if(player.getChop() != null) {
                sb.append("<td>" + player.getChop() + "</td>");
            } else {
                sb.append("<td></td>");
            }

            if(player.isAnnualTocPlayer()) {
                sb.append("<td align=\"center\">Yes</td>");
            } else {
                sb.append("<td></td>");
            }
            
            if(player.isQuarterlyTocPlayer()) {
                sb.append("<td align=\"center\">Yes</td>");
            } else {
                sb.append("<td></td>");
            }

            if(player.getBuyIn() != null && player.getBuyIn() > 0) {
                sb.append("<td align=\"center\">$" + player.getBuyIn() + "</td>");
            } else {
                sb.append("<td></td>");
            }

            if(player.getReBuyIn() != null && player.getReBuyIn() > 0) {
                sb.append("<td align=\"center\">$" + player.getReBuyIn() + "</td>");
            } else {
                sb.append("<td></td>");
            }

            if(player.getNote() != null) {
                sb.append("<td align=\"center\">" + player.getNote() + "</td>");
            } else {
                sb.append("<td></td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");

        sb.append("<br>");
        sb.append("<table>");
        sb.append("<tr><td colspan=\"2\" align=\"center\"><h2>Payouts</h2></td></tr>");
        sb.append("<tr>");
        sb.append("<th>Place</th>");
        sb.append("<th>Amount</th>");
        sb.append("</tr>");

        for (GamePayout payout : game.getPayouts()) {
            sb.append("<tr>");
            sb.append("<td align=\"center\">" + payout.getPlace() + "</td>");
            sb.append("<td align=\"center\">$" + payout.getAmount() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        
        sb.append("'");

        Player gil = playerDao.selectByName("Gil", "Pratte");
        if (StringUtils.isNotBlank(gil.getEmail())) {
            sendGameChangedEmail(gil.getEmail(), sb.toString());
        }

        Player brian = playerDao.selectByName("Brian", "Lendecky");
        if (StringUtils.isNotBlank(brian.getEmail())) {
            sendGameChangedEmail(brian.getEmail(), sb.toString());
        }

        Player host = null;
        if (game.getHostId() != null) {
            host = playerDao.selectById(game.getHostId());
            if (host.getId() != gil.getId() && 
                    host.getId() != brian.getId()) {
                if (StringUtils.isNotBlank(host.getEmail())) {
                    sendGameChangedEmail(host.getEmail(), sb.toString());
                }
            }
        }
    }
    
    private void sendGameChangedEmail(String to, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + to + "',");
        sb.append("Subject: 'Game update',");
        sb.append(body);
        sb.append("}");
        
        send(sb.toString());
    }

    @Override
    public void sendBlindsUpMail(Game game, String round, int small, int big, int ante) {

        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        for (GamePlayer gamePlayer : game.getPlayers()) {
            if (!gamePlayer.isOptIn()) {
                continue;
            }
            
            if (gamePlayer.getPlayer().getCellCarrier() == null
                    || StringUtils.isEmpty(gamePlayer.getPlayer().getCellCarrier())
                    || gamePlayer.getPlayer().getPhone() == null
                    || StringUtils.isEmpty(gamePlayer.getPlayer().getPhone())) {
                continue;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("From: 'info@texastoc.com',");
            sb.append("To: '" + gamePlayer.getPlayer().getPhone() + 
                    gamePlayer.getPlayer().getCellCarrier() + "',");
            sb.append("Subject: 'Blinds are up',");
            sb.append("HtmlBody: '");
            sb.append(round + " small " + small
                    + " big " + big + " ante " + ante);
            sb.append("'");
            sb.append("}");

            send(sb.toString());
        }
    }

    @Override
    public void sendOptIn(GamePlayer gamePlayer) {
        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        if (!gamePlayer.isOptIn()) {
            return;
        }
        
        if (gamePlayer.getPlayer().getCellCarrier() == null
                || StringUtils.isEmpty(gamePlayer.getPlayer().getCellCarrier())
                || gamePlayer.getPlayer().getPhone() == null
                || StringUtils.isEmpty(gamePlayer.getPlayer().getPhone())) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + gamePlayer.getPlayer().getPhone() + 
                gamePlayer.getPlayer().getCellCarrier() + "',");
        sb.append("Subject: 'Clock updates',");
        sb.append("HtmlBody: '");
        sb.append("You have opted in to get clock round change text messages");
        sb.append("'");
        sb.append("}");

        send(sb.toString());
    }

    @Override
    public String sendEmail(String address) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + address + "',");
        sb.append("Subject: 'test',");
        sb.append("HtmlBody: '");
        sb.append("Test from texastoc");
        sb.append("'");
        sb.append("}");

        return send(sb.toString());
    }

    private String send(String emailPayload) {
        
        String response = null;
        
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost("https://api.postmarkapp.com/email");
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-Type", "application/json");
            post.setHeader("X-Postmark-Server-Token", "cf3ef3e4-f688-45ba-8139-bb7476c685c5");
            StringEntity payload = new StringEntity(emailPayload, "UTF-8");
            post.setEntity(payload);
            
            try {
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = client.execute(post, responseHandler);
            } catch (HttpResponseException hre) {
                switch(hre.getStatusCode()) {
                    case 401:
                    case 422:
                        logger.warn("There was a problem with the email: " + hre.getMessage());
                    case 500:
                        logger.warn("There has been an error sending your email: " + hre.getMessage());
                    default:
                        logger.warn("There has been an unknow error sending your email: " + hre.getMessage());
                }
            }
        } catch(Exception e) {
            logger.error("Could not send email " + e);
        }
        
        return response;
    }

}
