package com.medicalbillinguserportal.usermanagement.controller;

import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.Role;
import com.medicalbillinguserportal.usermanagement.dto.*;
import com.medicalbillinguserportal.usermanagement.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/user-management/roles")
@CrossOrigin(origins = "*")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    // Create new role
    @PostMapping("/create")
    public ResponseEntity<RoleDetailDto> createRole(@RequestBody RoleCreateDto roleCreateDto) {
        try {
            RoleDetailDto createdRole = roleService.createRole(roleCreateDto);
            return ResponseEntity.ok(createdRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
    
    // Update existing role
    @PostMapping("/update")
    public ResponseEntity<RoleDetailDto> updateRole(@RequestBody RoleUpdateDto roleUpdateDto) {
        try {
            roleUpdateDto.setRoleId(roleUpdateDto.getRoleId());
            RoleDetailDto updatedRole = roleService.updateRole(roleUpdateDto.getRoleId(), roleUpdateDto);
            return ResponseEntity.ok(updatedRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
    
    // List all roles
    @PostMapping("/list")
    public ResponseEntity<Page<RoleListDto>> listRoles(@RequestBody RoleListRequestDto requestDto) {
        try {
            Page<RoleListDto> roles = roleService.listRoles(requestDto);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // Get role detail
    @GetMapping("/{roleId}")
    public ResponseEntity<RoleDetailDto> getRoleDetail(@PathVariable Long roleId) {
        try {
            RoleDetailDto role = roleService.getRoleDetail(roleId);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
