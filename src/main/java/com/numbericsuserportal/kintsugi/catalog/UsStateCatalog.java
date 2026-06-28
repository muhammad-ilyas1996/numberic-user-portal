package com.numbericsuserportal.kintsugi.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class UsStateCatalog {

    private static final Map<String, String> STATES = new LinkedHashMap<>();

    static {
        STATES.put("AL", "Alabama");
        STATES.put("AK", "Alaska");
        STATES.put("AZ", "Arizona");
        STATES.put("AR", "Arkansas");
        STATES.put("CA", "California");
        STATES.put("CO", "Colorado");
        STATES.put("CT", "Connecticut");
        STATES.put("DE", "Delaware");
        STATES.put("FL", "Florida");
        STATES.put("GA", "Georgia");
        STATES.put("HI", "Hawaii");
        STATES.put("ID", "Idaho");
        STATES.put("IL", "Illinois");
        STATES.put("IN", "Indiana");
        STATES.put("IA", "Iowa");
        STATES.put("KS", "Kansas");
        STATES.put("KY", "Kentucky");
        STATES.put("LA", "Louisiana");
        STATES.put("ME", "Maine");
        STATES.put("MD", "Maryland");
        STATES.put("MA", "Massachusetts");
        STATES.put("MI", "Michigan");
        STATES.put("MN", "Minnesota");
        STATES.put("MS", "Mississippi");
        STATES.put("MO", "Missouri");
        STATES.put("MT", "Montana");
        STATES.put("NE", "Nebraska");
        STATES.put("NV", "Nevada");
        STATES.put("NH", "New Hampshire");
        STATES.put("NJ", "New Jersey");
        STATES.put("NM", "New Mexico");
        STATES.put("NY", "New York");
        STATES.put("NC", "North Carolina");
        STATES.put("ND", "North Dakota");
        STATES.put("OH", "Ohio");
        STATES.put("OK", "Oklahoma");
        STATES.put("OR", "Oregon");
        STATES.put("PA", "Pennsylvania");
        STATES.put("RI", "Rhode Island");
        STATES.put("SC", "South Carolina");
        STATES.put("SD", "South Dakota");
        STATES.put("TN", "Tennessee");
        STATES.put("TX", "Texas");
        STATES.put("UT", "Utah");
        STATES.put("VT", "Vermont");
        STATES.put("VA", "Virginia");
        STATES.put("WA", "Washington");
        STATES.put("WV", "West Virginia");
        STATES.put("WI", "Wisconsin");
        STATES.put("WY", "Wyoming");
        STATES.put("DC", "District of Columbia");
    }

    private UsStateCatalog() {
    }

    public static List<Map<String, String>> allStates() {
        return STATES.entrySet().stream()
                .map(e -> Map.of("stateCode", e.getKey(), "stateName", e.getValue()))
                .toList();
    }

    public static Optional<String> resolveName(String stateCode) {
        if (stateCode == null || stateCode.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(STATES.get(stateCode.trim().toUpperCase()));
    }

    public static boolean isValid(String stateCode) {
        return resolveName(stateCode).isPresent();
    }
}
