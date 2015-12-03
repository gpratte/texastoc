package com.texastoc.service.calculate;

import java.util.ArrayList;
import java.util.List;

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
    private GamePlayerDao gamePlayerDao;
    @Autowired
    private ChopCalculator chopCalculator;
    @Autowired
    private PointSystemService pointSystemService;

    // TODO cache results
    public void calculate(Game game) {
        TopTenPoints ttp = pointSystemService.getTopTenPoints(game
                .getNumPlayers());

        for (GamePlayer player : game.getPlayers()) {

            if (player.getPoints() != null) {
                player.setPoints(null);
            }

            if (player.getFinish() != null && player.getFinish() <= 10) {
                if (player.isAnnualTocPlayer() || player.isQuarterlyTocPlayer()) {
                    player.setPoints(ttp.getPointsForPlace(player.getFinish()));
                } else {
                    player.setNonTocPoints(ttp.getPointsForPlace(player.getFinish()));
                }
            }

            gamePlayerDao.update(player);
        }

        // See if there is a chop
        List<Integer> chips = null;
        List<Integer> amounts = null;
        for (GamePlayer player : game.getPlayers()) {
            if (player.getChop() != null) {
                if (chips == null) {
                    chips = new ArrayList<Integer>();
                    chips.add(player.getChop());
                    amounts = new ArrayList<Integer>();
                    if (player.getPoints() != null) {
                        amounts.add(player.getPoints());
                    } else if (player.getNonTocPoints() != null) {
                        amounts.add(player.getNonTocPoints());
                    }
                } else {
                    boolean inserted = false;
                    for (int i = 0; i < chips.size(); ++i) {
                        if (player.getChop().intValue() >= chips.get(i).intValue()) {
                            chips.add(i, player.getChop());
                            if (player.getPoints() != null) {
                                amounts.add(i, player.getPoints());
                            } else if (player.getNonTocPoints() != null) {
                                amounts.add(i, player.getNonTocPoints());
                            }
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) {
                        chips.add(player.getChop());
                        if (player.getPoints() != null) {
                            amounts.add(player.getPoints());
                        } else if (player.getNonTocPoints() != null) {
                            amounts.add(player.getNonTocPoints());
                        }
                    }
                }
            }
        }
        
        if (chips != null) {
            List<Chop> chops = chopCalculator.calculate(chips, amounts);
            if (chops != null && chops.size() > 1) {
                for (Chop chop : chops) {
                    for (GamePlayer player : game.getPlayers()) {
                        if (player.getPoints() != null && 
                                player.getPoints().intValue() == chop.getOrgAmount()) {
                            player.setPoints(chop.getChopAmount());
                            gamePlayerDao.update(player);
                            break;
                        }
                    }
                }
            }
        }
    }
}
