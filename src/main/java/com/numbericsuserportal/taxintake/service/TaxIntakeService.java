package com.numbericsuserportal.taxintake.service;

import com.numbericsuserportal.taxintake.dto.TaxIntakeRecordDto;
import com.numbericsuserportal.taxintake.dto.TaxSoftwareDto;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaxIntakeService {

    List<TaxSoftwareDto> listSoftwares();

    TaxSoftwareDto getSoftware(String softwareCode);

    TaxIntakeRecordDto uploadDocument(String softwareCode, MultipartFile file, User currentUser);

    TaxIntakeRecordDto uploadSpreadsheet(String softwareCode, MultipartFile file, User currentUser);

    List<TaxIntakeRecordDto> listRecords(String softwareCode, User currentUser);

    TaxIntakeRecordDto getRecord(String softwareCode, Long id, User currentUser);

    byte[] buildCsvTemplate(String softwareCode);

    byte[] buildExcelTemplate(String softwareCode);
}
