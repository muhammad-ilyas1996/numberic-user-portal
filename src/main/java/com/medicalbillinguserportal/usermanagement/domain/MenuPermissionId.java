package com.medicalbillinguserportal.usermanagement.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class MenuPermissionId implements Serializable {
    
    private Long menuId;
    private Long permissionId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuPermissionId that = (MenuPermissionId) o;
        return Objects.equals(menuId, that.menuId) && Objects.equals(permissionId, that.permissionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(menuId, permissionId);
    }
}


