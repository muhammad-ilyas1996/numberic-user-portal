package com.numbericsuserportal.federaltax.service;

import com.numbericsuserportal.federaltax.entity.FederalTaxDocument;
import com.numbericsuserportal.federaltax.entity.FederalTaxReturnCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Builds a preparation-ready export package (JSON + CSV + doc copies + README).
 * Not claimed as a native ProSeries/CrossLink import without vendor confirmation.
 */
@Service
@Slf4j
public class TaxExportPackageService {

    public String buildPackage(FederalTaxReturnCase c, List<FederalTaxDocument> docs) {
        try {
            Path root = Paths.get("uploads", "federal-tax-exports", c.getPublicCaseId());
            Files.createDirectories(root);

            Path json = root.resolve("organizer.json");
            Files.writeString(json, c.getOrganizerJson() != null ? c.getOrganizerJson() : "{}", StandardCharsets.UTF_8);

            Path extracted = root.resolve("extracted.json");
            Files.writeString(extracted, c.getExtractedJson() != null ? c.getExtractedJson() : "{}", StandardCharsets.UTF_8);

            Path validation = root.resolve("validation.json");
            Files.writeString(validation,
                    c.getValidationJson() != null ? c.getValidationJson() : "{\"ready\":true}",
                    StandardCharsets.UTF_8);

            String csv = buildCsv(c);
            Files.writeString(root.resolve("organizer_summary.csv"), csv, StandardCharsets.UTF_8);

            Path docsDir = root.resolve("supporting_documents");
            Files.createDirectories(docsDir);
            for (FederalTaxDocument d : docs) {
                if (d.getStoredPath() == null) {
                    continue;
                }
                Path src = Paths.get(d.getStoredPath());
                if (Files.exists(src)) {
                    String name = d.getOriginalFilename() != null ? d.getOriginalFilename() : src.getFileName().toString();
                    Files.copy(src, docsDir.resolve(d.getId() + "_" + name.replaceAll("[^a-zA-Z0-9._-]", "_")),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }

            String readme = """
                    Numbrics Federal Tax Export Package
                    Case: %s
                    Tax year: %s
                    Return type: %s
                    
                    Contents:
                    - organizer.json — structured organizer fields
                    - extracted.json — OCR/AI extractions
                    - validation.json — validation report
                    - organizer_summary.csv — flat summary for spreadsheet tools
                    - supporting_documents/ — original uploads
                    
                    IMPORTANT:
                    JSON/CSV/PDF are internal / fallback formats.
                    Do NOT treat this package as a confirmed native import into Intuit ProSeries
                    or CrossLink Tax until vendor import format is verified and demonstrated.
                    Existing Numbrics multi-software intake connectors (ProConnect, Drake, ProSeries file-based)
                    can consume CSV/PDF for storage; partner preparers should import into their licensed software.
                    """.formatted(c.getPublicCaseId(), c.getTaxYear(), c.getReturnType());
            Files.writeString(root.resolve("README.txt"), readme, StandardCharsets.UTF_8);

            return root.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build export package: " + e.getMessage(), e);
        }
    }

    private String buildCsv(FederalTaxReturnCase c) {
        return "field,value\n"
                + "publicCaseId," + safe(c.getPublicCaseId()) + "\n"
                + "taxYear," + c.getTaxYear() + "\n"
                + "returnType," + c.getReturnType() + "\n"
                + "clientStatus," + c.getClientStatus() + "\n"
                + "serviceOption," + c.getServiceOption() + "\n"
                + "missingItems,\"" + safe(c.getMissingItemsJson()) + "\"\n";
    }

    private String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\"", "'").replace("\n", " ");
    }
}
