package com.numbericsuserportal.kintsugi.catalog;

import com.numbericsuserportal.kintsugi.domain.SalesTaxBusinessType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BusinessTypeCatalog {

    private BusinessTypeCatalog() {
    }

    public static List<Map<String, Object>> allTypes() {
        return List.of(
                type(SalesTaxBusinessType.pet_store, "Pet Store / Grooming", "Products + services", "🐾"),
                type(SalesTaxBusinessType.salon, "Salon / Spa", "Services + retail", "💇"),
                type(SalesTaxBusinessType.restaurant, "Restaurant / Food", "Prepared food + drinks", "🍔"),
                type(SalesTaxBusinessType.contractor, "Contractor / Trades", "Labor + materials", "🔧"),
                type(SalesTaxBusinessType.retail, "Retail Store", "Physical products", "🏪"),
                type(SalesTaxBusinessType.software, "Software / SaaS", "Digital products", "💻"),
                type(SalesTaxBusinessType.healthcare, "Healthcare", "Medical + services", "🏥"),
                type(SalesTaxBusinessType.education, "Education / Training", "Classes + materials", "📚"),
                type(SalesTaxBusinessType.agriculture, "Agriculture", "Farm products + supplies", "🌾"),
                type(SalesTaxBusinessType.other, "Other / Mixed", "I'll describe it myself", "✏️")
        );
    }

    public static String displayName(SalesTaxBusinessType type) {
        return allTypes().stream()
                .filter(t -> type.name().equals(t.get("id")))
                .map(t -> String.valueOf(t.get("name")))
                .findFirst()
                .orElse(type.name());
    }

    private static Map<String, Object> type(
            SalesTaxBusinessType id, String name, String subtitle, String icon) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id.name());
        row.put("name", name);
        row.put("subtitle", subtitle);
        row.put("icon", icon);
        return row;
    }
}
