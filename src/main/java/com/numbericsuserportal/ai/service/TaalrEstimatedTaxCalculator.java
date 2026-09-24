package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.dto.TaalrEstimatedTaxDraft;
import com.numbericsuserportal.ai.i18n.TaalrEstimatedTaxMessages;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Simplified federal + self-employment estimated-tax math for Phase 1 coach.
 * Transparent assumptions; always paired with IRS citations in the reply.
 */
public final class TaalrEstimatedTaxCalculator {

    private TaalrEstimatedTaxCalculator() {
    }

    public static final class EstimateResult {
        public final int taxYear;
        public final double netProfit;
        public final double seTax;
        public final double federalIncomeTax;
        public final double totalLiability;
        public final double amountPaid;
        public final double remaining;
        public final LocalDate nextDueDate;
        public final String nextPaymentLabel;
        /** Suggested amount for the next quarterly installment (remaining ÷ quarters left). */
        public final double suggestedNextPayment;
        public final List<String> assumptions = new ArrayList<>();

        EstimateResult(int taxYear, double netProfit, double seTax, double federalIncomeTax,
                double totalLiability, double amountPaid, double remaining,
                LocalDate nextDueDate, String nextPaymentLabel, double suggestedNextPayment) {
            this.taxYear = taxYear;
            this.netProfit = netProfit;
            this.seTax = seTax;
            this.federalIncomeTax = federalIncomeTax;
            this.totalLiability = totalLiability;
            this.amountPaid = amountPaid;
            this.remaining = remaining;
            this.nextDueDate = nextDueDate;
            this.nextPaymentLabel = nextPaymentLabel;
            this.suggestedNextPayment = suggestedNextPayment;
        }
    }

    public static EstimateResult compute(TaalrEstimatedTaxDraft draft) {
        int year = draft.getTaxYear() != null ? draft.getTaxYear() : LocalDate.now().getYear();
        double income = nz(draft.getYtdIncome());
        double expenses = nz(draft.getDeductibleExpenses());
        double paidEst = nz(draft.getPriorPayments());
        double withheld = nz(draft.getWithholding());

        double netProfit = Math.max(0, income - expenses);
        // SE tax: 15.3% on 92.35% of net earnings (Social Security + Medicare), simplified
        double seBase = netProfit * 0.9235;
        double seTax = round2(seBase * 0.153);
        double seDeduction = round2(seTax / 2.0);

        double taxableIncome = Math.max(0, netProfit - seDeduction - standardDeduction(draft.getFilingStatus(), year));
        double federalIncomeTax = round2(progressiveFederalTax(taxableIncome, draft.getFilingStatus(), year));
        double totalLiability = round2(federalIncomeTax + seTax);
        double amountPaid = round2(paidEst + withheld);
        double remaining = round2(Math.max(0, totalLiability - amountPaid));

        LocalDate nextDue = nextQuarterlyDue(LocalDate.now(), year);
        String quarterLabel = quarterLabel(nextDue);
        int quartersLeft = quartersRemaining(quarterLabel);
        double suggestedNext = round2(remaining / Math.max(1, quartersLeft));

        EstimateResult r = new EstimateResult(year, round2(netProfit), seTax, federalIncomeTax,
                totalLiability, amountPaid, remaining, nextDue, quarterLabel, suggestedNext);
        r.assumptions.add("Net profit = YTD income − deductible expenses ($"
                + money(r.netProfit) + ").");
        r.assumptions.add("Self-employment tax ≈ 15.3% × 92.35% of net earnings (simplified; Social Security wage base not applied).");
        r.assumptions.add("Federal income tax uses simplified " + year + " brackets after ½ SE tax + standard deduction.");
        r.assumptions.add("State income tax is NOT included in this Phase 1 federal estimate.");
        r.assumptions.add("Amount paid = estimated payments + federal withholding.");
        r.assumptions.add("Suggested next payment ≈ remaining liability ÷ " + quartersLeft + " remaining quarter(s).");
        return r;
    }

    public static String formatReply(TaalrEstimatedTaxDraft draft, EstimateResult r) {
        String lang = TaalrEstimatedTaxMessages.normalizeLang(draft.getLanguage());
        String body = switch (lang) {
            case "ES" -> formatEs(draft, r);
            case "HT" -> formatHt(draft, r);
            default -> formatEn(draft, r);
        };
        return body + TaalrEstimatedTaxCitations.footer(lang, r.taxYear);
    }

