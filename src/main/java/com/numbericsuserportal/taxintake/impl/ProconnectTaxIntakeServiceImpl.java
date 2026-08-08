package com.numbericsuserportal.taxintake.impl;

import com.numbericsuserportal.taxintake.domain.TaxSoftwareType;
import com.numbericsuserportal.taxintake.dto.ProconnectTaxIntakeRecordDto;
import com.numbericsuserportal.taxintake.dto.TaxIntakeRecordDto;
import com.numbericsuserportal.taxintake.service.ProconnectTaxIntakeService;
import com.numbericsuserportal.taxintake.service.TaxIntakeService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Backward-compatible ProConnect façade over the shared TaxIntakeService. */
@Service
public class ProconnectTaxIntakeServiceImpl implements ProconnectTaxIntakeService {

    private static final String SOFTWARE = TaxSoftwareType.PROCONNECT.getCode();

    @Autowired
    private TaxIntakeService taxIntakeService;

    @Override
    public ProconnectTaxIntakeRecordDto uploadAndExtract(MultipartFile file, User currentUser) {
        return toLegacy(taxIntakeService.uploadDocument(SOFTWARE, file, currentUser));
    }

    @Override
    public ProconnectTaxIntakeRecordDto uploadSpreadsheet(MultipartFile file, User currentUser) {
        return toLegacy(taxIntakeService.uploadSpreadsheet(SOFTWARE, file, currentUser));
    }

    @Override
    public List<ProconnectTaxIntakeRecordDto> listMyRecords(User currentUser) {
        return taxIntakeService.listRecords(SOFTWARE, currentUser).stream().map(this::toLegacy).toList();
    }

    @Override
    public ProconnectTaxIntakeRecordDto getRecord(Long id, User currentUser) {
        return toLegacy(taxIntakeService.getRecord(SOFTWARE, id, currentUser));
    }

    @Override
    public byte[] buildCsvTemplate() {
        return taxIntakeService.buildCsvTemplate(SOFTWARE);
    }

    @Override
    public byte[] buildExcelTemplate() {
        return taxIntakeService.buildExcelTemplate(SOFTWARE);
    }

    private ProconnectTaxIntakeRecordDto toLegacy(TaxIntakeRecordDto d) {
        return new ProconnectTaxIntakeRecordDto(
                d.getId(),
                d.getSoftwareName(),
                d.getOriginalFileName(),
                d.getContentType(),
                d.getFileSize(),
                d.getDocumentType(),
                d.getRawText(),
                d.getExtractedDataJson(),
                d.getOverallConfidence(),
                d.getPageCount(),
                d.getProcessingStatus(),
                d.getErrorMessage(),
                d.getCreatedOn(),
                d.getCreatedBy()
        );
    }
}
