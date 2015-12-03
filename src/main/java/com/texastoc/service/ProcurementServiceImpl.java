package com.texastoc.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.GameDao;
import com.texastoc.dao.SupplyDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.Supply;
import com.texastoc.service.calculate.GameCalculator;
import com.texastoc.service.calculate.PayoutCalculator;

@Service
public class ProcurementServiceImpl implements ProcurementService {

    static final Logger logger = Logger.getLogger(ProcurementServiceImpl.class);

    @Autowired
    GameDao gameDao;
    @Autowired
    private SupplyDao supplyDao;
    @Autowired
    GameCalculator gameCalculator;
    @Autowired
    PayoutCalculator payoutCalculator;

    @Override
    public void procure(Supply supply) {
        if (supply.getSeasonId() == null && supply.getGameId() == null) {
            throw new IllegalArgumentException(
                    "Game id and season id cannot both be null");
        }

        if (supply.getSeasonId() == null && supply.getGameId() != null) {
            Game game = gameDao.selectById(supply.getGameId());
            supply.setSeasonId(game.getSeasonId());
        }

        if (supply.getCreateDate() == null) {
            supply.setCreateDate(new LocalDate());
        }

        supplyDao.create(supply);
        calculate(supply);
    }

    @Override
    public void update(Supply supply) {
        supplyDao.update(supply);
        calculate(supply);
    }

    @Override
    public Supply getSupply(int id) {
        return supplyDao.selectById(id);
    }

    @Override
    public List<Supply> getAllSupplies() {
        return supplyDao.selectAll();
    }

    @Override
    public List<Supply> getSuppliesForGame(int gameId) {
        return supplyDao.selectSuppliesForGame(gameId);
    }

    @Override
    public List<Supply> getSuppliesForSeason(int seasonId) {
        return supplyDao.selectSuppliesForSeason(seasonId);
    }

    @Override
    public void delete(int id) {
        Supply supply = supplyDao.selectById(id);
        supplyDao.delete(id);
        calculate(supply);
    }

    @Override
    public int getKitty() {
        int kitty = supplyDao.getKitty();
        int kittySpent = supplyDao.getKittySpent();
        return kitty - kittySpent;
    }

    private void calculate(Supply supply) {
        if (supply.getGameId() != null) {
            try {
                gameCalculator.calculate(supply.getGameId());
                payoutCalculator.calculate(supply.getGameId());
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    @Override
    public void addInvoice(int supplyId, byte[] invoice) {
        supplyDao.addInvoice(supplyId, invoice);
    }
    
    @Override
    public void removeInvoice(int supplyId)  {
        supplyDao.deleteInvoice(supplyId);
    }

    @Override
    public byte[] getInvoice(int supplyId) {
        return supplyDao.selectInvoice(supplyId);
    }

}
