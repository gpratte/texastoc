package com.texastoc.domain;

public class Seat {

    private int table;
    private int position;
    private Player player;
    
    public int getTable() {
        return table;
    }
    public void setTable(int table) {
        this.table = table;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    
}
