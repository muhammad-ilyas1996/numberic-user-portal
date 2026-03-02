package com.numbericsuserportal.invoice.impl;

import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.invoice.repo.InvoiceAndTaxRepo;
import com.numbericsuserportal.invoice.service.InvoicePdfService;
import com.numbericsuserportal.invoiceproduct.entity.InvoiceProductEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 18;
    private static final float FONT_SIZE_HEADING = 12;
    private static final float FONT_SIZE_NORMAL = 10;
    private static final float LINE_HEIGHT = 14;

    @Autowired
    private InvoiceAndTaxRepo invoiceAndTaxRepo;

    @Override
    public byte[] generatePdf(Long invoiceId) {
        Optional<InvoiceAndTaxEntity> opt = invoiceAndTaxRepo.findByIdAndIsActiveTrue(invoiceId);
        if (opt.isEmpty()) {
            return null;
        }
        InvoiceAndTaxEntity inv = opt.get();
        try {
            return buildPdf(inv);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage(), e);
        }
    }

    private byte[] buildPdf(InvoiceAndTaxEntity inv) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float y = page.getMediaBox().getHeight() - MARGIN;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                PDType1Font fontTitle = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                // Title
                cs.beginText();
                cs.setFont(fontTitle, FONT_SIZE_TITLE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("INVOICE");
                cs.endText();
                y -= LINE_HEIGHT * 1.5f;

                // Invoice #
                String invNum = inv.getInvoiceNum() != null ? inv.getInvoiceNum() : ("#" + inv.getId());
                y = writeLine(cs, "Invoice: " + invNum, fontBold, FONT_SIZE_HEADING, MARGIN, y);
                y -= LINE_HEIGHT * 0.5f;

                DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
                if (inv.getInvoiceIssueDate() != null) {
                    y = writeLine(cs, "Issue Date: " + inv.getInvoiceIssueDate().format(df), fontNormal, FONT_SIZE_NORMAL, MARGIN, y);
                }
                if (inv.getInvoiceDueDate() != null) {
                    y = writeLine(cs, "Due Date: " + inv.getInvoiceDueDate().format(df), fontNormal, FONT_SIZE_NORMAL, MARGIN, y);
                }
                y = writeLine(cs, "Status: " + (inv.getInvoiceStatus() != null ? inv.getInvoiceStatus() : "-"), fontNormal, FONT_SIZE_NORMAL, MARGIN, y);
                y -= LINE_HEIGHT;

                // Bill To
                cs.beginText();
                cs.setFont(fontBold, FONT_SIZE_HEADING);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Bill To");
                cs.endText();
                y -= LINE_HEIGHT;
                if (inv.getCustomerName() != null) y = writeLine(cs, inv.getCustomerName(), fontNormal, FONT_SIZE_NORMAL, MARGIN, y);
                if (inv.getCustomerEmail() != null) y = writeLine(cs, inv.getCustomerEmail(), fontNormal, FONT_SIZE_NORMAL, MARGIN, y);
                String addr = formatAddress(inv.getCustomerStreet(), inv.getCustomerCity(), inv.getCustomerState(), inv.getCustomerPostalCode(), inv.getCustomerCountry());
                if (!addr.isEmpty()) y = writeLine(cs, addr, fontNormal, FONT_SIZE_NORMAL, MARGIN, y);
                y -= LINE_HEIGHT;

                // Description
                if (inv.getDescription() != null && !inv.getDescription().isEmpty()) {
                    y = writeLine(cs, "Description: " + inv.getDescription(), fontNormal, FONT_SIZE_NORMAL, MARGIN, y);
                    y -= LINE_HEIGHT * 0.5f;
                }

                // Table header
                y -= LINE_HEIGHT * 0.5f;
                cs.setFont(fontBold, FONT_SIZE_NORMAL);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Item");
                cs.newLineAtOffset(220, 0);
                cs.showText("Qty");
                cs.newLineAtOffset(50, 0);
                cs.showText("Amount");
                cs.endText();
                y -= LINE_HEIGHT;
                drawLine(cs, MARGIN, y, pageWidth - MARGIN, y);
                y -= LINE_HEIGHT;

                // Line items
                List<InvoiceProductEntity> items = inv.getInvoiceProductEntity();
                if (items != null) {
                    for (InvoiceProductEntity item : items) {
                        if (y < MARGIN + 80) break;
                        String name = item.getProductName() != null ? item.getProductName() : "-";
                        if (name.length() > 35) name = name.substring(0, 32) + "...";
                        cs.setFont(fontNormal, FONT_SIZE_NORMAL);
                        cs.beginText();
                        cs.newLineAtOffset(MARGIN, y);
                        cs.showText(name);
                        cs.newLineAtOffset(220, 0);
                        cs.showText(item.getQuantity() != null ? String.format("%.2f", item.getQuantity()) : "-");
                        cs.newLineAtOffset(50, 0);
                        cs.showText(item.getAmount() != null ? String.format("%.2f", item.getAmount()) : "-");
                        cs.endText();
                        y -= LINE_HEIGHT;
                    }
                }
                y -= LINE_HEIGHT * 0.5f;
                drawLine(cs, MARGIN, y, pageWidth - MARGIN, y);
                y -= LINE_HEIGHT;

                // Total
                Double total = inv.getTotalTaxAmountCalculated() != null ? inv.getTotalTaxAmountCalculated() : inv.getTaxableAmount();
                String totalStr = "Total: " + (total != null ? String.format("%.2f", total) : "0.00");
                if (inv.getCurrency() != null) totalStr += " " + inv.getCurrency();
                cs.setFont(fontBold, FONT_SIZE_HEADING);
                cs.beginText();
                cs.newLineAtOffset(pageWidth - MARGIN - 120, y);
                cs.showText(totalStr);
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private float writeLine(PDPageContentStream cs, String text, PDType1Font font, float size, float x, float y) throws Exception {
        if (text == null) return y;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(text));
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private void drawLine(PDPageContentStream cs, float x1, float y1, float x2, float y2) throws Exception {
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    private String formatAddress(String street, String city, String state, String postal, String country) {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.isEmpty()) sb.append(street);
        if (city != null && !city.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(city);
        if (state != null && !state.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(state);
        if (postal != null && !postal.isEmpty()) sb.append(sb.length() > 0 ? " " : "").append(postal);
        if (country != null && !country.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(country);
        return sb.toString();
    }

    private String sanitize(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\u0000-\\u001F]", "");
    }
}
