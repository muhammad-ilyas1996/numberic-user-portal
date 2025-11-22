package com.numbericsuserportal.taxkintsugi.impl;

import com.numbericsuserportal.taxkintsugi.converter.TransactionConverter;
import com.numbericsuserportal.taxkintsugi.dto.TransactionDTO;
import com.numbericsuserportal.taxkintsugi.entity.TransactionEntity;
import com.numbericsuserportal.taxkintsugi.repo.TransactionRepository;
import com.numbericsuserportal.taxkintsugi.service.TaxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TaxServiceImpl implements TaxService {

    @Value("${kintsugi.api.base-url}")
    private String baseUrl;

    @Value("${kintsugi.api.key}")
    private String apiKey;

    @Value("${kintsugi.api.organization-id}")
    private String organizationId;

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    public TaxServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public TransactionDTO calculateTax(TransactionDTO requestDto) {
        try {
            // Set Kintsugi headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("x-organization-id", organizationId);

            HttpEntity<TransactionDTO> httpEntity = new HttpEntity<>(requestDto, headers);

            // Call Kintsugi API
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/v1/tax/estimate",
                    HttpMethod.POST,
                    httpEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Store dynamic tax response
                requestDto.setTaxResponse(response.getBody());

                // Persist transaction with addresses/items cascade
                TransactionEntity entity = TransactionConverter.toEntity(requestDto);
                transactionRepository.save(entity);

                return requestDto;
            } else {
                throw new RuntimeException("Failed to calculate tax: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error calling Kintsugi API: " + e.getMessage(), e);
        }
    }
}