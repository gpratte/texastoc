package com.texastoc.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

public class SeasonPlayer extends AbstractSeasonPlayer implements Comparable<SeasonPlayer> {

    private int seasonId;

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    /**
     * Sort in reverse order (most points first)
     */
    @Override
    public int compareTo(SeasonPlayer o) {
        return super.compareTo(o);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
