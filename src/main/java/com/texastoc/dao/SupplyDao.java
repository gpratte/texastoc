package com.texastoc.dao;

import java.util.List;

import com.texastoc.domain.Supply;

public interface SupplyDao {

    void create(Supply supply);
    void update(Supply supply);
    Supply selectById(int id);
    List<Supply> selectAll();
    List<Supply> selectSuppliesForGame(int gameId);
    List<Supply> selectSuppliesForSeason(int seasonId);
    void delete(int id);
    int getKitty();
    int getKittySpent();
    void addInvoice(int supplyId, byte[] invoice);
    byte[] selectInvoice(int supplyId);
    void deleteInvoice(int supplyId);

}
