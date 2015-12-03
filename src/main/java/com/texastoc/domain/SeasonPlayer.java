package com.texastoc.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

public class SeasonPlayer extends AbstractSeasonPlayer implements Comparable<SeasonPlayer> {

    private int seasonId;
    private boolean forfeit;
    private boolean wsop;

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public boolean isForfeit() {
		return forfeit;
	}

	public void setForfeit(boolean forfeit) {
		this.forfeit = forfeit;
	}

    public boolean isWsop() {
		return wsop;
	}

	public void setWsop(boolean wsop) {
		this.wsop = wsop;
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
