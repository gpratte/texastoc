package com.texastoc.service.calculate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.GameDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;


@Service
public class GameCalculatorImpl implements GameCalculator {

    @Autowired
    GameDao gameDao;
    @Autowired
    PointsCalculator pointCalculator;
    
    public void calculate(int id) throws Exception {
        Game game = gameDao.selectById(id);
        
        int totalBuyIn = 0;
        int totalReBuy = 0;
        int totalAnnualToc = 0;
        int totalQuarterlyToc = 0;
        int numPlayers = 0;
        
        for (GamePlayer gp : game.getPlayers()) {
            if (gp.getBuyIn() == null) {
                continue;
            }
            
            ++numPlayers;
            
            totalBuyIn += gp.getBuyIn();
            
            if (gp.getReBuyIn() != null) {
                totalReBuy += gp.getReBuyIn();
            }

            int toc = 0;
            if (gp.isAnnualTocPlayer()) {
                toc = 5;
                
                if (gp.isAnnualTocPlayer() && gp.getReBuyIn() != null) {
                    if (game.isDoubleBuyIn()) {
                        toc += 15;
                        totalReBuy -= 15;
                    } else {
                        toc += 10;
                        totalReBuy -= 10;
                    }
                }
            }
            totalAnnualToc += toc;

            if (gp.isQuarterlyTocPlayer()) {
                totalQuarterlyToc += 5;
            }
        }

        game.setNumPlayers(numPlayers);
        game.setTotalAnnualToc(totalAnnualToc);
        game.setTotalBuyIn(totalBuyIn);
        game.setTotalReBuy(totalReBuy);
        game.setTotalQuarterlyToc(totalQuarterlyToc);
        
        gameDao.update(game);
        
        pointCalculator.calculate(game);
    }
}
