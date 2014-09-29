package com.texastoc.service.integration;

import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

public class GameChangedExecutor implements Executor {

    static final Logger logger = Logger.getLogger(GameChangedExecutor.class);

    public void execute(Runnable r) {
        new Thread(r).start();
    }
}
