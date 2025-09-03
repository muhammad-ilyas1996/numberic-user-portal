package com.medicalbillinguserportal.usermanagement.service;

import com.medicalbillinguserportal.usermanagement.domain.Menu;
import com.medicalbillinguserportal.usermanagement.domain.PortalType;
import com.medicalbillinguserportal.usermanagement.dto.MenuDto;
import com.medicalbillinguserportal.usermanagement.repo.MenuRepository;
import com.medicalbillinguserportal.usermanagement.repo.PortalTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MenuService {
    
    @Autowired
    private MenuRepository menuRepository;
    
    @Autowired
    private PortalTypeRepository portalTypeRepository;
    
    public List<MenuDto> getActiveMenusByPortalType(Long portalTypeId) {
        PortalType portalType = portalTypeRepository.findById(portalTypeId)
            .orElseThrow(() -> new RuntimeException("Portal type not found"));
            
        List<Menu> menus = menuRepository.findByPortalTypeAndIsActiveTrueOrderByMenuOrderAsc(portalType);
        return menus.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<MenuDto> getHierarchicalMenusByPortalType(Long portalTypeId) {
        PortalType portalType = portalTypeRepository.findById(portalTypeId)
            .orElseThrow(() -> new RuntimeException("Portal type not found"));
            
        List<Menu> parentMenus = menuRepository.findByPortalTypeAndParentMenuIdIsNullAndIsActiveTrueOrderByMenuOrderAsc(portalType);
        return parentMenus.stream()
                .map(this::buildHierarchicalMenu)
                .collect(Collectors.toList());
    }
    
    public MenuDto createMenu(MenuDto menuDto, Long createdBy) {
        Menu menu = convertToEntity(menuDto);
        // BaseEntity automatically manages these fields
        // No need to set them manually
        
        Menu savedMenu = menuRepository.save(menu);
        return convertToDto(savedMenu);
    }
    
    public MenuDto updateMenu(Long menuId, MenuDto menuDto, Long updatedBy) {
        Optional<Menu> existingMenu = menuRepository.findById(menuId);
        if (existingMenu.isPresent()) {
            Menu menu = existingMenu.get();
            updateMenuFromDto(menu, menuDto);
            // BaseEntity automatically manages these fields
            // No need to set them manually
            
            Menu savedMenu = menuRepository.save(menu);
            return convertToDto(savedMenu);
        }
        throw new RuntimeException("Menu not found with id: " + menuId);
    }
    
    public void deleteMenu(Long menuId) {
        Optional<Menu> menu = menuRepository.findById(menuId);
        if (menu.isPresent()) {
            Menu existingMenu = menu.get();
            existingMenu.setIsActive(false);
            // BaseEntity automatically manages these fields
            // No need to set them manually
            menuRepository.save(existingMenu);
        }
    }
    
    private MenuDto buildHierarchicalMenu(Menu parentMenu) {
        MenuDto parentDto = convertToDto(parentMenu);
        List<Menu> subMenus = menuRepository.findByParentMenuIdAndIsActiveTrueOrderByMenuOrderAsc(parentMenu.getMenuId());
        List<MenuDto> subMenuDtos = subMenus.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        parentDto.setSubMenus(subMenuDtos);
        return parentDto;
    }
    
    private MenuDto convertToDto(Menu menu) {
        MenuDto dto = new MenuDto();
        dto.setMenuId(menu.getMenuId());
        dto.setMenuName(menu.getMenuName());
        dto.setDisplayName(menu.getDisplayName());
        dto.setMenuUrl(menu.getMenuUrl());
        dto.setParentMenuId(menu.getParentMenuId());
        dto.setMenuOrder(menu.getMenuOrder());
        dto.setIconClass(menu.getIconClass());
        dto.setIsActive(menu.getIsActive());
        dto.setPortalTypeId(menu.getPortalType().getPortalTypeId());
        dto.setPortalTypeName(menu.getPortalType().getPortalName());
        // BaseEntity fields are automatically managed
        // No need to set them in DTO
        return dto;
    }
    
    private Menu convertToEntity(MenuDto dto) {
        Menu menu = new Menu();
        menu.setMenuName(dto.getMenuName());
        menu.setDisplayName(dto.getDisplayName());
        menu.setMenuUrl(dto.getMenuUrl());
        menu.setParentMenuId(dto.getParentMenuId());
        menu.setMenuOrder(dto.getMenuOrder());
        menu.setIconClass(dto.getIconClass());
        menu.setIsActive(dto.getIsActive());
        
        // Set portal type from ID
        if (dto.getPortalTypeId() != null) {
            PortalType portalType = portalTypeRepository.findById(dto.getPortalTypeId())
                .orElseThrow(() -> new RuntimeException("Portal type not found"));
            menu.setPortalType(portalType);
        }
        
        return menu;
    }
    
    private void updateMenuFromDto(Menu menu, MenuDto dto) {
        if (dto.getDisplayName() != null) menu.setDisplayName(dto.getDisplayName());
        if (dto.getMenuUrl() != null) menu.setMenuUrl(dto.getMenuUrl());
        if (dto.getMenuOrder() != null) menu.setMenuOrder(dto.getMenuOrder());
        if (dto.getIconClass() != null) menu.setIconClass(dto.getIconClass());
        if (dto.getIsActive() != null) menu.setIsActive(dto.getIsActive());
    }

    public MenuDto getMenuById(Long menuId) {
        Optional<Menu> menu = menuRepository.findById(menuId);
        if (menu.isPresent()) {
            return convertToDto(menu.get());
        }
        return null;
    }
}
