package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.numbericsuserportal.LlcNorthwest.dto.FilingMethodDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Picks LLC formation filing products/methods from Corporate Tools catalog responses.
 * Avoids ancillary filings (good standing, amendments, annual reports, etc.).
 */
@Component
public class NorthwestFilingProductSelector {

  private static final List<String> EXCLUDED_PRODUCT_TERMS = List.of(
      "good standing",
      "certificate of good",
      "assumed name",
      "amendment",
      "annual report",
      "reinstat",
      "dissolution",
      "withdrawal",
      "apostille",
      "certified copy",
      "trade name",
      "dba",
      "name reservation",
      "name change",
      "foreign qualification",
      "change of agent",
      "registered agent change"
  );

  private static final List<String> FORMATION_PRODUCT_TERMS = List.of(
      "articles of organization",
      "form a company",
      "company formation",
      "business formation",
      "llc formation",
      "limited liability company formation",
      "new company",
      "initial filing",
      "organize",
      "formation package",
      "form an llc",
      "start a company"
  );

  private static final List<String> EXCLUDED_METHOD_TERMS = List.of(
      "good standing",
      "assumed name",
      "amendment",
      "annual report",
      "reinstat",
      "dissolution",
      "withdrawal",
      "certified copy",
      "name reservation"
  );

  @Value("${llc.northwest.filing-product-name:}")
  private String configuredProductName;

  public Optional<FilingProductDTO> pickFormationProduct(List<FilingProductDTO> products) {
    if (products == null || products.isEmpty()) {
      return Optional.empty();
    }

    if (configuredProductName != null && !configuredProductName.isBlank()) {
      String needle = configuredProductName.trim().toLowerCase(Locale.ROOT);
      Optional<FilingProductDTO> configured = products.stream()
          .filter(p -> !isExcludedProduct(p))
          .filter(p -> productLabel(p).contains(needle))
          .findFirst();
      if (configured.isPresent()) {
        return configured;
      }
    }

    return products.stream()
        .filter(p -> !isExcludedProduct(p))
        .filter(p -> formationProductScore(p) > 0)
        .max(Comparator.comparingInt(this::formationProductScore));
  }

  public List<FilingMethodDTO> filterFormationMethods(List<FilingMethodDTO> methods) {
    if (methods == null || methods.isEmpty()) {
      return List.of();
    }
    List<FilingMethodDTO> filtered = methods.stream()
        .filter(m -> !isExcludedMethod(m))
        .toList();
    return filtered.isEmpty() ? methods : filtered;
  }

  public boolean isExcludedProduct(FilingProductDTO product) {
    String label = productLabel(product);
    return EXCLUDED_PRODUCT_TERMS.stream().anyMatch(label::contains);
  }

  public boolean isFormationProduct(FilingProductDTO product) {
    return !isExcludedProduct(product) && formationProductScore(product) > 0;
  }

  private boolean isExcludedMethod(FilingMethodDTO method) {
    String label = methodLabel(method);
    return EXCLUDED_METHOD_TERMS.stream().anyMatch(label::contains);
  }

  private int formationProductScore(FilingProductDTO product) {
    String label = productLabel(product);
    int score = 0;
    for (String term : FORMATION_PRODUCT_TERMS) {
      if (label.contains(term)) {
        score += 10;
      }
    }
    if (label.contains("formation")) {
      score += 5;
    }
    if (label.contains("articles")) {
      score += 8;
    }
    if (label.contains("organiz")) {
      score += 6;
    }
    return score;
  }

  private static String productLabel(FilingProductDTO product) {
    StringBuilder sb = new StringBuilder();
    if (product.getName() != null) {
      sb.append(product.getName()).append(' ');
    }
    if (product.getFilingName() != null) {
      sb.append(product.getFilingName());
    }
    return sb.toString().trim().toLowerCase(Locale.ROOT);
  }

  private static String methodLabel(FilingMethodDTO method) {
    StringBuilder sb = new StringBuilder();
    if (method.getName() != null) {
      sb.append(method.getName()).append(' ');
    }
    if (method.getFilingDescription() != null) {
      sb.append(method.getFilingDescription());
    }
    return sb.toString().trim().toLowerCase(Locale.ROOT);
  }
}
