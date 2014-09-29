package com.texastoc.domain.clock;

public class Timer extends Thread {
    private long duration;
    private long staticElapsed;
    private long runningElapsed;
    private Clock clock;
    private boolean stopped = false;
    private boolean paused = false;
    private TimerListener listener;
    
    public Timer(long duration, long elapsed, TimerListener listener) {
        this.duration = duration;
        this.staticElapsed = elapsed;
        this.listener = listener;
    }

    public long getElapsed() {
        return staticElapsed + runningElapsed;
    }
    
    public void run() {
        long start = System.currentTimeMillis();
        while((staticElapsed + runningElapsed) < duration && !stopped) {
            try {
                Thread.sleep(900L);
            } catch(Exception e) {
                // do nothing
            }
            
            if (!stopped && !paused) {
                long now = System.currentTimeMillis();
                runningElapsed = now - start;
            }
        }
        if (!stopped) {
            listener.finished();
        }
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    public void end() {
        stopped = true;
    }

}
