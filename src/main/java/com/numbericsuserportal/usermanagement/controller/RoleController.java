package com.numbericsuserportal.usermanagement.controller;

import com.numbericsuserportal.usermanagement.dto.*;
import com.numbericsuserportal.usermanagement.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user-management/roles")
@CrossOrigin(origins = "*")
public class RoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    
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
    public ResponseEntity<?> listRoles(@RequestBody RoleListRequestDto requestDto) {
        try {
            Page<RoleListDto> roles = roleService.listRoles(requestDto);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            logger.error("Error retrieving roles", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "Failed to retrieve roles"
            ));
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
