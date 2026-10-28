package com.numbericsuserportal.federaltax.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.federaltax.domain.ClientTaxStatus;
import com.numbericsuserportal.federaltax.domain.FederalTaxReturnType;
import com.numbericsuserportal.federaltax.domain.TaxServiceOption;
import com.numbericsuserportal.federaltax.dto.*;
import com.numbericsuserportal.federaltax.entity.*;
import com.numbericsuserportal.federaltax.repo.*;
import com.numbericsuserportal.twilio.service.DataExtractionService;
import com.numbericsuserportal.twilio.service.DocumentClassifierService;
import com.numbericsuserportal.twilio.service.FileHandlerService;
import com.numbericsuserportal.twilio.service.OCRService;
import com.numbericsuserportal.usermanagement.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Phase 1 Federal Income Tax intake → organizer → assign / export → 8879.
 * Reuses existing OCR stack. Does NOT implement MeF e-file (Phase 2).
 */
@Service
@Slf4j
public class FederalTaxReturnService {

    private static final List<ClientTaxStatus> OPEN_PREPARER_STATUSES = List.of(
            ClientTaxStatus.WITH_PREPARER, ClientTaxStatus.IN_REVIEW, ClientTaxStatus.ACTION_REQUIRED);

    @Autowired
    private FederalTaxReturnCaseRepository caseRepository;
    @Autowired
    private FederalTaxDocumentRepository documentRepository;
    @Autowired
    private TaxPreparerProfileRepository preparerRepository;
    @Autowired
    private Form8879AuthorizationRepository form8879Repository;
    @Autowired
    private TaxCaseAuditLogRepository auditLogRepository;
    @Autowired
    private OCRService ocrService;
    @Autowired
    private FileHandlerService fileHandlerService;
    @Autowired
    private DocumentClassifierService documentClassifierService;
    @Autowired
    private DataExtractionService dataExtractionService;
    @Autowired
    private TaxExportPackageService exportPackageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TaxModuleMenuDto> taxModuleMenu() {
        return List.of(
                new TaxModuleMenuDto("FEDERAL_INCOME_TAX", "Federal Income Tax", "PHASE_1", true,
                        "Form 1040 + Schedule C intake, organizer, assign/export, 8879"),
                new TaxModuleMenuDto("STATE_INCOME_TAX", "State Income Tax", "FUTURE", false, "Future expansion"),
                new TaxModuleMenuDto("SALES_TAX", "Sales Tax", "EXISTING", true, "Existing Kintsugi integration"),
                new TaxModuleMenuDto("TAX_PLANNING", "Tax Planning", "PHASE_1_PARTIAL", true,
                        "Estimated-tax coach (TAALR) available; full planning later"),
                new TaxModuleMenuDto("TAX_DOCUMENTS", "Tax Documents", "PHASE_1", true,
                        "Documents attached to federal tax cases"),
                new TaxModuleMenuDto("TAX_FILING_STATUS", "Tax Filing Status", "PHASE_1", true,
                        "Client-visible case statuses")
        );
    }

