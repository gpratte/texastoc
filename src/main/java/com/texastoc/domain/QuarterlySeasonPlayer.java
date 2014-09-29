package com.texastoc.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

public class QuarterlySeasonPlayer extends AbstractSeasonPlayer implements Comparable<QuarterlySeasonPlayer> {

    private int quarterlySeasonId;

    public int getQuarterlySeasonId() {
        return quarterlySeasonId;
    }

    public void setQuarterlySeasonId(int quarterlySeasonId) {
        this.quarterlySeasonId = quarterlySeasonId;
    }

    /**
     * Sort in reverse order (most points first)
     */
    @Override
    public int compareTo(QuarterlySeasonPlayer o) {
        return super.compareTo(o);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
