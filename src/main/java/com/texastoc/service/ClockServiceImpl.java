package com.texastoc.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.domain.clock.Clock;
import com.texastoc.domain.clock.Timer;

@Service
public class ClockServiceImpl implements ClockService {

    static final Logger logger = Logger.getLogger(ClockServiceImpl.class);
    
    @Autowired
    Clock clock;
    
    @Override
    public synchronized Clock getClock() {
        return clock;
    }

}
