package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.numbericsuserportal.LlcNorthwest.dto.FilingMethodDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NorthwestFilingProductSelectorTest {

  private final NorthwestFilingProductSelector selector = new NorthwestFilingProductSelector();

  @Test
  void pickFormationProduct_prefersArticlesOverGoodStanding() {
    FilingProductDTO goodStanding = product(UUID.fromString("9ba1ec77-db86-4a8d-938a-c394b9bda213"),
        "Certificate of Good Standing", "certificate of good standing");
    FilingProductDTO formation = product(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"),
        "Articles of Organization", "articles of organization");

    var picked = selector.pickFormationProduct(List.of(goodStanding, formation));

    assertTrue(picked.isPresent());
    assertEquals(formation.getId(), picked.get().getId());
  }

  @Test
  void filterFormationMethods_excludesGoodStandingDescriptions() {
    FilingMethodDTO goodStanding = method(UUID.fromString("73cd805f-0330-46fc-8e31-a502cffc2719"),
        "Standard", "Certificate of Good Standing");
    FilingMethodDTO formation = method(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
        "Standard", "Articles of Organization");

    List<FilingMethodDTO> filtered = selector.filterFormationMethods(List.of(goodStanding, formation));

    assertEquals(1, filtered.size());
    assertEquals(formation.getId(), filtered.get(0).getId());
  }

  private static FilingProductDTO product(UUID id, String name, String filingName) {
    FilingProductDTO dto = new FilingProductDTO();
    dto.setId(id);
    dto.setName(name);
    dto.setFilingName(filingName);
    return dto;
  }

  private static FilingMethodDTO method(UUID id, String name, String description) {
    FilingMethodDTO dto = new FilingMethodDTO();
    dto.setId(id);
    dto.setName(name);
    dto.setFilingDescription(description);
    return dto;
  }
}
