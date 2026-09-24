package com.numbericsuserportal.ai.action.dto;

import lombok.Data;

/**
 * Phase 1 estimated-tax coach draft (federal + self-employment estimate).
 * Language: EN | ES | HT (Haitian Creole).
 */
@Data
public class TaalrEstimatedTaxDraft {

    /** EN, ES, or HT */
    private String language;
    /** SINGLE, MFJ, MFS, HOH, QW */
    private String filingStatus;
    /** US state code, e.g. TX */
    private String stateCode;
    /** Tax year for the estimate (e.g. 2025) */
    private Integer taxYear;
    /** Year-to-date gross income (self-employment / business) */
    private Double ytdIncome;
    /** Year-to-date deductible business expenses */
    private Double deductibleExpenses;
    /** Estimated tax payments already made this year */
    private Double priorPayments;
    /** Federal income tax withheld YTD (W-2 etc.) */
    private Double withholding;
}
