package com.texastoc.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import com.texastoc.dao.QuarterlySeasonDao;
import com.texastoc.dao.SeasonDao;
import com.texastoc.domain.QuarterlySeason;
import com.texastoc.domain.Season;
import com.texastoc.exception.InvalidDateException;
import com.texastoc.exception.InvalidQuarterException;

@Service
@EnableCaching 
public class QuarterlySeasonServiceImpl implements QuarterlySeasonService {

    @Autowired
    private QuarterlySeasonDao quarterlySeasonDao;
    @Autowired
    private SeasonDao seasonDao;

    public List<QuarterlySeason> findBySeasonId(int seasonId) {
        return quarterlySeasonDao.selectBySeasonId(seasonId);
    }

    public QuarterlySeason findById(int id) {
        return quarterlySeasonDao.selectById(id);
    }

    @CacheEvict(value="seasoncache", allEntries=true)
    public int create(QuarterlySeason quarterly) throws Exception {
        Season season = seasonDao.selectById(quarterly.getSeasonId());
        checkQuarterlySeasonDates(season, quarterly, null);
        return quarterlySeasonDao.insert(quarterly);
    }

    @CacheEvict(value="seasoncache", allEntries=true)
    public void update(QuarterlySeason quarterly) throws Exception {
        Season season = seasonDao.selectById(quarterly.getSeasonId());
        QuarterlySeason existingQuarterly = findById(quarterly.getId());
        checkQuarterlySeasonDates(season, quarterly, existingQuarterly);
        quarterlySeasonDao.update(quarterly);
    }


    private void checkQuarterlySeasonDates(Season season, 
            QuarterlySeason newQuarterly, QuarterlySeason oldQuarterly) 
                    throws InvalidDateException, InvalidQuarterException {

        // Check that the dates are not null
        if (newQuarterly.getStartDate() == null) {
            throw new InvalidDateException(null, "Season start date is missing");
        }
        if (newQuarterly.getEndDate() == null) {
            throw new InvalidDateException(null, "Season end date is missing");
        }
        
        // Make sure the quarterly season is in the bounds of the
        // containing season
        if (newQuarterly.getStartDate().isBefore(season.getStartDate())) {
            throw new InvalidDateException(newQuarterly.getStartDate(), "Quarterly season start date cannot be before the season start date");
        }
        if (newQuarterly.getEndDate().isAfter(season.getEndDate())) {
            throw new InvalidDateException(newQuarterly.getEndDate(), "Quarterly season end date cannot be after the season end date");
        }
  
        List<QuarterlySeason> existingQuarterlies = findBySeasonId(season.getId());
        for (QuarterlySeason existingQuarterly : existingQuarterlies) {
            
            if (oldQuarterly != null && oldQuarterly.getId() == existingQuarterly.getId()) {
                // Do not compare to itself
                continue;
            }
            
            // Make sure the new quarterly quarter is not already taken
            if (newQuarterly.getQuarter() == existingQuarterly.getQuarter()) {
                throw new InvalidQuarterException(newQuarterly.getQuarter(), "Quarter already defined for the season");
            }

            // Check that the start and end dates are not already in another season
            LocalDate startDate = newQuarterly.getStartDate();
            LocalDate endDate = newQuarterly.getEndDate();
            LocalDate existingStartDate = existingQuarterly.getStartDate();
            LocalDate existingEndDate = existingQuarterly.getEndDate();

            if (startDate.equals(existingStartDate)) {
                throw new InvalidDateException(startDate, "Quarterly season start date already belongs to another quarterly season");
            }
            if (startDate.equals(existingEndDate)) {
                throw new InvalidDateException(startDate, "Quarterly season start date already belongs to another quarterly season");
            }
            if (endDate.equals(existingStartDate)) {
                throw new InvalidDateException(endDate, "Quarterly season end date already belongs to another quarterly season");
            }
            if (endDate.equals(existingEndDate)) {
                throw new InvalidDateException(endDate, "Quarterly season end date already belongs to another quarterly season");
            }
            if (startDate.isAfter(existingStartDate) && startDate.isBefore(existingEndDate)) {
                throw new InvalidDateException(startDate, "Quarterly season start date already belongs to another quarterly season");
            }
            if (endDate.isAfter(existingStartDate) && endDate.isBefore(existingEndDate)) {
                throw new InvalidDateException(endDate, "Quarterly season end date already belongs to another quarterly season");
            }
        }
    }

}
