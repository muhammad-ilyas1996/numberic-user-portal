package com.numbericsuserportal.recieptupload.dto;

import lombok.Data;

@Data
public class ReceiptSearch {
    
    private Integer pageNumber;
    private Integer pageSize;
    private String fromDate;
    private String toDate;
}
