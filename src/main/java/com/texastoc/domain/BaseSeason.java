package com.texastoc.domain;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.validation.Errors;

import com.texastoc.util.DateConverter;

public abstract class BaseSeason {

    private int id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
    private DateTime lastCalculated;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getStartDateText() {
        if (startDate != null) {
            return DateConverter.getDateAsString(startDate);
        } else {
            return null;
        }
    }

    public String getStartDateYearText() {
        if (startDate != null) {
            String text = DateConverter.getDateAsString(startDate);
            return text.substring(text.length() - 4);
        } else {
            return null;
        }
    }

    public void setStartDateText(String text) {
        if (StringUtils.isNotEmpty(text)) {
            this.startDate = DateConverter.getStringAsDate(text);
        } else {
            this.startDate = null;
        }
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getEndDateText() {
        if (endDate != null) {
            return DateConverter.getDateAsString(endDate);
        } else {
            return null;
        }
    }

    public String getEndDateYearText() {
        if (endDate != null) {
            String text = DateConverter.getDateAsString(endDate);
            return text.substring(text.length() - 4);
        } else {
            return null;
        }
    }

    public void setEndDateText(String text) {
        if (StringUtils.isNotEmpty(text)) {
            this.endDate = DateConverter.getStringAsDate(text);
        } else {
            this.endDate = null;
        }
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }

    public DateTime getLastCalculated() {
        return lastCalculated;
    }
    public void setLastCalculated(DateTime lastCalculated) {
        this.lastCalculated = lastCalculated;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    protected void validate(Object obj, Errors errors) {

        BaseSeason season = (BaseSeason) obj;
        if (season.getStartDate() == null) {
            errors.rejectValue("startDate", "emptyDate",
                    "Start date must be set");
            return;
        }
        if (season.getEndDate() == null) {
            errors.rejectValue("endDate", "emptyDate",
                    "End date must be set");
            return;
        }
        if (season.getStartDate().isAfter(season.getEndDate())) {
            errors.rejectValue("startDate", "startBeforeEnd",
                    "Start date must be before end date");
        } else if (season.getStartDate().equals(season.getEndDate())) {
            errors.rejectValue("startDate", "startBeforeEnd",
                    "Start date must be before end date");
        }
    }
}
