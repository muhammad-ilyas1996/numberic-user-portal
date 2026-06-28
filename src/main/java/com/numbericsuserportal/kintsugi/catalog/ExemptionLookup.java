package com.numbericsuserportal.kintsugi.catalog;

import com.numbericsuserportal.kintsugi.domain.SalesTaxBusinessType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static business_type + state → default taxable / exempt category labels.
 * State-specific overrides fall back to business-type defaults.
 */
public final class ExemptionLookup {

    public record ExemptionProfile(List<String> taxableCategories, List<String> exemptCategories) {
    }

    private static final Map<String, ExemptionProfile> LOOKUP = buildLookup();

    private ExemptionLookup() {
    }

    public static ExemptionProfile resolve(SalesTaxBusinessType businessType, String stateCode) {
        String code = stateCode != null ? stateCode.trim().toUpperCase() : "";
        ExemptionProfile stateSpecific = LOOKUP.get(key(businessType, code));
        if (stateSpecific != null) {
            return stateSpecific;
        }
        ExemptionProfile defaults = LOOKUP.get(businessType.name());
        if (defaults != null) {
            return defaults;
        }
        return new ExemptionProfile(
                List.of("Retail merchandise", "Product sales"),
                List.of("Professional services", "Labor"));
    }

    private static String key(SalesTaxBusinessType type, String stateCode) {
        return type.name() + ":" + stateCode;
    }

    private static Map<String, ExemptionProfile> buildLookup() {
        Map<String, ExemptionProfile> map = new HashMap<>();

        map.put(SalesTaxBusinessType.pet_store.name(), profile(
                List.of("Pet food & treats", "Toys & accessories", "Collars & leashes", "Retail merchandise", "Flea & tick products"),
                List.of("Grooming services", "Boarding services", "Training sessions", "Doggy daycare", "Veterinary care")));

        map.put(key(SalesTaxBusinessType.pet_store, "TX"), profile(
                List.of("Pet food & treats", "Toys & accessories", "Collars & leashes", "Retail merchandise", "Flea & tick products"),
                List.of("Grooming services", "Boarding services", "Training sessions", "Doggy daycare")));

        map.put(key(SalesTaxBusinessType.pet_store, "CA"), profile(
                List.of("Pet food & treats", "Toys & accessories", "Retail merchandise"),
                List.of("Grooming services", "Boarding services", "Training sessions", "Veterinary care")));

        map.put(SalesTaxBusinessType.salon.name(), profile(
                List.of("Retail products", "Hair care products", "Cosmetics"),
                List.of("Haircuts", "Color services", "Spa treatments", "Nail services")));

        map.put(SalesTaxBusinessType.restaurant.name(), profile(
                List.of("Prepared food", "Beverages", "Catering"),
                List.of("Tips", "Delivery fees (if separately stated)")));

        map.put(SalesTaxBusinessType.contractor.name(), profile(
                List.of("Materials", "Equipment rental", "Supplies"),
                List.of("Labor", "Installation services", "Repair services")));

        map.put(SalesTaxBusinessType.retail.name(), profile(
                List.of("Physical products", "Merchandise", "Accessories"),
                List.of("Gift cards (until redeemed)")));

        map.put(SalesTaxBusinessType.software.name(), profile(
                List.of("SaaS subscriptions", "Digital downloads", "Licensed software"),
                List.of("Implementation services", "Training", "Support contracts")));

        map.put(SalesTaxBusinessType.healthcare.name(), profile(
                List.of("Medical supplies", "Over-the-counter products"),
                List.of("Professional medical services", "Prescription drugs", "Insurance copays")));

        map.put(SalesTaxBusinessType.education.name(), profile(
                List.of("Books", "Materials", "Lab supplies"),
                List.of("Tuition", "Classes", "Workshops")));

        map.put(SalesTaxBusinessType.agriculture.name(), profile(
                List.of("Equipment", "Feed", "Seeds", "Fertilizer"),
                List.of("Farm labor", "Harvesting services")));

        map.put(SalesTaxBusinessType.other.name(), profile(
                List.of("Taxable sales"),
                List.of("Exempt sales")));

        // Top states for salon + retail
        map.put(key(SalesTaxBusinessType.salon, "TX"), profile(
                List.of("Retail products", "Hair care products"),
                List.of("Haircuts", "Color services", "Spa treatments", "Nail services")));

        map.put(key(SalesTaxBusinessType.retail, "FL"), profile(
                List.of("Physical products", "Merchandise"),
                List.of("Shipping (if separately stated)")));

        return Map.copyOf(map);
    }

    private static ExemptionProfile profile(List<String> taxable, List<String> exempt) {
        return new ExemptionProfile(List.copyOf(taxable), List.copyOf(exempt));
    }
}
