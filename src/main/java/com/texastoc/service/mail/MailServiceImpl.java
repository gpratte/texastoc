package com.texastoc.service.mail;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.texastoc.dao.PlayerDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePayout;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Player;
import com.texastoc.domain.PlayerCount;
import com.texastoc.domain.QuarterlySeason;
import com.texastoc.domain.QuarterlySeasonPlayer;
import com.texastoc.domain.Season;
import com.texastoc.domain.SeasonPlayer;
import com.texastoc.domain.Seat;
import com.texastoc.util.DateConverter;

@Component
public class MailServiceImpl implements MailService {

    static final Logger logger = Logger.getLogger(MailServiceImpl.class);
    private static final String EVITE = "Evite";
    private static final String EVITE_SENT = "TOC evite has been sent";

    @Autowired
    PlayerDao playerDao;

    @Value("${send.email}")
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
        sb.append(seat.getPlayer().getFullName() + "  Your Table "
                + seat.getTable() + "  Your Seat " + seat.getPosition());

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
    public void sendHostReminder(List<Player> possibleHosts, LocalDate monday,
            LocalDate friday) {

        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        if (possibleHosts == null || possibleHosts.size() < 1) {
            return;
        }

        //logger.info("sendHostReminder possibleHosts " + possibleHosts);
        StringBuilder recipients = new StringBuilder();
        boolean addComma = false;
        for (Player player : possibleHosts) {
            if (StringUtils.isNotBlank(player.getEmail())) {
                if (addComma) {
                    recipients.append(",");
                } else {
                    addComma = true;
                }
                recipients.append(player.getEmail());
            }
        }

        StringBuilder subject = new StringBuilder("Need a host ");
        if (monday != null || friday != null) {
            subject.append("(week of ");
            if (monday != null && friday != null) {
                subject.append(getMonthDay(monday) + "-" + getMonthDay(friday));
            } else if (monday != null) {
                subject.append(getMonthDay(monday));
            } else if (friday != null) {
                subject.append(getMonthDay(friday));
            }
            subject.append(")");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + recipients.toString() + "',");
        sb.append("Subject: '" + subject.toString() + "',");
        sb.append("HtmlBody: '");

        sb.append("Who will host?");

        sb.append("'");
        sb.append("}");

        send(sb.toString());
    }

    @Override
    public void sendTransportReminder(Player host,
            List<Player> possibleTransporters) {

        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        if (possibleTransporters == null || possibleTransporters.size() < 1) {
            return;
        }

        StringBuilder recipients = new StringBuilder();
        boolean addComma = false;
        for (Player player : possibleTransporters) {
            if (StringUtils.isNotBlank(player.getEmail())) {
                if (addComma) {
                    recipients.append(",");
                } else {
                    addComma = true;
                }
                recipients.append(player.getEmail());
            }
        }

        if (host != null && recipients.indexOf(host.getEmail()) == -1) {
            if (addComma) {
                recipients.append(",");
            } else {
                addComma = true;
            }
            recipients.append(host.getEmail());
        }

        StringBuilder subject = new StringBuilder("Transport supplies");

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + recipients.toString() + "',");
        sb.append("Subject: '" + subject.toString() + "',");
        sb.append("HtmlBody: '");

        sb.append("The supplies need to be transported. Who will step up?");

        sb.append("'");
        sb.append("}");

