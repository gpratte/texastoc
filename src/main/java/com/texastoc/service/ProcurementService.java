package com.texastoc.service;

import java.util.List;

import com.texastoc.domain.Supply;

public interface ProcurementService {

    void procure(Supply supply);
    void update(Supply supply);
    Supply getSupply(int id);
    List<Supply> getAllSupplies();
    List<Supply> getSuppliesForGame(int gameId);
    List<Supply> getSuppliesForSeason(int seasonId);
    void delete(int id);
    int getKitty();
    void addInvoice(int supplyId, byte[] invoice);
    byte[] getInvoice(int supplyId);
    void removeInvoice(int supplyId);
}
