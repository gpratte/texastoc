package com.texastoc.service;

import java.util.List;

import com.texastoc.domain.TopTenPoints;

public interface PointSystemService {
	
    List<TopTenPoints> findTopTenPoints(int min, int max);
    TopTenPoints getTopTenPoints(int numPlayers);

}
