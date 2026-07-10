package com.numbericsuserportal.ai.action.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaalrInvoiceDraft {

    private Long invoiceId;
    private String invoiceNum;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    /** Sum of line items (auto-calculated). */
    private Double amount;
    /** Legacy single description; prefer lineItems. */
    private String description;
    private Double quantity = 1.0;
    private List<TaalrInvoiceLineItem> lineItems = new ArrayList<>();
    /** Transient: collecting current line item fields. */
    private String pendingLineName;
    private Double pendingLineQty;
    private Boolean askingAddAnotherLine;
    /** True while collecting the next line item after user said YES to add another. */
    private Boolean collectingNextLine;
    /** WHATSAPP or EMAIL */
    private String channel;
    private String recipientPhoneOrEmail;
    /** Days until due; default 30. */
    private Integer dueDays;
}
