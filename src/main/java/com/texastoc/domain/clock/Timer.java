package com.texastoc.domain.clock;

public class Timer extends Thread {
    private long duration;
    private long elapsed = 0;
    private boolean stopped = false;
    private boolean paused = false;
    private TimerListener listener;
    
    public Timer(long duration, TimerListener listener) {
        this.duration = duration;
        this.listener = listener;
    }

    public long getDuration() {
        return duration;
    }

    public long getRemaining() {
        return duration - elapsed;
    }
    
    public void setRemaining(long remaining) {
        elapsed = duration - remaining;
    }
    
    public void run() {
        while((elapsed < duration || paused) && !stopped) {
            long start = System.currentTimeMillis();
            try {
                Thread.sleep(900L);
            } catch(Exception e) {
                // do nothing
            }
            
            if (!stopped && !paused) {
                long now = System.currentTimeMillis();
                elapsed += now - start;
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
