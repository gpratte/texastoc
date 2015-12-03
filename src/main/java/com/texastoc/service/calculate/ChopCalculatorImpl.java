package com.texastoc.service.calculate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ChopCalculatorImpl implements ChopCalculator {
    
    /**
     * Calculates the chop
     * 
     * @param amounts the amounts for the normal points/payouts. Must
     * be in order such that the first entry in the list is 1st place, 
     * second entry is 2nd place, etc..
     * @return Never returns null. Will be empty of no chop can be 
     * calculated
     */
    @Override
    public List<Chop> calculate(List<Integer> chips, List<Integer> amounts) {
        if (chips == null || chips.size() < 2) {
            return Collections.emptyList();
        }
        if (amounts == null || amounts.size() < 2) {
            return Collections.emptyList();
        }
        if (chips.size() != amounts.size()) {
            return Collections.emptyList();
        }
        for (Integer chip : chips) {
            if (chip == null) {
                return Collections.emptyList();
            }
        }
        for (Integer amount : amounts) {
            if (amount == null) {
                return Collections.emptyList();
            }
        }
        
        List<Chop> chops = new ArrayList<Chop>();
        
        int smallestAmount = amounts.get(amounts.size() -1);
        int combinedAmount = 0;
        int combinedChips = 0;
        
        for (int i = 0; i < chips.size(); ++i) {
            combinedChips += chips.get(i);
            combinedAmount += amounts.get(i);
        }
        
        // Calculate percentage
        for (int i = 0; i < chips.size(); ++i) {
            float percent = chips.get(i) / (float) combinedChips;
            Chop chop = new Chop();
            chop.setOrgAmount(amounts.get(i));
            chop.setPercent(percent);
            chops.add(chop);
        }

        // For all that chopped give them the smallest and then their
        // percent of the remaining
        int amountToChop = combinedAmount - (smallestAmount * chips.size());
        for (int i = 0; i < chips.size(); ++i) {
            Chop chop = chops.get(i);
            int choppedAmount = (int)(amountToChop * chop.getPercent());
            chop.setChopAmount(choppedAmount + smallestAmount);
        }
        
        recalculate(chips, amounts, chops, true);
        
        return chops;
    }

    // Make sure full amount has been allocated
    private void recalculate(List<Integer> chips, 
            List<Integer> amounts, List<Chop> chops, 
            boolean change1stPlace) {
        
        int amountAllocated = 0;
        int totalAmount = 0;
        
        for (int i = 0; i < amounts.size(); ++i) {
            totalAmount += amounts.get(i);
            amountAllocated += chops.get(i).getChopAmount();
        }
        
        if (totalAmount != amountAllocated) {
            int leftOver = totalAmount - amountAllocated;
            int increment = 1;
            if (amountAllocated > totalAmount) {
                leftOver = amountAllocated - totalAmount;
                increment = -1;
            }
            
            while (leftOver > 0) {
                for (int i = 0; i < chops.size(); ++i) {
                    if (!change1stPlace && i == 0) {
                        continue;
                    }
                    Chop chop = chops.get(i);
                    chop.setChopAmount(chop.getChopAmount() + increment);
                    if (--leftOver == 0) {
                        break;
                    }
                }
            }
        }
        
        // Check if first place good too much
        if (chops.get(0).getChopAmount() > amounts.get(0)) {
            int overage = chops.get(0).getChopAmount() - amounts.get(0);
            chops.get(0).setChopAmount(amounts.get(0));

            for (int i = 1; i < chops.size(); ++i) {
                Chop chop = chops.get(i);
                int extra = (int)(overage * chop.getPercent());
                chop.setChopAmount(chop.getChopAmount() + extra);
            }

            recalculate(chips, amounts, chops, false);
        }
    }

}
