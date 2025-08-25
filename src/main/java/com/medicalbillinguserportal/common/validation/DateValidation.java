package com.medicalbillinguserportal.common.validation;



import com.medicalbillinguserportal.common.exceptions.InvalidDateFormatException;
import com.medicalbillinguserportal.common.exceptions.StartDateMustBeGreaterException;
import com.medicalbillinguserportal.common.utils.DateUtils;
import com.medicalbillinguserportal.commonpersistence.dto.SearchDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DateValidation {

    @Value("${default.date.format}")
    String defaultDateFormat;

    public SearchDate validateDates(String fromDateStr, String toDateStr) {
        Date fromDate = null, toDate = null;
        if (fromDateStr != null && toDateStr != null) {
            fromDate = DateUtils.toDate(fromDateStr, defaultDateFormat);
            toDate = DateUtils.toDate(toDateStr, defaultDateFormat);
            if (fromDate == null || toDate == null) {
                throw new InvalidDateFormatException("Invalid date format provided");
            } else if (fromDate.after(toDate)) {
                throw new StartDateMustBeGreaterException("The start date must not be later than the end date ");
            } else {
                // do nothing
            }
        }
        SearchDate searchDate = new SearchDate(fromDate, toDate);
        return searchDate;
    }

}
