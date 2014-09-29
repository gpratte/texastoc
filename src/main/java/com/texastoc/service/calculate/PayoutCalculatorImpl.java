
package com.texastoc.service.calculate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.GameDao;
import com.texastoc.dao.GamePayoutDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePayout;
import com.texastoc.domain.GamePlayer;

@Service
public class PayoutCalculatorImpl implements PayoutCalculator {

    @Autowired
    GameDao gameDao;
    @Autowired
    GamePayoutDao gamePayoutDao;

    static final Logger logger = Logger.getLogger(PayoutCalculatorImpl.class);

    private static HashMap<Integer, HashMap<Integer,Float>> PAYOUTS = 
            new HashMap<Integer, HashMap<Integer,Float>>();
    
    static {
        // http://www.hogwildpokerleagues.com/tournamentpayouts/
        
        HashMap<Integer, Float> percent = new HashMap<Integer, Float>();
        percent.put(1, 0.65f);
        percent.put(2, 0.35f);
        PAYOUTS.put(2, percent);

        percent = new HashMap<Integer, Float>();
        percent.put(1, 0.50f);
        percent.put(2, 0.30f);
        percent.put(3, 0.20f);
        PAYOUTS.put(3, percent);
        
        percent = new HashMap<Integer, Float>();
        percent.put(1, 0.45f);
        percent.put(2, 0.25f);
        percent.put(3, 0.18f);
        percent.put(4, 0.12f);
        PAYOUTS.put(4, percent);
        
        percent = new HashMap<Integer, Float>();
        percent.put(1, 0.40f);
        percent.put(2, 0.23f);
        percent.put(3, 0.16f);
        percent.put(4, 0.12f);
        percent.put(5, 0.09f);
        PAYOUTS.put(5, percent);
        
        percent = new HashMap<Integer, Float>();
        percent.put(1, 0.38f);
        percent.put(2, 0.22f);
        percent.put(3, 0.15f);
        percent.put(4, 0.11f);
        percent.put(5, 0.08f);
        percent.put(6, 0.06f);
        PAYOUTS.put(6, percent);
        
        percent = new HashMap<Integer, Float>();
        percent.put(1, 0.35f);
        percent.put(2, 0.21f);
        percent.put(3, 0.15f);
        percent.put(4, 0.11f);
        percent.put(5, 0.08f);
        percent.put(6, 0.06f);
        percent.put(7, 0.04f);
        PAYOUTS.put(7, percent);
        
        percent = new HashMap<Integer, Float>();
        percent.put(1, 0.335f);
        percent.put(2, 0.20f);
        percent.put(3, 0.145f);
        percent.put(4, 0.11f);
        percent.put(5, 0.08f);
        percent.put(6, 0.06f);
        percent.put(7, 0.04f);
        percent.put(8, 0.03f);
        PAYOUTS.put(8, percent);
        
        percent = new HashMap<Integer, Float>();
        percent.put(1, 0.32f);
        percent.put(2, 0.195f);
        percent.put(3, 0.14f);
        percent.put(4, 0.11f);
        percent.put(5, 0.08f);
        percent.put(6, 0.06f);
        percent.put(7, 0.04f);
        percent.put(8, 0.03f);
        percent.put(9, 0.025f);
        PAYOUTS.put(9, percent);
        
        percent = new HashMap<Integer, Float>();
        percent.put(1, 0.30f);
        percent.put(2, 0.19f);
        percent.put(3, 0.1325f);
        percent.put(4, 0.105f);
        percent.put(5, 0.075f);
        percent.put(6, 0.055f);
        percent.put(7, 0.0375f);
        percent.put(8, 0.03f);
        percent.put(9, 0.0225f);
        percent.put(10, 0.015f);
        PAYOUTS.put(10, percent);
    }
    
    public HashMap<Integer, HashMap<Integer,Float>> getPayouts() {
        return PAYOUTS;
    }

    
    // TODO cache results
    public void calculate(int id) throws SQLException {
        Game game = gameDao.selectById(id);
        
        // round to multiple of 5 (e.g. 12 > 10 but 13 > 15)
        int numPlayers = (int)Math.round((double)game.getNumPlayers()/5) * 5;

        int numberPaid = numPlayers / 5;
        numberPaid += game.getPayoutDelta();
        
        // Always pay at least 2 players
        if (numberPaid < 2) {
            numberPaid = 2;
        }
        calculatePayout(numberPaid, game);
    }
    
