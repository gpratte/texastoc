package com.texastoc.service;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import com.texastoc.domain.clock.Clock;
import com.texastoc.domain.clock.ClockImpl;
import com.texastoc.domain.clock.Level;
import com.texastoc.service.mail.MailService;

@Service
@EnableCaching 
public class ClockServiceImpl implements ClockService {

    static final Logger logger = Logger.getLogger(ClockServiceImpl.class);

    @Autowired
    private MailService mailService;
    @Autowired
    private GameService gameService;

    private HashMap<Integer,Clock> clocks = new HashMap<Integer,Clock>();
    
    @Override
    public synchronized Clock getClock(int gameId) {
    	if (clocks.containsKey(gameId)) {
    		return clocks.get(gameId);
    	} else {
    		Clock clock = new ClockImpl(gameService, mailService);
    		clocks.put(gameId, clock);
    		return clock;
    	}
    }

	@Override
	public void endClock(int gameId) {
    	if (clocks.containsKey(gameId)) {
    		Clock clock = clocks.get(gameId);
    		clock.reset();
    		clocks.remove(gameId);
    	}
	}

	@Override
    @Cacheable(value="levelcache")
	public List<Level> getLevels() {
		for (Clock clock : clocks.values()) {
			return clock.getLevels();
		}
		
		Clock clock = new ClockImpl(gameService, mailService);
		return clock.getLevels();
	}

}