    @Transactional
    public FederalTaxCaseDto createCase(User user, CreateFederalTaxCaseRequest req) {
        requireUser(user);
        FederalTaxReturnCase c = new FederalTaxReturnCase();
        c.setPublicCaseId("FTX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        c.setTaxpayerUserId(user.getUserId());
        c.setReturnType(req.getReturnType() != null ? req.getReturnType() : FederalTaxReturnType.FORM_1040);
        c.setTaxYear(req.getTaxYear() != null ? req.getTaxYear() : LocalDate.now().getYear());
        c.setTaxpayerConsent(Boolean.TRUE.equals(req.getTaxpayerConsent()));
        c.setClientStatus(ClientTaxStatus.INTAKE);
        c.setOrganizerJson(writeJson(emptyOrganizer(c.getReturnType())));
        c.setMissingItemsJson(writeJson(defaultMissing(c.getReturnType())));
        c = caseRepository.save(c);
        audit(c.getId(), user.getUserId(), "TAXPAYER", "CASE_CREATED",
                "Return type=" + c.getReturnType() + " year=" + c.getTaxYear());
        return toClientDto(c, true);
    }

    public List<FederalTaxCaseDto> listMyCases(User user) {
        requireUser(user);
        return caseRepository.findByTaxpayerUserIdOrderByUpdatedAtDesc(user.getUserId())
                .stream().map(c -> toClientDto(c, false)).toList();
    }

    public FederalTaxCaseDto getCaseForTaxpayer(User user, String publicCaseId) {
        FederalTaxReturnCase c = requireTaxpayerCase(user, publicCaseId);
        return toClientDto(c, true);
    }

    @Transactional
    public FederalTaxCaseDto uploadDocument(User user, String publicCaseId, MultipartFile file, String via) {
        FederalTaxReturnCase c = requireTaxpayerCase(user, publicCaseId);
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("Max file size is 10MB");
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        Path dir = Paths.get("uploads", "federal-tax", String.valueOf(user.getUserId()), c.getPublicCaseId());
        try {
            Files.createDirectories(dir);
            Path dest = dir.resolve(System.currentTimeMillis() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_"));
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            FederalTaxDocument doc = new FederalTaxDocument();
            doc.setCaseId(c.getId());
            doc.setOriginalFilename(original);
            doc.setStoredPath(dest.toAbsolutePath().toString());
            doc.setContentType(file.getContentType());
            doc.setUploadedVia(via != null ? via : "WEB");

            Map<String, Object> extracted = new LinkedHashMap<>();
            String ocrText = null;
            String docType = "UNKNOWN";
            try (InputStream in = Files.newInputStream(dest)) {
                String detected = fileHandlerService.detectFileType(file.getContentType(), original);
                List<BufferedImage> pages;
                if ("PDF".equalsIgnoreCase(detected)) {
                    pages = fileHandlerService.convertPdfToImages(in, 300);
                } else if ("UNKNOWN".equalsIgnoreCase(detected)) {
                    throw new RuntimeException("Unsupported file type. Only PDF, JPG, JPEG, PNG are supported.");
                } else {
                    BufferedImage img = fileHandlerService.loadImage(in);
                    pages = img != null ? List.of(fileHandlerService.preprocessImage(img)) : List.of();
                }
                StringBuilder sb = new StringBuilder();
                for (BufferedImage page : pages) {
                    String t = ocrService.extractText(page);
                    if (t != null) {
                        sb.append(t).append("\n");
                    }
                }
                ocrText = sb.toString();
                docType = documentClassifierService.classifyDocument(ocrText);
                Object data = null;
                String upper = docType != null ? docType.toUpperCase(Locale.ROOT) : "";
                if (upper.contains("W2") || upper.contains("W-2")) {
                    data = dataExtractionService.extractW2Data(ocrText);
                } else if (upper.contains("1099")) {
                    data = dataExtractionService.extract1099Data(ocrText, docType);
                } else if (upper.contains("ID") || upper.contains("LICENSE") || upper.contains("PASSPORT")) {
                    data = dataExtractionService.extractGovernmentIdData(ocrText);
                }
                if (data != null) {
                    extracted = objectMapper.convertValue(data, new TypeReference<Map<String, Object>>() {});
                } else if (ocrText != null && !ocrText.isBlank()) {
                    extracted.put("ocrPreview", ocrText.substring(0, Math.min(500, ocrText.length())));
                }
            } catch (Exception e) {
                log.warn("OCR failed for federal tax doc: {}", e.getMessage());
                extracted.put("ocrError", e.getMessage());
            }
            doc.setOcrText(ocrText);
            doc.setDocType(docType);
            doc.setExtractedJson(writeJson(extracted));
            documentRepository.save(doc);

            mergeExtractedIntoCase(c, extracted, docType);
            recomputeMissing(c);
            if (c.getClientStatus() == ClientTaxStatus.INTAKE
                    || c.getClientStatus() == ClientTaxStatus.DOCS_PENDING) {
                c.setClientStatus(hasMissing(c) ? ClientTaxStatus.DOCS_PENDING : ClientTaxStatus.INTAKE);
            }
            caseRepository.save(c);
            audit(c.getId(), user.getUserId(), "TAXPAYER", "DOC_UPLOADED",
                    original + " type=" + docType + " via=" + doc.getUploadedVia());
            return toClientDto(c, true);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public FederalTaxCaseDto updateOrganizer(User user, String publicCaseId, UpdateOrganizerRequest req) {
        FederalTaxReturnCase c = requireTaxpayerCase(user, publicCaseId);
        if (req.getOrganizer() != null) {
            c.setOrganizerJson(writeJson(req.getOrganizer()));
        }
        recomputeMissing(c);
        if (Boolean.TRUE.equals(req.getMarkComplete())) {
            List<String> missing = readStringList(c.getMissingItemsJson());
            if (!missing.isEmpty()) {
                c.setClientStatus(ClientTaxStatus.DOCS_PENDING);
                c.setValidationJson(writeJson(Map.of(
                        "ready", false,
                        "missing", missing,
                        "message", "Complete missing items before marking organizer complete")));
                caseRepository.save(c);
                throw new RuntimeException("Organizer incomplete: " + String.join(", ", missing));
            }
            c.setClientStatus(ClientTaxStatus.ORGANIZER_COMPLETE);
            c.setValidationJson(writeJson(Map.of("ready", true, "validatedAt", LocalDateTime.now().toString())));
            c.setClarificationOpen(false);
            audit(c.getId(), user.getUserId(), "TAXPAYER", "ORGANIZER_COMPLETE", null);
        }
        caseRepository.save(c);
        return toClientDto(c, true);
    }

    @Transactional
    public FederalTaxCaseDto selectServiceOption(User user, String publicCaseId, SelectServiceOptionRequest req) {
        FederalTaxReturnCase c = requireTaxpayerCase(user, publicCaseId);
        if (c.getClientStatus() != ClientTaxStatus.ORGANIZER_COMPLETE
                && c.getClientStatus() != ClientTaxStatus.WITH_PREPARER
                && c.getClientStatus() != ClientTaxStatus.EXPORTED_TO_TAX_PRO) {
            throw new RuntimeException("Organizer must be complete before selecting a service option");
        }
        if (req.getServiceOption() == null) {
            throw new RuntimeException("serviceOption is required");
        }
        c.setServiceOption(req.getServiceOption());
        if (req.getServiceOption() == TaxServiceOption.FILE_WITH_US) {
            assignPreparer(c);
            c.setClientStatus(ClientTaxStatus.WITH_PREPARER);
            audit(c.getId(), user.getUserId(), "SYSTEM", "ASSIGNED_PREPARER",
                    "preparerUserId=" + c.getAssignedPreparerUserId());
        } else {
            String path = exportPackageService.buildPackage(c, documentRepository.findByCaseIdOrderByCreatedAtDesc(c.getId()));
            c.setExportPackagePath(path);
            c.setAssignedPreparerUserId(null);
            c.setClientStatus(ClientTaxStatus.EXPORTED_TO_TAX_PRO);
            audit(c.getId(), user.getUserId(), "TAXPAYER", "EXPORTED_TO_TAX_PRO", path);
        }
        caseRepository.save(c);
        return toClientDto(c, true);
    }

    @Transactional
    public FederalTaxCaseDto respondClarification(User user, String publicCaseId, ClarificationRequest req) {
        FederalTaxReturnCase c = requireTaxpayerCase(user, publicCaseId);
        if (!c.isClarificationOpen()) {
            throw new RuntimeException("No clarification request is open");
        }
        Map<String, Object> org = readMap(c.getOrganizerJson());
        org.put("clarificationResponse", req.getNote());
        org.put("clarificationRespondedAt", LocalDateTime.now().toString());
        c.setOrganizerJson(writeJson(org));
        c.setClarificationOpen(false);
        c.setClarificationNote(null);
        c.setClientStatus(ClientTaxStatus.WITH_PREPARER);
        caseRepository.save(c);
        audit(c.getId(), user.getUserId(), "TAXPAYER", "CLARIFICATION_RESPONSE", req.getNote());
        return toClientDto(c, true);
    }

    @Transactional
    public FederalTaxCaseDto sign8879(User user, String publicCaseId, Sign8879Request req,
            String ip, String userAgent) {
        FederalTaxReturnCase c = requireTaxpayerCase(user, publicCaseId);
        if (!c.isReviewerApproved() && c.getServiceOption() == TaxServiceOption.FILE_WITH_US) {
            throw new RuntimeException("Return must be reviewer-approved before Form 8879 signature");
        }
        if (!Boolean.TRUE.equals(req.getConsent())) {
            throw new RuntimeException("Consent is required to sign Form 8879");
        }
        String role = req.getSignerRole() != null ? req.getSignerRole().toUpperCase(Locale.ROOT) : "TAXPAYER";
        Form8879Authorization auth = new Form8879Authorization();
        auth.setCaseId(c.getId());
        auth.setSignerRole(role);
        auth.setSignerUserId(user.getUserId());
        auth.setSignerName(req.getSignerName() != null ? req.getSignerName() : user.getUsername());
        auth.setConsentText("I authorize the electronic filing of my federal income tax return (Form 8879). "
                + "This is an IRS-oriented e-signature audit record. Provider: INTERNAL_AUDIT_TRAIL "
                + "(replace with DocuSign/HelloSign when production compliance vendor is selected).");
        auth.setSignatureMethod(req.getSignatureMethod());
        auth.setESignProvider("INTERNAL_AUDIT_TRAIL");
        auth.setIpAddress(ip);
        auth.setUserAgent(userAgent);
        auth.setSignedAt(LocalDateTime.now());
        auth.setAuthTokenHash(sha256(c.getPublicCaseId() + "|" + user.getUserId() + "|" + auth.getSignedAt()));
        form8879Repository.save(auth);

        if ("SPOUSE".equals(role)) {
            c.setSpouse8879Signed(true);
        } else {
            c.setForm8879Signed(true);
            c.setForm8879SignedAt(LocalDateTime.now());
        }
        boolean spouseOk = !c.isSpouse8879Required() || c.isSpouse8879Signed();
        if (c.isForm8879Signed() && spouseOk && c.isReviewerApproved()) {
            c.setClientStatus(ClientTaxStatus.SIGNED);
        }
        caseRepository.save(c);
        audit(c.getId(), user.getUserId(), "TAXPAYER", "FORM_8879_SIGNED", role);
        return toClientDto(c, true);
    }

    // --- Preparer / reviewer (internal) ---

    @Transactional
    public TaxPreparerProfile upsertPreparer(User admin, Long preparerUserId, String name,
            int maxOpen, String quals) {
        requireUser(admin);
        TaxPreparerProfile p = preparerRepository.findByUserId(preparerUserId).orElseGet(TaxPreparerProfile::new);
        p.setUserId(preparerUserId);
        p.setDisplayNameInternal(name);
        p.setMaxOpenCases(maxOpen > 0 ? maxOpen : 25);
        p.setQualifications(quals != null ? quals : "FORM_1040,SCHEDULE_C");
        p.setActive(true);
        return preparerRepository.save(p);
    }

    public List<FederalTaxCaseDto> listAssignedToPreparer(User preparer) {
        requireUser(preparer);
        return caseRepository.findByAssignedPreparerUserIdAndClientStatusInOrderByUpdatedAtDesc(
                        preparer.getUserId(), OPEN_PREPARER_STATUSES)
                .stream().map(c -> toClientDto(c, true)).toList();
    }

    @Transactional
    public FederalTaxCaseDto requestClarification(User preparer, String publicCaseId, ClarificationRequest req) {
        FederalTaxReturnCase c = requirePreparerCase(preparer, publicCaseId);
        c.setClarificationOpen(true);
        c.setClarificationNote(req.getNote());
        c.setClientStatus(ClientTaxStatus.ACTION_REQUIRED);
        caseRepository.save(c);
        audit(c.getId(), preparer.getUserId(), "PREPARER", "CLARIFICATION_REQUESTED", req.getNote());
        return toClientDto(c, true);
    }

    @Transactional
    public FederalTaxCaseDto markInReview(User preparer, String publicCaseId) {
        FederalTaxReturnCase c = requirePreparerCase(preparer, publicCaseId);
        c.setClientStatus(ClientTaxStatus.IN_REVIEW);
        caseRepository.save(c);
        audit(c.getId(), preparer.getUserId(), "PREPARER", "MARKED_IN_REVIEW", null);
        return toClientDto(c, true);
    }

    @Transactional
    public FederalTaxCaseDto reviewerApprove(User reviewer, String publicCaseId) {
        FederalTaxReturnCase c = caseRepository.findByPublicCaseId(publicCaseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        requireUser(reviewer);
        c.setReviewerUserId(reviewer.getUserId());
        c.setReviewerApproved(true);
        c.setReviewerApprovedAt(LocalDateTime.now());
        if (c.getClientStatus() == ClientTaxStatus.IN_REVIEW
                || c.getClientStatus() == ClientTaxStatus.WITH_PREPARER) {
            // Ready for taxpayer signature — still show With Preparer / In Review until signed
            c.setClientStatus(ClientTaxStatus.IN_REVIEW);
        }
        caseRepository.save(c);
        audit(c.getId(), reviewer.getUserId(), "REVIEWER", "APPROVED", null);
        return toClientDto(c, true);
    }

    @Transactional
    public FederalTaxCaseDto markFiled(User actor, String publicCaseId, boolean accepted) {
        FederalTaxReturnCase c = caseRepository.findByPublicCaseId(publicCaseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        if (!c.isForm8879Signed() || !c.isReviewerApproved()) {
            throw new RuntimeException("Cannot file without reviewer approval and Form 8879 signatures");
        }
        c.setClientStatus(accepted ? ClientTaxStatus.ACCEPTED : ClientTaxStatus.FILED);
        caseRepository.save(c);
        audit(c.getId(), actor.getUserId(), "SYSTEM", accepted ? "ACCEPTED" : "FILED",
                "Partner tax software filing acknowledgment recorded in Numbrics (MeF not in Phase 1)");
        return toClientDto(c, true);
    }

    public Map<String, Object> exportFormatNotes() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("internalFormats", List.of("JSON", "CSV", "PDF_SUMMARY", "SOURCE_DOCUMENTS_ZIP"));
        m.put("proSeries", Map.of(
                "status", "FILE_BASED_PACKAGE",
                "note", "Phase 1 delivers a preparation-ready package. Native ProSeries import format "
                        + "must be confirmed with Intuit Client Manager / vendor docs; do not claim "
                        + "direct ProSeries import until demonstrated."));
        m.put("crossLink", Map.of(
                "status", "INVESTIGATION_REQUIRED",
                "note", "CrossLink Tax import method not yet vendor-confirmed. Same package + limitations letter."));
        m.put("existingIntakeConnectors", List.of("PROCONNECT", "DRAKE", "PROSERIES", "TAXWISE", "CCH_AXCESS", "LACERTE"));
        return m;
    }

    // --- helpers ---

    private void assignPreparer(FederalTaxReturnCase c) {
        List<TaxPreparerProfile> pool = preparerRepository.findByActiveTrue();
        if (pool.isEmpty()) {
            // Seed a virtual assignment slot using taxpayer admin fallback — still tracks assignment id null-safe
            log.warn("No DFG preparer profiles configured; case {} left unassigned until pool is seeded",
                    c.getPublicCaseId());
            return;
        }
        String need = c.getReturnType().name();
        TaxPreparerProfile best = null;
        long bestLoad = Long.MAX_VALUE;
        for (TaxPreparerProfile p : pool) {
            if (p.getQualifications() != null && !p.getQualifications().contains(need)
                    && !p.getQualifications().contains("FORM_1040")) {
                continue;
            }
            long load = caseRepository.countByAssignedPreparerUserIdAndClientStatusIn(
                    p.getUserId(), OPEN_PREPARER_STATUSES);
            if (load >= p.getMaxOpenCases()) {
                continue;
            }
            if (load < bestLoad) {
                bestLoad = load;
                best = p;
            }
        }
        if (best != null) {
            c.setAssignedPreparerUserId(best.getUserId());
            c.setAssignedAt(LocalDateTime.now());
        }
    }

    private Map<String, Object> emptyOrganizer(FederalTaxReturnType type) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("personal", Map.of("firstName", "", "lastName", "", "ssnLast4", "", "filingStatus", "",
                "address", "", "city", "", "state", "", "zip", "", "email", "", "phone", ""));
        o.put("dependents", List.of());
        o.put("w2s", List.of());
        o.put("form1099s", List.of());
        o.put("income", Map.of());
        o.put("deductions", Map.of());
        o.put("credits", Map.of());
        if (type == FederalTaxReturnType.SCHEDULE_C || type == FederalTaxReturnType.FORM_1040) {
            o.put("scheduleC", Map.of(
                    "businessName", "", "ein", "", "revenue", 0, "expenses", Map.of(),
                    "mileage", 0, "homeOffice", false));
        }
        o.put("confirmedByTaxpayer", false);
        return o;
    }

    private List<String> defaultMissing(FederalTaxReturnType type) {
        List<String> m = new ArrayList<>(List.of("personal.firstName", "personal.lastName", "personal.filingStatus"));
        if (type == FederalTaxReturnType.SCHEDULE_C) {
            m.add("scheduleC.businessName");
            m.add("scheduleC.revenue");
        }
        return m;
    }

    private void mergeExtractedIntoCase(FederalTaxReturnCase c, Map<String, Object> extracted, String docType) {
        Map<String, Object> allExtracted = readMap(c.getExtractedJson());
        allExtracted.put(docType + "_" + System.currentTimeMillis(), extracted);
        c.setExtractedJson(writeJson(allExtracted));

        Map<String, Object> org = readMap(c.getOrganizerJson());
        @SuppressWarnings("unchecked")
        Map<String, Object> personal = org.get("personal") instanceof Map
                ? new LinkedHashMap<>((Map<String, Object>) org.get("personal"))
                : new LinkedHashMap<>();
        copyIfPresent(extracted, personal, "firstName", "first_name", "taxpayerFirstName");
        copyIfPresent(extracted, personal, "lastName", "last_name", "taxpayerLastName");
        copyIfPresent(extracted, personal, "ssn", "ssnLast4", "ssn_last4");
        copyIfPresent(extracted, personal, "address", "street", "streetAddress");
        copyIfPresent(extracted, personal, "city", "city");
        copyIfPresent(extracted, personal, "state", "state");
        copyIfPresent(extracted, personal, "zip", "zip", "postalCode");
        org.put("personal", personal);

        if ("W2".equalsIgnoreCase(docType) || extracted.containsKey("wages") || extracted.containsKey("w2_wages")) {
            @SuppressWarnings("unchecked")
            List<Object> w2s = org.get("w2s") instanceof List ? new ArrayList<>((List<Object>) org.get("w2s")) : new ArrayList<>();
            w2s.add(extracted);
            org.put("w2s", w2s);
        }
        if (docType != null && docType.toUpperCase(Locale.ROOT).contains("1099")) {
            @SuppressWarnings("unchecked")
            List<Object> f = org.get("form1099s") instanceof List ? new ArrayList<>((List<Object>) org.get("form1099s")) : new ArrayList<>();
            f.add(extracted);
            org.put("form1099s", f);
        }
        c.setOrganizerJson(writeJson(org));
    }

    private void copyIfPresent(Map<String, Object> from, Map<String, Object> to, String toKey, String... fromKeys) {
        for (String k : fromKeys) {
            if (from.containsKey(k) && from.get(k) != null && !String.valueOf(from.get(k)).isBlank()) {
                Object cur = to.get(toKey);
                if (cur == null || String.valueOf(cur).isBlank()) {
                    to.put(toKey, from.get(k));
                }
                return;
            }
        }
    }

    private void recomputeMissing(FederalTaxReturnCase c) {
        Map<String, Object> org = readMap(c.getOrganizerJson());
        List<String> missing = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> personal = org.get("personal") instanceof Map
                ? (Map<String, Object>) org.get("personal") : Map.of();
        if (blank(personal.get("firstName"))) {
            missing.add("personal.firstName");
        }
        if (blank(personal.get("lastName"))) {
            missing.add("personal.lastName");
        }
        if (blank(personal.get("filingStatus"))) {
            missing.add("personal.filingStatus");
        }
        if (c.getReturnType() == FederalTaxReturnType.SCHEDULE_C) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sch = org.get("scheduleC") instanceof Map
                    ? (Map<String, Object>) org.get("scheduleC") : Map.of();
            if (blank(sch.get("businessName"))) {
                missing.add("scheduleC.businessName");
            }
        }
        @SuppressWarnings("unchecked")
        List<?> w2s = org.get("w2s") instanceof List ? (List<?>) org.get("w2s") : List.of();
        @SuppressWarnings("unchecked")
        List<?> f1099 = org.get("form1099s") instanceof List ? (List<?>) org.get("form1099s") : List.of();
        if (w2s.isEmpty() && f1099.isEmpty() && c.getReturnType() == FederalTaxReturnType.FORM_1040) {
            missing.add("income.document (W-2 or 1099 upload or manual entry)");
        }
        c.setMissingItemsJson(writeJson(missing));
        if (!missing.isEmpty() && (c.getClientStatus() == ClientTaxStatus.INTAKE
                || c.getClientStatus() == ClientTaxStatus.DOCS_PENDING)) {
            c.setClientStatus(ClientTaxStatus.DOCS_PENDING);
        }
    }

    private boolean hasMissing(FederalTaxReturnCase c) {
        return !readStringList(c.getMissingItemsJson()).isEmpty();
    }

    private boolean blank(Object v) {
        return v == null || String.valueOf(v).isBlank();
    }

    private FederalTaxCaseDto toClientDto(FederalTaxReturnCase c, boolean includeDocs) {
        FederalTaxCaseDto dto = new FederalTaxCaseDto();
        dto.setPublicCaseId(c.getPublicCaseId());
        dto.setReturnType(c.getReturnType());
        dto.setServiceOption(c.getServiceOption());
        dto.setClientStatus(c.getClientStatus());
        dto.setTaxYear(c.getTaxYear());
        dto.setOrganizer(readMap(c.getOrganizerJson()));
        dto.setExtracted(readMap(c.getExtractedJson()));
        dto.setMissingItems(readStringList(c.getMissingItemsJson()));
        dto.setValidation(readMap(c.getValidationJson()));
        dto.setClarificationOpen(c.isClarificationOpen());
        // Client sees clarification prompt text, never preparer identity
        dto.setClarificationNote(c.isClarificationOpen() ? c.getClarificationNote() : null);
        dto.setForm8879Signed(c.isForm8879Signed());
        dto.setSpouse8879Required(c.isSpouse8879Required());
        dto.setSpouse8879Signed(c.isSpouse8879Signed());
        dto.setReviewerApproved(c.isReviewerApproved());
        dto.setTaxpayerConsent(c.isTaxpayerConsent());
        dto.setExportPackagePath(c.getExportPackagePath());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        if (includeDocs) {
            dto.setDocuments(documentRepository.findByCaseIdOrderByCreatedAtDesc(c.getId()).stream().map(d -> {
                FederalTaxDocumentDto dd = new FederalTaxDocumentDto();
                dd.setId(d.getId());
                dd.setOriginalFilename(d.getOriginalFilename());
                dd.setDocType(d.getDocType());
                dd.setUploadedVia(d.getUploadedVia());
                dd.setExtracted(readMap(d.getExtractedJson()));
                dd.setCreatedAt(d.getCreatedAt());
                return dd;
            }).toList());
        }
        return dto;
    }

    private FederalTaxReturnCase requireTaxpayerCase(User user, String publicCaseId) {
        requireUser(user);
        FederalTaxReturnCase c = caseRepository.findByPublicCaseId(publicCaseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        if (!Objects.equals(c.getTaxpayerUserId(), user.getUserId())) {
            throw new RuntimeException("Not authorized for this case");
        }
        return c;
    }

    private FederalTaxReturnCase requirePreparerCase(User preparer, String publicCaseId) {
        requireUser(preparer);
        FederalTaxReturnCase c = caseRepository.findByPublicCaseId(publicCaseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        if (!Objects.equals(c.getAssignedPreparerUserId(), preparer.getUserId())) {
            throw new RuntimeException("Case is not assigned to this preparer");
        }
        return c;
    }

    private void requireUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new RuntimeException("Authentication required");
        }
    }

    private void audit(Long caseId, Long actorId, String role, String action, String detail) {
        TaxCaseAuditLog logRow = new TaxCaseAuditLog();
        logRow.setCaseId(caseId);
        logRow.setActorUserId(actorId);
        logRow.setActorRole(role);
        logRow.setAction(action);
        logRow.setDetail(detail);
        auditLogRepository.save(logRow);
    }

    private String writeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