        send(sb.toString());
    }

    @Override
    public void sendPtcg(Player host, List<Player> ptcgs, LocalDate gameDate) {

        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        if (ptcgs == null || ptcgs.size() < 1) {
            return;
        }

        StringBuilder recipients = new StringBuilder();
        boolean addComma = false;
        for (Player player : ptcgs) {
            if (player.isPtcg() && StringUtils.isNotBlank(player.getEmail())) {
                if (addComma) {
                    recipients.append(",");
                } else {
                    addComma = true;
                }
                recipients.append(player.getEmail());
            }
        }

        if (host != null && StringUtils.isNotBlank(host.getEmail())
                && recipients.indexOf(host.getEmail()) == -1) {
            if (addComma) {
                recipients.append(",");
            } else {
                addComma = true;
            }
            recipients.append(host.getEmail());
        }

        StringBuilder subject = new StringBuilder("PTCG ("
                + DateConverter.getDateAsString(gameDate) + ")");

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + recipients.toString() + "',");
        sb.append("Subject: '" + subject.toString() + "',");
        sb.append("HtmlBody: '");

        sb.append("Is there a PTCG? If so, who is in?");

        sb.append("'");
        sb.append("}");

        send(sb.toString());
    }

    @Override
    public void sendRally(Player fromPlayer, Player host, List<Player> actives,
            String message, LocalDate gameDate) {

        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        if (actives == null || actives.size() < 1) {
            return;
        }

        if (StringUtils.isBlank(message)) {
            return;
        }

        StringBuilder recipients = new StringBuilder();
        boolean addComma = false;
        for (Player player : actives) {
            if (player.isActive() && StringUtils.isNotBlank(player.getEmail())) {
                if (addComma) {
                    recipients.append(",");
                } else {
                    addComma = true;
                }
                recipients.append(player.getEmail());
            }
        }

        if (host != null && StringUtils.isNotBlank(host.getEmail())
                && recipients.indexOf(host.getEmail()) == -1) {
            if (addComma) {
                recipients.append(",");
            } else {
                addComma = true;
            }
            recipients.append(host.getEmail());
        }

        if (fromPlayer != null && StringUtils.isNotBlank(fromPlayer.getEmail())
                && recipients.indexOf(fromPlayer.getEmail()) == -1) {
            if (addComma) {
                recipients.append(",");
            } else {
                addComma = true;
            }
            recipients.append(fromPlayer.getEmail());
        }

        StringBuilder subject = new StringBuilder("Rally! ("
                + DateConverter.getDateAsString(gameDate) + ")");

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + recipients.toString() + "',");
        sb.append("Subject: '" + subject.toString() + "',");
        sb.append("HtmlBody: '");

        sb.append(fromPlayer.getFullName() + " says:<br><br>");

        message = StringUtils.replace(message, "'", "");
        sb.append(message);

        sb.append("'");
        sb.append("}");

        send(sb.toString());
    }

    @Override
    public void sendEviteHasBeenSent(List<Player> core) {

        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        if (core == null || core.size() < 1) {
            return;
        }

        for (Player player : core) {
            if (StringUtils.isNotBlank(player.getPhone()) && StringUtils.isNotBlank(player.getCellCarrier())) {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("From: 'info@texastoc.com',");
                sb.append("To: '" + player.getPhone() + player.getCellCarrier() + "',");
                sb.append("Subject: '" + EVITE + "',");
                sb.append("HtmlBody: '");

                sb.append(EVITE_SENT);

                sb.append("'");
                sb.append("}");

                send(sb.toString());
            }
        }

    }

    @Override
    public void sendToGroup(Player fromPlayer, List<Player> players,
            String subject, String message) {

        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        if (players == null || players.size() < 1) {
            return;
        }

        if (StringUtils.isBlank(message)) {
            return;
        }

        StringBuilder recipients = new StringBuilder();
        boolean addComma = false;
        for (Player player : players) {
            if (StringUtils.isNotBlank(player.getEmail())) {
                if (addComma) {
                    recipients.append(",");
                } else {
                    addComma = true;
                }
                recipients.append(player.getEmail());
            }
        }

        if (fromPlayer != null && StringUtils.isNotBlank(fromPlayer.getEmail())
                && recipients.indexOf(fromPlayer.getEmail()) == -1) {
            if (addComma) {
                recipients.append(",");
            } else {
                addComma = true;
            }
            recipients.append(fromPlayer.getEmail());
        }

        if (subject == null) {
            subject = "Listen up";
        } else {
            subject = StringUtils.replace(subject, "'", "");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + recipients.toString() + "',");
        sb.append("Subject: '" + subject.toString() + "',");
        sb.append("HtmlBody: '");

        sb.append(fromPlayer.getFullName() + " says:<br><br>");

        message = StringUtils.replace(message, "'", "");
        sb.append(message);

        sb.append("'");
        sb.append("}");

        send(sb.toString());
    }

    @Override
    public void sendNewPassword(String email, String password) {

        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        StringBuilder subject = new StringBuilder("Password reset");

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + email + "',");
        sb.append("Subject: '" + subject.toString() + "',");
        sb.append("HtmlBody: '");

        sb.append(password);

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
                sb.append("<td align=\"center\">" + player.getFinish()
                        + "</td>");
            } else {
                sb.append("<td></td>");
            }
            sb.append("<td>");
            sb.append(player.getPlayer().getFullName());
            sb.append("</td>");

            if (player.getPoints() != null) {
                sb.append("<td align=\"center\">" + player.getPoints()
                        + "</td>");
            } else {
                sb.append("<td></td>");
            }

            if (player.getChop() != null) {
                sb.append("<td>" + player.getChop() + "</td>");
            } else {
                sb.append("<td></td>");
            }

            if (player.isAnnualTocPlayer()) {
                sb.append("<td align=\"center\">Yes</td>");
            } else {
                sb.append("<td></td>");
            }

            if (player.isQuarterlyTocPlayer()) {
                sb.append("<td align=\"center\">Yes</td>");
            } else {
                sb.append("<td></td>");
            }

            if (player.getBuyIn() != null && player.getBuyIn() > 0) {
                sb.append("<td align=\"center\">$" + player.getBuyIn()
                        + "</td>");
            } else {
                sb.append("<td></td>");
            }

            if (player.getReBuyIn() != null && player.getReBuyIn() > 0) {
                sb.append("<td align=\"center\">$" + player.getReBuyIn()
                        + "</td>");
            } else {
                sb.append("<td></td>");
            }

            if (player.getNote() != null) {
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

        for (GamePlayer gamePlayer : game.getPlayers()) {
            if (gamePlayer.isEmailOptIn()
                    && StringUtils
                            .isNotBlank(gamePlayer.getPlayer().getEmail())) {
                sendGameChangedEmail(gamePlayer.getPlayer().getEmail(),
                        sb.toString());
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
    public void sendBlindsUpMail(Game game, String round, int small, int big,
            int ante) {

        if (!StringUtils.equals("true", sendEmail)) {
            return;
        }

        for (GamePlayer gamePlayer : game.getPlayers()) {
            if (!gamePlayer.isOptIn()) {
                continue;
            }

            if (gamePlayer.getPlayer().getCellCarrier() == null
                    || StringUtils.isEmpty(gamePlayer.getPlayer()
                            .getCellCarrier())
                    || gamePlayer.getPlayer().getPhone() == null
                    || StringUtils.isEmpty(gamePlayer.getPlayer().getPhone())) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("From: 'info@texastoc.com',");
            sb.append("To: '" + gamePlayer.getPlayer().getPhone()
                    + gamePlayer.getPlayer().getCellCarrier() + "',");
            sb.append("Subject: 'Blinds are up',");
            sb.append("HtmlBody: '");
            sb.append(round + " small " + small + " big " + big + " ante "
                    + ante);
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
        sb.append("To: '" + gamePlayer.getPlayer().getPhone()
                + gamePlayer.getPlayer().getCellCarrier() + "',");
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

    @Override
    public void sendSeasonSummary(Player player, Game game, Season season,
            QuarterlySeason qSeason, List<GamePayout> payouts,
            List<PlayerCount> hosts) {

        if (game == null || player == null
                || StringUtils.isBlank(player.getEmail())) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("From: 'info@texastoc.com',");
        sb.append("To: '" + player.getEmail() + "',");
        sb.append("Subject: 'Summary " + game.getGameDateText() + "',");
        sb.append("HtmlBody: '");

        sb.append("<table>");
        sb.append(" <tr>");
        sb.append("  <td colspan=\"2\" align=\"center\">");
        sb.append("   <h2 align=\"center\">Game</h2>");
        sb.append("  </td>");
        sb.append(" </tr>");
        sb.append(" <tr>");
        sb.append("  <table>");
        sb.append("   <tr>");
        sb.append("    <td colspan=\"2\">Season games " + game.getAnnualIndex()
                + "</td>");
        sb.append("   </tr>");
        sb.append("   <tr>");
        sb.append("    <td colspan=\"2\">Quarterly games "
                + game.getQuarterlyIndex() + "</td>");
        sb.append("   </tr>");
        sb.append("   <tr>");
        sb.append("    <td>Host:</td>");
        if (game.getHostId() == null) {
            sb.append("    <td></td>");
        } else {
            Player host = playerDao.selectById(game.getHostId());
            if (host != null) {
                sb.append("    <td>");
                sb.append(host.getFullName());
                sb.append("</td>");
            }
        }
        sb.append("   <tr>");
        sb.append("    <td>Date:</td>");
        sb.append("    <td>" + game.getGameDateText() + "</td>");
        sb.append("   </tr>");
        sb.append("   <tr>");
        sb.append("    <td>Double buy in:</td>");
        if (game.isDoubleBuyIn())
            sb.append("<td>Yes</td>");
        else
            sb.append("<td>No</td>");
        sb.append("   </tr>");
        sb.append("   <tr>");
        sb.append("    <td>Number of Players:</td>");
        sb.append("    <td>" + game.getNumPlayers() + "</td>");
        sb.append("   </tr>");
        sb.append("   <tr>");
        sb.append("    <td>Total buy in:</td>");
        sb.append("    <td>$" + game.getTotalBuyIn() + "</td>");
        sb.append("   </tr>");
        sb.append("   <tr>");
        sb.append("    <td>Total Re-buy/Add-on:</td>");
        sb.append("    <td>$" + game.getTotalReBuy() + "</td>");
        sb.append("   </tr>");
        sb.append("   <tr>");
        sb.append("    <td>Total Annual TOC:</td>");
        sb.append("    <td>$" + game.getTotalAnnualToc() + "</td>");
        sb.append("   </tr>");
        sb.append("   <tr>");
        sb.append("    <td>Total Quarterly TOC:</td>");
        sb.append("    <td>$" + game.getTotalQuarterlyToc() + "</td>");
        sb.append("   </tr>");
        sb.append("   <tr>");
        sb.append("    <td>Kitty:</td>");
        sb.append("    <td>$");
        sb.append(game.getKittyDebit());
        sb.append("</td>");
        sb.append("   <tr>");
        sb.append("    <td>Notes:</td>");
        sb.append("    <td>" + game.getNote() + "</td>");
        sb.append("   </tr>");
        sb.append("  </table>");
        sb.append(" </tr>");

        sb.append(" </tr>");
        sb.append("  <td>");
        sb.append("   <table>");
        sb.append("    <tr>");
        sb.append("     <th>Place</th>");
        sb.append("     <th>Payout</th>");
        sb.append("    </tr>");
        for (GamePayout payout : payouts) {
            sb.append("    <td>" + payout.getPlace() + "<td>");
            if (payout.getChopAmount() == null) {
                sb.append("    <td>$" + payout.getAmount() + "<td>");
            } else {
                sb.append("    <td>$" + payout.getChopAmount() + "<td>");
            }
            sb.append("    </tr>");
        }
        sb.append("   </table>");
        sb.append("  </td>");
        sb.append(" </tr>");

        sb.append(" <tr>");
        sb.append("  <td>");
        sb.append("   <table>");
        sb.append("    <tr>");
        sb.append("     <th>Fin</th>");
        sb.append("     <th>Name</th>");
        sb.append("     <th>Pts</th>");
        sb.append("     <th>Chp</th>");
        sb.append("     <th>TOC</th>");
        sb.append("     <th>QTOC</th>");
        sb.append("     <th>Buy<br/>In</th>");
        sb.append("     <th>Re<br/>Buy</th>");
        sb.append("    </tr>");

        for (GamePlayer gamePlayer : game.getPlayers()) {
            sb.append("    <tr>");
            sb.append("     <td align=\"center\">"
                    + (gamePlayer.getFinish() == null ? "" : gamePlayer
                            .getFinish()) + "</td>");
            sb.append("     <td>");
            sb.append(gamePlayer.getPlayer().getFullName());
            sb.append("     </td>");
            sb.append("     <td align=\"center\">"
                    + (gamePlayer.getPoints() == null ? "" : gamePlayer
                            .getPoints()) + "</td>");

            if (gamePlayer.getChop() != null && gamePlayer.getChop() > 0)
                sb.append("     <td>" + gamePlayer.getChop() + "</td>");
            else
                sb.append("     <td></td>");
            if (gamePlayer.isAnnualTocPlayer())
                sb.append("     <td align=\"center\">Yes</td>");
            else
                sb.append("     <td></td>");
            if (gamePlayer.isQuarterlyTocPlayer())
                sb.append("     <td align=\"center\">Yes</td>");
            else
                sb.append("     <td></td>");
            if (gamePlayer.getBuyIn() != null && gamePlayer.getBuyIn() > 0)
                sb.append("     <td align=\"center\">" + gamePlayer.getBuyIn()
                        + "</td>");
            else
                sb.append("     <td></td>");
            if (gamePlayer.getReBuyIn() != null && gamePlayer.getReBuyIn() > 0)
                sb.append("     <td align=\"center\">"
                        + gamePlayer.getReBuyIn() + "</td>");
            else
                sb.append("     <td></td>");
            sb.append("    </tr>");
        }

        sb.append("   </table>");
        sb.append("  </td>");
        sb.append(" </tr>");
        sb.append("</table>");
        sb.append("<p/>");

        sb.append("<hr/>");
        sb.append("<table>");
        sb.append(" <tr>");
        sb.append("  <td colspan=\"2\" align=\"center\">");
        sb.append("   <h2 align=\"center\">Season</h2>");
        sb.append("  </td>");
        sb.append(" </tr>");
        sb.append(" <tr>");
        sb.append("  <td colspan=\"2\">Games played " + game.getAnnualIndex()
                + "  </td>");
        sb.append(" </tr>");
        sb.append(" <tr>");
        sb.append("  <td>Start date:</td>");
        sb.append("  <td>" + season.getStartDateText() + "</td>");
        sb.append(" </tr>");
        sb.append(" <tr>");
        sb.append("  <td>End date:</td>");
        sb.append("  <td>" + season.getEndDateText() + "</td>");
        sb.append(" </tr>");

        sb.append(" <tr>");
        sb.append("  <td>Total buy in:</td>");
        sb.append("  <td>$" + season.getTotalBuyIn() + "</td>");
        sb.append(" </tr>");
        sb.append(" <tr>");
        sb.append("  <td>Total rebuy/add on:</td>");
        sb.append("  <td>$" + season.getTotalReBuy() + "</td>");
        sb.append(" </tr>");
        sb.append(" <tr>");
        sb.append("  <td>Total Annual TOC:</td>");
        sb.append("  <td>$" + season.getTotalAnnualToc() + "</td>");
        sb.append(" </tr>");
        sb.append(" <tr>");
        sb.append("  <td>Notes:</td>");
        sb.append("  <td>" + (season.getNote() == null ? "" : season.getNote())
                + "</td>");
        sb.append(" </tr>");
        sb.append("</table>");

        sb.append("<p/>");

        sb.append("<table>");
        sb.append(" <tr>");
        sb.append("  <th>Place</th>");
        sb.append("  <th>Name</th>");
        sb.append("  <th>Points</th>");
        sb.append("  <th>Entries</th>");
        sb.append(" </tr>");
        for (SeasonPlayer seasonPlayer : season.getSeasonPlayers()) {
            sb.append(" <tr>");
            if (seasonPlayer.getPlace() > 0)
                sb.append("  <td align=\"center\">" + seasonPlayer.getPlace()
                        + "</td>");
            else
                sb.append("  <td align=\"center\"></td>");

            sb.append("  <td align=\"right\">"
                    + seasonPlayer.getPlayer().getFullName() + "</td>");

            if (seasonPlayer.getPoints() > 0)
                sb.append("  <td align=\"center\">" + seasonPlayer.getPoints()
                        + "</td>");
            else
                sb.append("  <td align=\"center\"></td>");

            sb.append("  <td align=\"center\">" + seasonPlayer.getNumEntries()
                    + "</td>");
            sb.append(" </tr>");
        }

        sb.append("</table>");

        sb.append("<hr/>");
        sb.append("<p/>");

        sb.append("<table>");
        sb.append(" <tr>");
        sb.append("  <td colspan=\"2\" align=\"center\">");
        sb.append("   <h2 align=\"center\">Quarterly Season</h2>");
        sb.append("  </td>");
        sb.append(" </tr>");
        sb.append(" <tr>");
        sb.append("  <td>");
        sb.append("   <table>");
        sb.append("    <tr>");
        sb.append("     <td colspan=\"2\">Games played "
                + game.getQuarterlyIndex() + "</td>");
        sb.append("    </tr>");
        sb.append("    <tr>");
        sb.append("     <td>Quarter</td>");
        sb.append("    <td>" + qSeason.getQuarter().getText() + "</td>");
        sb.append("    </tr>");
        sb.append("    <tr>");
        sb.append("     <td>Start date</td>");
        sb.append("     <td>" + qSeason.getStartDateText() + "</td>");
        sb.append("    </tr>");
        sb.append("    <tr>");
        sb.append("     <td>End date</td>");
        sb.append("     <td>" + qSeason.getEndDateText() + "</td>");
        sb.append("    </tr>");
        sb.append("    <tr>");
        sb.append("     <td>Total Quarterly TOC</td>");
        sb.append("     <td>$" + qSeason.getTotalQuarterlyToc() + "</td>");
        sb.append("    </tr>");
        sb.append("    <tr>");
        sb.append("     <td>Note</td>");
        sb.append("     <td>" + qSeason.getNote() + "</td>");
        sb.append("    </tr>");
        sb.append("   </table>");
        sb.append("  </td>");
        sb.append(" </tr>");

        sb.append(" <tr>");
        sb.append("  <td>");
        sb.append("   <table>");
        sb.append("    <tr>");
        sb.append("     <th>Place</th>");
        sb.append("     <th>Name</th>");
        sb.append("     <th>Points</th>");
        sb.append("     <th>Entries</th>");
        sb.append("    </tr>");
        for (QuarterlySeasonPlayer qsPlayer : qSeason
                .getQuarterlySeasonPlayers()) {
            sb.append("    <tr>");
            if (qsPlayer.getPlace() > 0)
                sb.append("     <td align=\"center\">" + qsPlayer.getPlace()
                        + "</td>");
            else
                sb.append("     <td align=\"center\"></td>");

            sb.append("     <td align=\"right\">"
                    + qsPlayer.getPlayer().getFullName() + "</td>");
            if (qsPlayer.getPoints() > 0)
                sb.append("     <td align=\"center\">" + qsPlayer.getPoints()
                        + "</td>");
            else
                sb.append("     <td align=\"center\"></td>");

            sb.append("     <td align=\"center\">" + qsPlayer.getNumEntries()
                    + "</td>");
            sb.append("    </tr>");
        }
        sb.append("   </table>");
        sb.append("  </td>");
        sb.append(" </tr>");
        sb.append("</table>");

        sb.append("<hr/>");
        sb.append("<table>");
        sb.append(" <tr>");
        sb.append("  <td colspan=\"2\" align=\"center\">");
        sb.append("  <h2 align=\"center\">Hosts</h2>");
        sb.append("  </td>");
        sb.append(" </tr>");
        sb.append(" <tr>");
        sb.append("  <td>");
        sb.append("   <table>");
        sb.append("    <tr>");
        sb.append("     <th>Name</th>");
        sb.append("     <th>Times</th>");
        sb.append("    </tr>");
        for (PlayerCount host : hosts) {
            sb.append("    <tr>");
            sb.append("     <td>" + host.getPlayer().getFullName() + "</td>");
            sb.append("     <td>" + host.getCount() + "</td>");
            sb.append("    </tr>");
        }
        sb.append("   </table>");
        sb.append("  </td>");
        sb.append(" </tr>");
        sb.append("</table>");

        sb.append("'");
        sb.append("}");
        send(sb.toString());
    }

    private String send(String emailPayload) {
        if (!StringUtils.equals("true", sendEmail)) {
            return "Not sending because the sendEmail flag is false";
        }

        logger.info("Attemping to send email " + emailPayload);

        String response = null;

        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost("https://api.postmarkapp.com/email");
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-Type", "application/json");
            post.setHeader("X-Postmark-Server-Token",
                    "cf3ef3e4-f688-45ba-8139-bb7476c685c5");
            StringEntity payload = new StringEntity(emailPayload, "UTF-8");
            post.setEntity(payload);

            try {
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = client.execute(post, responseHandler);
            } catch (HttpResponseException hre) {
                switch (hre.getStatusCode()) {
                case 401:
                case 422:
                    logger.warn("There was a problem with the email: "
                            + hre.getMessage());
                    break;
                case 500:
                    logger.warn("There has been an error sending your email: "
                            + hre.getMessage());
                    break;
                default:
                    logger.warn("There has been an unknow error sending your email: "
                            + hre.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Could not send email " + e);
        }

        return response;
    }

    private String getMonthDay(LocalDate date) {
        if (date == null) {
            return "";
        }
        return Integer.toString(date.getMonthOfYear()) + "/"
                + Integer.toString(date.getDayOfMonth());
    }
}
