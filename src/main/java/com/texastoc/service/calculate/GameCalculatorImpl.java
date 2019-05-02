package com.texastoc.service.calculate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.GameDao;
import com.texastoc.dao.SeasonDao;
import com.texastoc.dao.SupplyDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Season;
import com.texastoc.domain.Supply;


@Service
public class GameCalculatorImpl implements GameCalculator {

    @Autowired
    GameDao gameDao;
    @Autowired
    SupplyDao supplyDao;
    @Autowired
    SeasonDao seasonDao;
    @Autowired
    PointsCalculator pointCalculator;
    
    public void calculate(int id) throws Exception {
        Game game = gameDao.selectById(id);
        
        int totalBuyIn = 0;
        int totalReBuy = 0;
        int totalAnnualToc = 0;
        int totalQuarterlyToc = 0;
        int numPlayers = 0;
        int totalPotSupplies = 0;
        int totalAnnualTocSupplies = 0;
        
        Season season = seasonDao.selectById(game.getSeasonId());
        int annualTocAmount = season.getAnnualTocAmount();
        int quarterlyTocAmount = season.getQuarterlyTocAmount();
        
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
                toc = annualTocAmount;
                
                if (gp.isAnnualTocPlayer() && gp.getReBuyIn() != null) {
                    if (game.isDoubleBuyIn()) {
                        toc += 15;
                        totalReBuy -= 15;
                    } else {
                        toc += 20;
                        totalReBuy -= 20;
                    }
                }
            }
            totalAnnualToc += toc;

            if (gp.isQuarterlyTocPlayer()) {
                totalQuarterlyToc += quarterlyTocAmount;
            }
        }

        List<Supply> supplies = supplyDao.selectSuppliesForGame(id);
        for (Supply supply : supplies) {
            if (supply.getPrizePotAmount() != null) {
                totalPotSupplies += supply.getPrizePotAmount();
            }
            if (supply.getAnnualTocAmount() != null) {
                totalAnnualTocSupplies += supply.getAnnualTocAmount();
            }
        }
        
        game.setNumPlayers(numPlayers);
        game.setTotalAnnualToc(totalAnnualToc);
        game.setTotalBuyIn(totalBuyIn);
        game.setTotalReBuy(totalReBuy);
        game.setTotalQuarterlyToc(totalQuarterlyToc);
        game.setTotalAnnualTocSupplies(totalAnnualTocSupplies);
        game.setTotalPotSupplies(totalPotSupplies);
        
        gameDao.update(game);
        
        pointCalculator.calculate(game);
    }
}
