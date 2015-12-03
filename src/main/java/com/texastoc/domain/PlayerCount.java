package com.texastoc.domain;

import java.util.HashMap;

import org.apache.commons.lang.builder.ToStringBuilder;

public class PlayerCount implements Comparable<PlayerCount> {

    private Player player;
    private int count;
    private int count2;
    private Object misc1;

    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    
    public int getCount2() {
        return count2;
    }
    public void setCount2(int count2) {
        this.count2 = count2;
    }
    public Object getMisc1() {
        return misc1;
    }
    public void setMisc1(Object misc1) {
        this.misc1 = misc1;
    }
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        
        if ( !(o instanceof PlayerCount)) {
            return false;
        }
        
        PlayerCount other = (PlayerCount)o;
        
        if (player == null || other.getPlayer() == null) {
            return false;
        }
        
        if (player.getId() == other.getPlayer().getId()) {
            return true;
        }
        
        return false;
    }

    public int hashCode() {
        if (player != null) {
            return Integer.valueOf(player.getId()).hashCode();
        }
        
        return 0;
    }
    /**
     * Sort in reverse order (most count first)
     */
    @Override
    public int compareTo(PlayerCount o) {
        if (this.count < o.count) {
            return 1;
        } else if (this.count > o.count) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
