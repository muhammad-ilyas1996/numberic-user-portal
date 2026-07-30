package com.numbericsuserportal.taxintake.service;

import com.numbericsuserportal.taxintake.dto.ProconnectTaxIntakeRecordDto;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProconnectTaxIntakeService {
    ProconnectTaxIntakeRecordDto uploadAndExtract(MultipartFile file, User currentUser);

    ProconnectTaxIntakeRecordDto uploadSpreadsheet(MultipartFile file, User currentUser);

    List<ProconnectTaxIntakeRecordDto> listMyRecords(User currentUser);

    ProconnectTaxIntakeRecordDto getRecord(Long id, User currentUser);

    byte[] buildCsvTemplate();

    byte[] buildExcelTemplate();
}