    private static String formatEn(TaalrEstimatedTaxDraft draft, EstimateResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("TAALR empowered by Numbrics — Estimated tax summary (").append(r.taxYear).append(")\n\n");
        sb.append("Filing status: ").append(labelStatus(draft.getFilingStatus())).append('\n');
        sb.append("State: ").append(draft.getStateCode()).append('\n');
        sb.append("YTD net profit (est.): $").append(money(r.netProfit)).append("\n\n");
        sb.append("Estimated federal income tax: $").append(money(r.federalIncomeTax)).append('\n');
        sb.append("Estimated self-employment tax: $").append(money(r.seTax)).append('\n');
        sb.append("Total estimated liability: $").append(money(r.totalLiability)).append('\n');
        sb.append("Amount paid so far (estimates + withholding): $").append(money(r.amountPaid)).append('\n');
        sb.append("Remaining (if liability > paid): $").append(money(r.remaining)).append('\n');
        sb.append("Suggested next estimated payment: $").append(money(r.suggestedNextPayment))
                .append(" (").append(r.nextPaymentLabel).append(")\n");
        sb.append("Next due date: ").append(r.nextDueDate).append("\n\n");
        sb.append("Assumptions:\n");
        for (String a : r.assumptions) {
            sb.append("• ").append(a).append('\n');
        }
        appendMissingNotes(sb, draft, "EN");
        return sb.toString().trim();
    }

    private static String formatEs(TaalrEstimatedTaxDraft draft, EstimateResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("TAALR empowered by Numbrics — Resumen de impuestos estimados (").append(r.taxYear).append(")\n\n");
        sb.append("Estado civil: ").append(labelStatus(draft.getFilingStatus())).append('\n');
        sb.append("Estado: ").append(draft.getStateCode()).append('\n');
        sb.append("Beneficio neto YTD (est.): $").append(money(r.netProfit)).append("\n\n");
        sb.append("Impuesto federal sobre la renta (est.): $").append(money(r.federalIncomeTax)).append('\n');
        sb.append("Impuesto sobre el trabajo por cuenta propia (est.): $").append(money(r.seTax)).append('\n');
        sb.append("Responsabilidad total estimada: $").append(money(r.totalLiability)).append('\n');
        sb.append("Monto pagado (estimados + retención): $").append(money(r.amountPaid)).append('\n');
        sb.append("Restante: $").append(money(r.remaining)).append('\n');
        sb.append("Próximo pago estimado sugerido: $").append(money(r.suggestedNextPayment))
                .append(" (").append(r.nextPaymentLabel).append(")\n");
        sb.append("Fecha de vencimiento: ").append(r.nextDueDate).append("\n\n");
        sb.append("Supuestos:\n");
        for (String a : r.assumptions) {
            sb.append("• ").append(a).append('\n');
        }
        appendMissingNotes(sb, draft, "ES");
        return sb.toString().trim();
    }

    private static String formatHt(TaalrEstimatedTaxDraft draft, EstimateResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("TAALR empowered by Numbrics — Rezime enpo estime (").append(r.taxYear).append(")\n\n");
        sb.append("Estati depo: ").append(labelStatus(draft.getFilingStatus())).append('\n');
        sb.append("Eta: ").append(draft.getStateCode()).append('\n');
        sb.append("Pwofi nèt YTD (est.): $").append(money(r.netProfit)).append("\n\n");
        sb.append("Enpo federal sou revni (est.): $").append(money(r.federalIncomeTax)).append('\n');
        sb.append("Enpo travay pou kont pwòp (est.): $").append(money(r.seTax)).append('\n');
        sb.append("Total responsabilite estime: $").append(money(r.totalLiability)).append('\n');
        sb.append("Montan peye (estime + retni): $").append(money(r.amountPaid)).append('\n');
        sb.append("Rès: $").append(money(r.remaining)).append('\n');
        sb.append("Pwochen peman estime sijere: $").append(money(r.suggestedNextPayment))
                .append(" (").append(r.nextPaymentLabel).append(")\n");
        sb.append("Dat limit: ").append(r.nextDueDate).append("\n\n");
        sb.append("Sipozisyon:\n");
        for (String a : r.assumptions) {
            sb.append("• ").append(a).append('\n');
        }
        appendMissingNotes(sb, draft, "HT");
        return sb.toString().trim();
    }

    private static void appendMissingNotes(StringBuilder sb, TaalrEstimatedTaxDraft draft, String lang) {
        List<String> missing = missingNotes(draft, lang);
        if (missing.isEmpty()) {
            return;
        }
        sb.append(switch (lang) {
            case "ES" -> "\nNotas sobre entradas:\n";
            case "HT" -> "\nNòt sou antre:\n";
            default -> "\nNotes on inputs:\n";
        });
        for (String m : missing) {
            sb.append("• ").append(m).append('\n');
        }
    }

