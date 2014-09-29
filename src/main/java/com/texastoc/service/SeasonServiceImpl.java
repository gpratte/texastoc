package com.texastoc.service;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.SeasonDao;
import com.texastoc.domain.QuarterlySeason;
import com.texastoc.domain.Season;
import com.texastoc.exception.InvalidDateException;
import com.texastoc.service.calculate.QuarterlySeasonCalculator;
import com.texastoc.service.calculate.SeasonCalculator;

@Service
public class SeasonServiceImpl implements SeasonService {

    @Autowired
    private SeasonDao seasonDao;
    @Autowired
    private SeasonCalculator seasonCalculator;
    @Autowired
    private QuarterlySeasonCalculator qSeasonCalculator;


    @Override
    public List<Season> findAll() {
        return seasonDao.selectAll();
    }

    @Override
    public Season findById(int id) {
        return seasonDao.selectById(id);
    }

    @Override
    public int create(Season season) throws Exception {
        checkSeasonDates(season, null);
        return seasonDao.insert(season);
    }

    @Override
    public void update(Season season) throws Exception {
        Season existingSeason = findById(season.getId());
        checkSeasonDates(season, existingSeason);
        seasonDao.update(season);
    }

    @Override
    public Season getUpToGame(int gameId) throws Exception {
        Season season = seasonCalculator.calcluateUpToGame(gameId);
        QuarterlySeason qSeason = qSeasonCalculator.calculateUpToGame(gameId);
        if (qSeason != null) {
            ArrayList<QuarterlySeason> qSeasons = new ArrayList<QuarterlySeason>();
            qSeasons.add(qSeason);
            season.setQuarterlies(qSeasons);
        }
        return season;
    }

    private void checkSeasonDates(Season newSeason, Season oldSeason) 
            throws InvalidDateException {
 
        // Check that the dates are not null
        if (newSeason.getStartDate() == null) {
            throw new InvalidDateException(null, "Season start date is missing");
        }
        if (newSeason.getEndDate() == null) {
            throw new InvalidDateException(null, "Season end date is missing");
        }
        
        List<Season> existingSeasons = findAll();
        for (Season existingSeason : existingSeasons) {
            
            if (oldSeason != null && oldSeason.getId() == existingSeason.getId()) {
                // Do not compare to itself
                continue;
            }
            
            // Check that the start and end dates are not already in another season
            LocalDate startDate = newSeason.getStartDate();
            LocalDate endDate = newSeason.getEndDate();
            LocalDate existingStartDate = existingSeason.getStartDate();
            LocalDate existingEndDate = existingSeason.getEndDate();

            if (startDate.equals(existingStartDate)) {
                throw new InvalidDateException(startDate, "Season start date already belongs to another season");
            }
            if (startDate.equals(existingEndDate)) {
                throw new InvalidDateException(startDate, "Season start date already belongs to another season");
            }
            if (endDate.equals(existingStartDate)) {
                throw new InvalidDateException(endDate, "Season end date already belongs to another season");
            }
            if (endDate.equals(existingEndDate)) {
                throw new InvalidDateException(endDate, "Season end date already belongs to another season");
            }
            if (startDate.isAfter(existingStartDate) && startDate.isBefore(existingEndDate)) {
                throw new InvalidDateException(startDate, "Season start date already belongs to another season");
            }
            if (endDate.isAfter(existingStartDate) && endDate.isBefore(existingEndDate)) {
                throw new InvalidDateException(endDate, "Season end date already belongs to another season");
            }
        }
    }

}
