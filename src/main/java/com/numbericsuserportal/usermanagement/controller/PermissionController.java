package com.numbericsuserportal.usermanagement.controller;

import com.numbericsuserportal.usermanagement.dto.PermissionDto;
import com.numbericsuserportal.usermanagement.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*")
public class PermissionController {
    
    @Autowired
    private PermissionService permissionService;
    
    @GetMapping("/portal/{portalTypeId}")
    public ResponseEntity<List<PermissionDto>> getPermissionsByPortalType(@PathVariable Long portalTypeId) {
        try {
            List<PermissionDto> permissions = permissionService.getActivePermissionsByPortalType(portalTypeId);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/portal/{portalTypeId}/category/{category}")
    public ResponseEntity<List<PermissionDto>> getPermissionsByCategoryAndPortalType(
            @PathVariable Long portalTypeId, @PathVariable String category) {
        try {
            List<PermissionDto> permissions = permissionService.getPermissionsByCategoryAndPortalType(category, portalTypeId);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/portal/{portalTypeId}/categories")
    public ResponseEntity<List<String>> getPermissionCategoriesByPortalType(@PathVariable Long portalTypeId) {
        try {
            List<String> categories = permissionService.getPermissionCategoriesByPortalType(portalTypeId);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<PermissionDto> createPermission(@RequestBody PermissionDto permissionDto) {
        try {
            PermissionDto createdPermission = permissionService.createPermission(permissionDto);
            return ResponseEntity.ok(createdPermission);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/update")
    public ResponseEntity<PermissionDto> updatePermission( @RequestBody PermissionDto permissionDto) {
        try {
            PermissionDto updatedPermission = permissionService.updatePermission(permissionDto);
            return ResponseEntity.ok(updatedPermission);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