    private static List<String> missingNotes(TaalrEstimatedTaxDraft draft, String lang) {
        List<String> notes = new ArrayList<>();
        if (nz(draft.getDeductibleExpenses()) == 0) {
            notes.add(switch (lang) {
                case "ES" -> "Gastos deducibles ingresados como $0.";
                case "HT" -> "Depans dediktib antre kòm $0.";
                default -> "Deductible expenses entered as $0.";
            });
        }
        if (nz(draft.getPriorPayments()) == 0) {
            notes.add(switch (lang) {
                case "ES" -> "No se ingresaron pagos estimados previos.";
                case "HT" -> "Pa gen peman estime anvan yo antre.";
                default -> "No prior estimated payments entered.";
            });
        }
        if (nz(draft.getWithholding()) == 0) {
            notes.add(switch (lang) {
                case "ES" -> "No se ingresó retención federal.";
                case "HT" -> "Pa gen retni federal antre.";
                default -> "No federal withholding entered.";
            });
        }
        return notes;
    }

    /** Approximate TY2024/2025 standard deduction (simplified). */
    private static double standardDeduction(String filingStatus, int year) {
        String s = filingStatus == null ? "SINGLE" : filingStatus.toUpperCase(Locale.ROOT);
        // Rough 2025 levels for Phase 1 demo
        double single = year >= 2025 ? 15000 : 14600;
        double mfj = year >= 2025 ? 30000 : 29200;
        double hoh = year >= 2025 ? 22500 : 21900;
        return switch (s) {
            case "MFJ", "QW" -> mfj;
            case "HOH" -> hoh;
            default -> single; // SINGLE, MFS
        };
    }

    private static double progressiveFederalTax(double taxable, String filingStatus, int year) {
        // Simplified 2025 Single brackets (approx); MFJ brackets roughly 2x widths
        boolean joint = filingStatus != null
                && (filingStatus.equalsIgnoreCase("MFJ") || filingStatus.equalsIgnoreCase("QW"));
        double[][] brackets = joint
                ? new double[][]{{0, 0.10}, {23850, 0.12}, {96950, 0.22}, {206700, 0.24}, {394600, 0.32}, {501050, 0.35}, {751600, 0.37}}
                : new double[][]{{0, 0.10}, {11925, 0.12}, {48475, 0.22}, {103350, 0.24}, {197300, 0.32}, {250525, 0.35}, {626350, 0.37}};
        if (year < 2025) {
            // Slightly lower 2024-ish
            brackets = joint
                    ? new double[][]{{0, 0.10}, {23200, 0.12}, {94300, 0.22}, {201050, 0.24}, {383900, 0.32}, {487450, 0.35}, {731200, 0.37}}
                    : new double[][]{{0, 0.10}, {11600, 0.12}, {47150, 0.22}, {100525, 0.24}, {191950, 0.32}, {243725, 0.35}, {609350, 0.37}};
        }
        double tax = 0;
        for (int i = 0; i < brackets.length; i++) {
            double floor = brackets[i][0];
            double rate = brackets[i][1];
            double ceiling = (i + 1 < brackets.length) ? brackets[i + 1][0] : Double.MAX_VALUE;
            if (taxable <= floor) {
                break;
            }
            double slice = Math.min(taxable, ceiling) - floor;
            tax += slice * rate;
        }
        return tax;
    }

    public static LocalDate nextQuarterlyDue(LocalDate today, int taxYear) {
        // Typical Form 1040-ES due dates for tax year T:
        // Apr 15 T, Jun 15 T, Sep 15 T, Jan 15 T+1
        MonthDay[] dues = {
                MonthDay.of(4, 15),
                MonthDay.of(6, 15),
                MonthDay.of(9, 15),
                MonthDay.of(1, 15)
        };
        for (int i = 0; i < 3; i++) {
            LocalDate d = dues[i].atYear(taxYear);
            if (!today.isAfter(d)) {
                return d;
            }
        }
        return MonthDay.of(1, 15).atYear(taxYear + 1);
    }

    private static String quarterLabel(LocalDate due) {
        int m = due.getMonthValue();
        if (m == 4) {
            return "Q1";
        }
        if (m == 6) {
            return "Q2";
        }
        if (m == 9) {
            return "Q3";
        }
        return "Q4";
    }

    private static int quartersRemaining(String quarterLabel) {
        return switch (quarterLabel) {
            case "Q1" -> 4;
            case "Q2" -> 3;
            case "Q3" -> 2;
            default -> 1;
        };
    }

    private static String labelStatus(String status) {
        if (status == null) {
            return "SINGLE";
        }
        return status.toUpperCase(Locale.ROOT);
    }

    private static double nz(Double v) {
        return v == null ? 0.0 : v;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String money(double v) {
        return String.format(Locale.US, "%,.2f", v);
    }
}
