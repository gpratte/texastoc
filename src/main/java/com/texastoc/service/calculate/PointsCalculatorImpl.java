package com.texastoc.service.calculate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.GamePlayerDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.TopTenPoints;
import com.texastoc.service.PointSystemService;

@Service
public class PointsCalculatorImpl implements PointsCalculator {

    @Autowired
    PointSystemService pointSystemService;
    @Autowired
    GamePlayerDao gamePlayerDao;

    // TODO cache results
    public void calculate(Game game) {
        TopTenPoints ttp = pointSystemService.getTopTenPoints(game
                .getNumPlayers());

        ArrayList<GamePlayer> playersThatChopped = new ArrayList<GamePlayer>();
        int totalChips = 0;

        for (GamePlayer player : game.getPlayers()) {
            boolean update = false;

            if (player.getPoints() != null) {
                player.setPoints(null);
                update = true;
            }

            if (player.getFinish() != null && player.getFinish() <= 10) {
                if (player.getChop() == null || player.getChop() < 1) {
                    if (player.isAnnualTocPlayer() || player.isQuarterlyTocPlayer()) {
                        player.setPoints(ttp.getPointsForPlace(player.getFinish()));
                        update = true;
                    }
                }
            }

            if (player.getChop() != null && player.getChop() > 0) {
                playersThatChopped.add(player);
                totalChips += player.getChop();
            }

            try {
                gamePlayerDao.update(player);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (playersThatChopped.size() > 0) {
            int minPoints = ttp.getPointsForPlace(playersThatChopped.size());

            int totalPointsToChop = 0;
            for (int i = playersThatChopped.size(); i > 0; --i) {
                totalPointsToChop += ttp.getPointsForPlace(i) - minPoints;
            }

            Collections.sort(playersThatChopped);
            int pointsAwarded = 0;
            //int finish = 1;
            for (GamePlayer player : playersThatChopped) {
                //player.setFinish(finish++);
                double percentage = (double) player.getChop()
                        / (double) totalChips;
                double extraPointsD = percentage * totalPointsToChop;
                int extraPointsI = (int) Math.round(extraPointsD);
                pointsAwarded += extraPointsI;
                player.setPoints(minPoints + extraPointsI);
            }

            if (pointsAwarded < totalPointsToChop) {
                int leftOverPoints = totalPointsToChop - pointsAwarded;
                while (leftOverPoints > 0) {
                    for (GamePlayer player : playersThatChopped) {
                        player.setPoints(player.getPoints() + 1);
                        if (--leftOverPoints == 0) {
                            break;
                        }
                    }
                }
            }

            for (GamePlayer player : playersThatChopped) {
                try {
                    if (!player.isAnnualTocPlayer() && !player.isQuarterlyTocPlayer()) {
                        player.setPoints(null);
                    }
                    gamePlayerDao.update(player);
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
