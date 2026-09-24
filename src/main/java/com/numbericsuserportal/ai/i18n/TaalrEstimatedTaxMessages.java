package com.numbericsuserportal.ai.i18n;

/**
 * EN / ES / Haitian Creole (HT) copy for the estimated-tax coach.
 */
public final class TaalrEstimatedTaxMessages {

    private TaalrEstimatedTaxMessages() {
    }

    public static String normalizeLang(String language) {
        if (language == null || language.isBlank()) {
            return "EN";
        }
        String u = language.trim().toUpperCase();
        if (u.startsWith("ES") || u.equals("SPANISH") || u.equals("ESPAÑOL") || u.equals("ESPANOL")) {
            return "ES";
        }
        if (u.startsWith("HT") || u.contains("CREOLE") || u.contains("KREYOL") || u.equals("HAITIAN")) {
            return "HT";
        }
        return "EN";
    }

    public static String askLanguage() {
        return """
                TAALR empowered by Numbrics — Estimated tax coach
                
                Which language? / ¿Qué idioma? / Ki lang?
                Reply: English / Español / Kreyòl""";
    }

    public static String ask(String language, String field) {
        String lang = normalizeLang(language);
        return switch (field) {
            case "filingStatus" -> switch (lang) {
                case "ES" -> "¿Cuál es su estado civil para efectos fiscales?\n"
                        + "Responda: Soltero, Casado en conjunto (MFJ), Casado por separado (MFS), Cabeza de familia (HOH)";
                case "HT" -> "Ki estati depo ou?\n"
                        + "Reponn: Selibatè, Marye ansanm (MFJ), Marye separe (MFS), Chèf fanmi (HOH)";
                default -> "What is your filing status?\n"
                        + "Reply: Single, Married filing jointly (MFJ), Married filing separately (MFS), or Head of household (HOH)";
            };
            case "stateCode" -> switch (lang) {
                case "ES" -> "¿En qué estado de EE. UU. presenta (código de 2 letras, p. ej. TX, CA, FL)?";
                case "HT" -> "Nan ki eta Etazini w ap depoze (kòd 2 lèt, egzanp TX, CA, FL)?";
                default -> "Which U.S. state do you file in? (2-letter code, e.g. TX, CA, FL)";
            };
            case "taxYear" -> switch (lang) {
                case "ES" -> "¿Para qué año fiscal es esta estimación? (p. ej. 2025)";
                case "HT" -> "Pou ki ane taks estime sa a? (egzanp 2025)";
                default -> "Which tax year is this estimate for? (e.g. 2025)";
            };
            case "ytdIncome" -> switch (lang) {
                case "ES" -> "¿Cuál es su ingreso bruto del año hasta la fecha (autónomo/negocio)? Sólo el número, p. ej. 45000";
                case "HT" -> "Ki revni brit ane-a-jodi a (travay pou kont pwòp/biznis)? Nimewo sèlman, egzanp 45000";
                default -> "What is your year-to-date gross income (self-employment/business)? Number only, e.g. 45000";
            };
            case "deductibleExpenses" -> switch (lang) {
                case "ES" -> "¿Cuáles son sus gastos deducibles del año hasta la fecha? (0 si ninguno)";
                case "HT" -> "Ki depans dediktib ane-a-jodi a? (0 si pa genyen)";
                default -> "What are your year-to-date deductible business expenses? (0 if none)";
            };
            case "priorPayments" -> switch (lang) {
                case "ES" -> "¿Cuánto ha pagado ya en impuestos estimados este año? (0 si ninguno)";
                case "HT" -> "Konbyen enpo estime ou deja peye ane sa a? (0 si pa genyen)";
                default -> "How much have you already paid in estimated taxes this year? (0 if none)";
            };
            case "withholding" -> switch (lang) {
                case "ES" -> "¿Cuánta retención federal de impuestos sobre la renta lleva este año (W-2 u otra)? (0 si ninguna)";
                case "HT" -> "Konbyen retni federal sou revni ou genyen ane sa a (W-2 oswa lòt)? (0 si pa genyen)";
                default -> "How much federal income tax has been withheld year-to-date (W-2 or other)? (0 if none)";
            };
            default -> ask(language, "filingStatus");
        };
    }

    public static String invalid(String language, String field) {
        String lang = normalizeLang(language);
        return switch (lang) {
            case "ES" -> "No entendí eso. " + ask(language, field);
            case "HT" -> "Mwen pa konprann sa. " + ask(language, field);
            default -> "I didn't catch that. " + ask(language, field);
        };
    }

    public static String cancelled(String language) {
        return switch (normalizeLang(language)) {
            case "ES" -> "Estimación fiscal cancelada. Escriba \"estimated tax\" o \"impuestos estimados\" para empezar de nuevo.";
            case "HT" -> "Estime enpo anile. Ekri \"estimated tax\" oswa \"enpo estime\" pou rekòmanse.";
            default -> "Estimated tax coach cancelled. Say \"estimated tax\" to start again.";
        };
    }

    public static String intro(String language) {
        return switch (normalizeLang(language)) {
            case "ES" -> "Perfecto. Soy el coach de impuestos estimados de TAALR empowered by Numbrics. "
                    + "Recopilaré algunos datos y le daré una estimación federal + SE (no es una declaración presentada).\n\n";
            case "HT" -> "OK. Mwen se antrenè enpo estime TAALR empowered by Numbrics. "
                    + "M ap kolekte kèk enfòmasyon epi m ap ba ou yon estime federal + SE (se pa yon deklarasyon depoze).\n\n";
            default -> "Got it. I'm the TAALR empowered by Numbrics estimated-tax coach. "
                    + "I'll collect a few inputs and return a federal + self-employment estimate "
                    + "(not a filed return).\n\n";
        };
    }
}
