package com.texastoc.service.calculate;

import java.util.HashMap;


public interface PayoutCalculator {

    void calculate(int id) throws Exception;
    
    HashMap<Integer, HashMap<Integer,Float>> getPayouts();
}
