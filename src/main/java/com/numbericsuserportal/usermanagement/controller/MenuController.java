package com.numbericsuserportal.usermanagement.controller;


import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.dto.MenuDto;
import com.numbericsuserportal.usermanagement.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/menus")
@CrossOrigin(origins = "*")
public class MenuController {

        @Autowired
        private MenuService menuService;

//        @GetMapping("/portal/{portalTypeId}")
//        public ResponseEntity<List<MenuDto>> getMenusByPortalType(@PathVariable Long portalTypeId) {
//            try {
//                List<MenuDto> menus = menuService.getActiveMenusByPortalType(portalTypeId);
//                return ResponseEntity.ok(menus);
//            } catch (Exception e) {
//                return ResponseEntity.badRequest().build();
//            }
//        }

//        @GetMapping("/portal/{portalTypeId}/hierarchy")
//        public ResponseEntity<List<MenuDto>> getHierarchicalMenus(@PathVariable Long portalTypeId) {
//            try {
//                List<MenuDto> menus = menuService.getHierarchicalMenusByPortalType(portalTypeId);
//                return ResponseEntity.ok(menus);
//            } catch (Exception e) {
//                return ResponseEntity.badRequest().build();
//            }
//        }

//        @GetMapping("/{menuId}")
//        public ResponseEntity<MenuDto> getMenuById(@PathVariable Long menuId) {
//            try {
//                MenuDto menu = menuService.getMenuById(menuId);
//                if (menu != null) {
//                    return ResponseEntity.ok(menu);
//                } else {
//                    return ResponseEntity.notFound().build();
//                }
//            } catch (Exception e) {
//                return ResponseEntity.badRequest().build();
//            }
//        }

//        @PostMapping
//        public ResponseEntity<MenuDto> createMenu(@RequestBody MenuDto menuDto) {
//            try {
//                MenuDto createdMenu = menuService.createMenu(menuDto, 1L); // Use actual user ID
//                return ResponseEntity.ok(createdMenu);
//            } catch (Exception e) {
//                return ResponseEntity.badRequest().build();
//            }
//        }


    @GetMapping("/user")
    public ResponseEntity<List<MenuDto>> getMenusByUser(
            @AuthenticationPrincipal User currentUser) {
        try {
            List<MenuDto> menus = menuService.getMenusByUser(currentUser.getUserId());
            return ResponseEntity.ok(menus);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/user/hierarchy")
    public ResponseEntity<List<MenuDto>> getHierarchicalMenusByUser(
            @AuthenticationPrincipal User currentUser) {
        try {
            List<MenuDto> menus = menuService.getHierarchicalMenusByUser(currentUser.getUserId());
            return ResponseEntity.ok(menus);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

