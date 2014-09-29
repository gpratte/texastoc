package com.texastoc.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.PointSystemDao;
import com.texastoc.domain.PointSystem;
import com.texastoc.domain.TopTenPoints;

@Service
public class PointSystemServiceImpl implements PointSystemService {

    @Autowired
    PointSystemDao pointSystemDao;

    private PointSystem pointSystem;

    @Override
    public List<TopTenPoints> findTopTenPoints(int min, int max) {
        if (min < 2) {
            throw new IllegalArgumentException(
                    "Must have at least 2 players to play calculate points for a tournament");
        }

        List<TopTenPoints> pointsList = new ArrayList<TopTenPoints>();
        for (int totalPlayers = min; totalPlayers <= max; ++totalPlayers) {
            TopTenPoints ttp = calculateTopTenPoints(totalPlayers);
            pointsList.add(ttp);
        }
        return pointsList;
    }

    public TopTenPoints getTopTenPoints(int totalPlayers) {
        return this.calculateTopTenPoints(totalPlayers);
    }

    private TopTenPoints calculateTopTenPoints(int totalPlayers) {

        double value = pointSystem.getTenthPlacePoints();

        for (int i = 2; i < totalPlayers; ++i) {
            value += pointSystem.getTenthPlaceIncr();
        }

        TopTenPoints ttp = new TopTenPoints();
        ttp.setNumPlayers(totalPlayers);

        int players = Math.min(totalPlayers, 10);
        if (players >= 10) {
            ttp.setPointsForPlace(10, Long.valueOf(Math.round(value))
                    .intValue());
        } else {
            ttp.setPointsForPlace(10, 0);
        }
        for (int i = 9; i > 0; --i) {
            value *= pointSystem.getMultiplier();
            if (players >= i) {
                ttp.setPointsForPlace(i, Long.valueOf(Math.round(value))
                        .intValue());
            } else {
                ttp.setPointsForPlace(i, 0);
            }
        }
        
        return ttp;
    }

    @PostConstruct
    public void init() {
        pointSystem = pointSystemDao.selectPointSystem();
    }
}
