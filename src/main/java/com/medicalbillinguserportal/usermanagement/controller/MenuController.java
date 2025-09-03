package com.medicalbillinguserportal.usermanagement.controller;


import com.medicalbillinguserportal.usermanagement.dto.MenuDto;
import com.medicalbillinguserportal.usermanagement.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/menus")
@CrossOrigin(origins = "*")
public class MenuController {

        @Autowired
        private MenuService menuService;

        @GetMapping("/portal/{portalTypeId}")
        public ResponseEntity<List<MenuDto>> getMenusByPortalType(@PathVariable Long portalTypeId) {
            try {
                List<MenuDto> menus = menuService.getActiveMenusByPortalType(portalTypeId);
                return ResponseEntity.ok(menus);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }

        @GetMapping("/portal/{portalTypeId}/hierarchy")
        public ResponseEntity<List<MenuDto>> getHierarchicalMenus(@PathVariable Long portalTypeId) {
            try {
                List<MenuDto> menus = menuService.getHierarchicalMenusByPortalType(portalTypeId);
                return ResponseEntity.ok(menus);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }

        @GetMapping("/{menuId}")
        public ResponseEntity<MenuDto> getMenuById(@PathVariable Long menuId) {
            try {
                MenuDto menu = menuService.getMenuById(menuId);
                if (menu != null) {
                    return ResponseEntity.ok(menu);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }

        @PostMapping
        public ResponseEntity<MenuDto> createMenu(@RequestBody MenuDto menuDto) {
            try {
                MenuDto createdMenu = menuService.createMenu(menuDto, 1L); // Use actual user ID
                return ResponseEntity.ok(createdMenu);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }
    }

