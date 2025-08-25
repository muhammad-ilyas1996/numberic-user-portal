package com.medicalbillinguserportal.commonpersistence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchDate {
    Date fromDate;
    Date toDate;
}
