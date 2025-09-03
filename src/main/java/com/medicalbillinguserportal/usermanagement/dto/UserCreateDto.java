package com.medicalbillinguserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {
    
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String npi;
    private String licenseNumber;
    private Boolean isPracticeAdmin;
    private Long locationId;
    private Long taxonomyId;
    
    // Role assignment
    private Long roleId;
    
    // Additional permissions (optional)
    private List<Long> additionalPermissionIds;
}


