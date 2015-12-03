package com.texastoc.service.calculate;

import java.util.List;


public interface ChopCalculator {

    List<Chop> calculate(List<Integer> chips, List<Integer> amounts);
}
