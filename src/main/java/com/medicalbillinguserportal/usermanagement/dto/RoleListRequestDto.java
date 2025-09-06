package com.medicalbillinguserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleListRequestDto {
    private Long roleId;
    private Integer pageNumber;
    private Integer pageSize;
    private String fromDate;
    private String toDate;
}