    private void calculatePayout(int numToPay, Game game) throws SQLException {
        
        HashMap<Integer,Float> payouts = PAYOUTS.get(numToPay);
        int prizePot = game.getTotalBuyIn() + game.getTotalReBuy();
        int totalPayout = 0;
        List<GamePayout> gamePayouts = new ArrayList<GamePayout>();
        for (int i = 1; i <= numToPay; ++i) {
            GamePayout gp = new GamePayout();
            gp.setGameId(game.getId());
            gp.setPlace(i);
            float percent = payouts.get(i);
            int payout = Math.round(percent * prizePot);
            gp.setAmount(payout);
            totalPayout += payout;
            gamePayouts.add(gp);
        }
        
        if (totalPayout > prizePot) {
            int extra = totalPayout - prizePot;
            while (extra > 0) {
                for (int i = gamePayouts.size() - 1; i >= 0; --i) {
                    GamePayout gp = gamePayouts.get(i);
                    gp.setAmount(gp.getAmount() - 1);
                    if (--extra == 0) {
                        break;
                    }
                }
            }
        } else if (totalPayout < prizePot) {
            int extra = prizePot - totalPayout;
            while (extra > 0) {
                for (GamePayout gp : gamePayouts) {
                    gp.setAmount(gp.getAmount() + 1);
                    if (--extra == 0) {
                        break;
                    }
                }
            }
        }
        
        for (GamePlayer gamePlayer : game.getPlayers()) {
            if (gamePlayer.getFinish() != null && gamePlayer.getChop() != null) {
                calculateChopPayout(gamePayouts, game);
            }
        }
        
        gamePayoutDao.deleteAllByGameId(game.getId());
        
        for (GamePayout gp : gamePayouts) {
            gamePayoutDao.insert(gp);
        }
    }
    
    private void calculateChopPayout(List<GamePayout> gamePayouts, Game game) {
        int smallestPayout = 0;
        int numPlayersThatChopped = 0;
        int combinedPayout = 0;
        int combinedChips = 0;
        
        for (GamePlayer gamePlayer : game.getPlayers()) {
            if (gamePlayer.getFinish() != null && gamePlayer.getChop() != null) {
                GamePayout gamePayout = getPayoutForFinish(gamePlayer.getFinish().intValue(), gamePayouts);
                if (gamePayout == null) {
                    return;
                }
                int payout = gamePayout.getAmount();
                if (numPlayersThatChopped == 0) {
                    smallestPayout = payout;
                } else {
                    smallestPayout = Math.min(smallestPayout, payout);
                }
                
                ++numPlayersThatChopped;                
                combinedPayout += payout;
                combinedChips += gamePlayer.getChop();
            }
        }
        
        // Calculate percentage
        for (GamePlayer gamePlayer : game.getPlayers()) {
            if (gamePlayer.getFinish() != null && gamePlayer.getChop() != null) {
                GamePayout gamePayout = getPayoutForFinish(gamePlayer.getFinish().intValue(), gamePayouts);
                if (gamePayout == null) {
                    return;
                }
                float percent = gamePlayer.getChop() / (float) combinedChips;
                gamePayout.setChopPercent(percent);
            }
        }
        
        // For all that chopped give them the smallest and then their
        // percent of the remaining
        int amountToChop = combinedPayout - (smallestPayout * numPlayersThatChopped);
        for (GamePayout gamePayout : gamePayouts) {
            if (gamePayout.getChopPercent() != null) {
                int choppedAmount = (int)(amountToChop * gamePayout.getChopPercent());
                gamePayout.setChopAmount(choppedAmount + smallestPayout);
            }
        }
        
        // Now make sure all the money is account for (because when using
        // floats for percentages the int are rounded)
        int recalculatedCombinedPayouts = 0;
        for (GamePayout gamePayout : gamePayouts) {
            if (gamePayout.getChopPercent() != null) {
                recalculatedCombinedPayouts += gamePayout.getChopAmount();
            }
        }

        recalculatePayouts(combinedPayout - recalculatedCombinedPayouts, numPlayersThatChopped, gamePayouts);
    }

    private GamePayout getPayoutForFinish(int finish, List<GamePayout> gamePayouts) {
        for (GamePayout gamePayout : gamePayouts) {
            if (gamePayout.getPlace() == finish) {
                return gamePayout;
            }
        }
        
        return null;
    }

    private void recalculatePayouts(int delta, int numThatChopped, List<GamePayout> gamePayouts) {
        if (delta == 0) {
            return;
        } else if (delta > 0) {
            for (int i = 1; i <= delta; ++i) {
                changeChop(i, true, numThatChopped, gamePayouts);
            }
        } else {
            delta *= -1;
            for (int i = 1; i <= delta; --i) {
                changeChop(i, true, numThatChopped, gamePayouts);
            }
        }
    }
    
    private void changeChop(int place, boolean increase, int numThatChopped, List<GamePayout> gamePayouts) {
        int placeToChange = place % numThatChopped;
        if (placeToChange == 0) {
            placeToChange = numThatChopped;
        }
        
        GamePayout gamePayout = getPayoutForFinish(placeToChange, gamePayouts);
        if (increase) {
            gamePayout.setChopAmount(gamePayout.getChopAmount() + 1);
        } else {
            gamePayout.setChopAmount(gamePayout.getChopAmount() - 1);
        }
    }

}
