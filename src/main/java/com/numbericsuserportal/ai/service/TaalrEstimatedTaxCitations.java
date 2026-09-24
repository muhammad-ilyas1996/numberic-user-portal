package com.numbericsuserportal.ai.service;

/**
 * Official sources appended to estimated-tax coach answers (tax-year aware).
 */
public final class TaalrEstimatedTaxCitations {

    private TaalrEstimatedTaxCitations() {
    }

    public static String footer(String language, int taxYear) {
        String irsEs = "https://www.irs.gov/payments/estimated-taxes";
        String pub505 = "https://www.irs.gov/publications/p505";
        String form1040es = "https://www.irs.gov/forms-pubs/about-form-1040-es";
        String se = "https://www.irs.gov/businesses/small-businesses-self-employed/self-employment-tax-social-security-and-medicare-taxes";

        return switch (normalize(language)) {
            case "ES" -> """
                    
                    Fuentes (%d):
                    • Impuestos estimados IRS: %s
                    • Pub. 505: %s
                    • Formulario 1040-ES: %s
                    • Impuesto sobre el trabajo por cuenta propia: %s
                    
                    Esto es una estimación educativa de TAALR empowered by Numbrics — no es una declaración presentada ni asesoría fiscal personalizada.
                    """.formatted(taxYear, irsEs, pub505, form1040es, se);
            case "HT" -> """
                    
                    Sous (%d):
                    • Enpo estime IRS: %s
                    • Pub. 505: %s
                    • Fòm 1040-ES: %s
                    • Enpo travay pou kont pwòp: %s
                    
                    Sa a se yon estime edikatif TAALR empowered by Numbrics — se pa yon deklarasyon depoze ni konsèy taks pèsonalize.
                    """.formatted(taxYear, irsEs, pub505, form1040es, se);
            default -> """
                    
                    Sources (tax year %d):
                    • IRS Estimated Taxes: %s
                    • IRS Publication 505: %s
                    • Form 1040-ES: %s
                    • Self-employment tax: %s
                    
                    This is an educational estimate from TAALR empowered by Numbrics — not a filed return or personalized tax advice.
                    """.formatted(taxYear, irsEs, pub505, form1040es, se);
        };
    }

    private static String normalize(String language) {
        if (language == null || language.isBlank()) {
            return "EN";
        }
        return language.trim().toUpperCase();
    }
}
