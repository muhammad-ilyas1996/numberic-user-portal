package com.numbericsuserportal.taxbandit.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.taxbandit.auth.TaxBanditsAuthService;
import com.numbericsuserportal.taxbandit.exception.TaxBanditsApiException;
import com.numbericsuserportal.taxbandit.formnec.dto.*;
import com.numbericsuserportal.taxbandit.form1099misc.dto.*;
import com.numbericsuserportal.taxbandit.form1099k.dto.*;
import java.util.UUID;
import com.numbericsuserportal.taxbandit.service.TaxBanditsApiService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class TaxBanditsApiServiceImpl implements TaxBanditsApiService {

    private static final Logger log = LoggerFactory.getLogger(TaxBanditsApiServiceImpl.class);

    @Autowired
    private TaxBanditsAuthService taxBanditsAuthService;

    @Value("${taxbandits.api.base.url:https://testapi.taxbandits.com/V1.7.3}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TaxBanditsApiServiceImpl() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper = mapper;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(java.time.Duration.ofSeconds(30));
        factory.setConnectionRequestTimeout(java.time.Duration.ofSeconds(30));
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public CreateForm1099NECResponseDTO createForm1099NEC(CreateForm1099NECRequestDTO request) {
        return executeApiCall("Form1099NEC/Create", HttpMethod.POST, request,
            CreateForm1099NECResponseDTO.class, "Form 1099-NEC Create");
    }

    @Override
    public CreateForm1099MISCResponseDTO createForm1099MISC(CreateForm1099MISCRequestDTO request) {
        return executeApiCall("Form1099MISC/Create", HttpMethod.POST, request,
            CreateForm1099MISCResponseDTO.class, "Form 1099-MISC Create");
    }

    // ========== Form 1099-NEC Additional Endpoints ==========
    
    @Override
    public UpdateForm1099NECResponseDTO updateForm1099NEC(UpdateForm1099NECRequestDTO request) {
        return makeForm1099NECApiCall("Form1099NEC/Update", HttpMethod.PUT, request, 
            UpdateForm1099NECResponseDTO.class);
    }

    @Override
    public TransmitForm1099NECResponseDTO transmitForm1099NEC(TransmitForm1099NECRequestDTO request) {
        return makeForm1099NECApiCall("Form1099NEC/Transmit", HttpMethod.POST, request, 
            TransmitForm1099NECResponseDTO.class);
    }

    @Override
    public Form1099NECStatusResponseDTO getForm1099NECStatus(UUID submissionId) {
        String endpoint = "Form1099NEC/Status?SubmissionId=" + submissionId;
        return makeForm1099NECApiCall(endpoint, HttpMethod.GET, null, 
            Form1099NECStatusResponseDTO.class);
    }

    @Override
    public ValidateForm1099NECResponseDTO validateForm1099NEC(ValidateForm1099NECRequestDTO request) {
        return makeForm1099NECApiCall("Form1099NEC/ValidateForm", HttpMethod.POST, request, 
            ValidateForm1099NECResponseDTO.class);
    }

    @Override
    public ValidateForm1099NECResponseDTO validateForm1099NEC(UUID submissionId) {
        String endpoint = "Form1099NEC/Validate?SubmissionId=" + submissionId;
        return makeForm1099NECApiCall(endpoint, HttpMethod.GET, null, 
            ValidateForm1099NECResponseDTO.class);
    }

    @Override
    public CreateForm1099NECResponseDTO getForm1099NEC(UUID submissionId, UUID recordId) {
        String endpoint = "Form1099NEC/Get?SubmissionId=" + submissionId + 
            (recordId != null ? "&RecordId=" + recordId : "");
        return makeForm1099NECApiCall(endpoint, HttpMethod.GET, null, 
            CreateForm1099NECResponseDTO.class);
    }

    @Override
    public Object listForm1099NEC(UUID submissionId, String businessId) {
        String endpoint = "Form1099NEC/List?SubmissionId=" + submissionId + 
            (businessId != null ? "&BusinessId=" + businessId : "");
        return makeForm1099NECApiCall(endpoint, HttpMethod.GET, null, Object.class);
    }

    @Override
    public Object deleteForm1099NEC(UUID submissionId, UUID recordId) {
        String endpoint = "Form1099NEC/Delete?SubmissionId=" + submissionId + 
            (recordId != null ? "&RecordId=" + recordId : "");
        return makeForm1099NECApiCall(endpoint, HttpMethod.DELETE, null, Object.class);
    }

    @Override
    public Object requestPdfURLsForm1099NEC(RequestPdfURLsForm1099NECRequestDTO request) {
        return makeForm1099NECApiCall("Form1099NEC/RequestPdfURLs", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object requestDraftPdfUrlForm1099NEC(RequestDraftPdfUrlForm1099NECRequestDTO request) {
        return makeForm1099NECApiCall("Form1099NEC/RequestDraftPdfUrl", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object getPDFForm1099NEC(UUID submissionId, UUID recordId) {
        String endpoint = "Form1099NEC/GetPDF?SubmissionId=" + submissionId + 
            (recordId != null ? "&RecordId=" + recordId : "");
        return makeForm1099NECApiCall(endpoint, HttpMethod.GET, null, Object.class);
    }

    @Override
    public Object generateFromTxnsForm1099NEC(GenerateFromTxnsForm1099NECRequestDTO request) {
        return makeForm1099NECApiCall("Form1099NEC/GenerateFromTxns", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object approveForm1099NEC(ApproveForm1099NECRequestDTO request) {
        return makeForm1099NECApiCall("Form1099NEC/Approve", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object getByRecordIdsForm1099NEC(GetbyRecordIdsForm1099NECRequestDTO request) {
        return makeForm1099NECApiCall("Form1099NEC/GetbyRecordIds", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object requestDistUrlForm1099NEC(RequestDistUrlForm1099NECRequestDTO request) {
        return makeForm1099NECApiCall("Form1099NEC/RequestDistUrl", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object uploadAttachmentForm1099NEC(UploadAttachmentForm1099NECRequestDTO request) {
        return makeForm1099NECApiCall("Form1099NEC/UploadAttachment", HttpMethod.POST, request, Object.class);
    }

    private <T> T makeForm1099NECApiCall(String endpointPath, HttpMethod httpMethod, Object requestBody, 
                                         Class<T> responseType) {
        return executeApiCall(endpointPath, httpMethod, requestBody, responseType, "Form 1099-NEC");
    }

    // ========== Form 1099-MISC Additional Endpoints ==========
    
    @Override
    public UpdateForm1099MISCResponseDTO updateForm1099MISC(UpdateForm1099MISCRequestDTO request) {
        return makeForm1099MISCApiCall("Form1099MISC/Update", HttpMethod.PUT, request, 
            UpdateForm1099MISCResponseDTO.class);
    }

    @Override
    public TransmitForm1099MISCResponseDTO transmitForm1099MISC(TransmitForm1099MISCRequestDTO request) {
        return makeForm1099MISCApiCall("Form1099MISC/Transmit", HttpMethod.POST, request, 
            TransmitForm1099MISCResponseDTO.class);
    }

    @Override
    public Form1099MISCStatusResponseDTO getForm1099MISCStatus(UUID submissionId) {
        String endpoint = "Form1099MISC/Status?SubmissionId=" + submissionId;
        return makeForm1099MISCApiCall(endpoint, HttpMethod.GET, null, 
            Form1099MISCStatusResponseDTO.class);
    }

    @Override
    public ValidateForm1099MISCResponseDTO validateForm1099MISC(ValidateForm1099MISCRequestDTO request) {
        return makeForm1099MISCApiCall("Form1099MISC/ValidateForm", HttpMethod.POST, request, 
            ValidateForm1099MISCResponseDTO.class);
    }

    @Override
    public ValidateForm1099MISCResponseDTO validateForm1099MISC(UUID submissionId) {
        String endpoint = "Form1099MISC/Validate?SubmissionId=" + submissionId;
        return makeForm1099MISCApiCall(endpoint, HttpMethod.GET, null, 
            ValidateForm1099MISCResponseDTO.class);
    }

    @Override
    public CreateForm1099MISCResponseDTO getForm1099MISC(UUID submissionId, UUID recordId) {
        String endpoint = "Form1099MISC/Get?SubmissionId=" + submissionId + 
            (recordId != null ? "&RecordId=" + recordId : "");
        return makeForm1099MISCApiCall(endpoint, HttpMethod.GET, null, 
            CreateForm1099MISCResponseDTO.class);
    }

    @Override
    public Object listForm1099MISC(UUID submissionId, String businessId) {
        String endpoint = "Form1099MISC/List?SubmissionId=" + submissionId + 
            (businessId != null ? "&BusinessId=" + businessId : "");
        return makeForm1099MISCApiCall(endpoint, HttpMethod.GET, null, Object.class);
    }

    @Override
    public Object deleteForm1099MISC(UUID submissionId, UUID recordId) {
        String endpoint = "Form1099MISC/Delete?SubmissionId=" + submissionId + 
            (recordId != null ? "&RecordId=" + recordId : "");
        return makeForm1099MISCApiCall(endpoint, HttpMethod.DELETE, null, Object.class);
    }

    @Override
    public Object requestPdfURLsForm1099MISC(RequestPdfURLsForm1099MISCRequestDTO request) {
        return makeForm1099MISCApiCall("Form1099MISC/RequestPdfURLs", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object requestDraftPdfUrlForm1099MISC(RequestDraftPdfUrlForm1099MISCRequestDTO request) {
        return makeForm1099MISCApiCall("Form1099MISC/RequestDraftPdfUrl", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object getPDFForm1099MISC(UUID submissionId, UUID recordId) {
        String endpoint = "Form1099MISC/GetPDF?SubmissionId=" + submissionId + 
            (recordId != null ? "&RecordId=" + recordId : "");
        return makeForm1099MISCApiCall(endpoint, HttpMethod.GET, null, Object.class);
    }

    @Override
    public Object generateFromTxnsForm1099MISC(GenerateFromTxnsForm1099MISCRequestDTO request) {
        return makeForm1099MISCApiCall("Form1099MISC/GenerateFromTxns", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object approveForm1099MISC(ApproveForm1099MISCRequestDTO request) {
        return makeForm1099MISCApiCall("Form1099MISC/Approve", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object getByRecordIdsForm1099MISC(GetbyRecordIdsForm1099MISCRequestDTO request) {
        return makeForm1099MISCApiCall("Form1099MISC/GetbyRecordIds", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object requestDistUrlForm1099MISC(RequestDistUrlForm1099MISCRequestDTO request) {
        return makeForm1099MISCApiCall("Form1099MISC/RequestDistUrl", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object uploadAttachmentForm1099MISC(UploadAttachmentForm1099MISCRequestDTO request) {
        return makeForm1099MISCApiCall("Form1099MISC/UploadAttachment", HttpMethod.POST, request, Object.class);
    }

    private <T> T makeForm1099MISCApiCall(String endpointPath, HttpMethod httpMethod, Object requestBody, 
                                         Class<T> responseType) {
        return executeApiCall(endpointPath, httpMethod, requestBody, responseType, "Form 1099-MISC");
    }

    // ========== Form 1099-K Additional Endpoints ==========
    
    @Override
    public CreateForm1099KResponseDTO createForm1099K(CreateForm1099KRequestDTO request) {
        return executeApiCall("Form1099K/Create", HttpMethod.POST, request,
            CreateForm1099KResponseDTO.class, "Form 1099-K Create");
    }

    @Override
    public UpdateForm1099KResponseDTO updateForm1099K(UpdateForm1099KRequestDTO request) {
        return makeForm1099KApiCall("Form1099K/Update", HttpMethod.PUT, request, 
            UpdateForm1099KResponseDTO.class);
    }

    @Override
    public TransmitForm1099KResponseDTO transmitForm1099K(TransmitForm1099KRequestDTO request) {
        return makeForm1099KApiCall("Form1099K/Transmit", HttpMethod.POST, request, 
            TransmitForm1099KResponseDTO.class);
    }

    @Override
    public Form1099KStatusResponseDTO getForm1099KStatus(UUID submissionId) {
        String endpoint = "Form1099K/Status?SubmissionId=" + submissionId;
        return makeForm1099KApiCall(endpoint, HttpMethod.GET, null, 
            Form1099KStatusResponseDTO.class);
    }

    @Override
    public ValidateForm1099KResponseDTO validateForm1099K(ValidateForm1099KRequestDTO request) {
        return makeForm1099KApiCall("Form1099K/ValidateForm", HttpMethod.POST, request, 
            ValidateForm1099KResponseDTO.class);
    }

    @Override
    public ValidateForm1099KResponseDTO validateForm1099K(UUID submissionId) {
        String endpoint = "Form1099K/Validate?SubmissionId=" + submissionId;
        return makeForm1099KApiCall(endpoint, HttpMethod.GET, null, 
            ValidateForm1099KResponseDTO.class);
    }

    @Override
    public CreateForm1099KResponseDTO getForm1099K(UUID submissionId, UUID recordId) {
        String endpoint = "Form1099K/Get?SubmissionId=" + submissionId + 
            (recordId != null ? "&RecordId=" + recordId : "");
        return makeForm1099KApiCall(endpoint, HttpMethod.GET, null, 
            CreateForm1099KResponseDTO.class);
    }

    @Override
    public Object listForm1099K(UUID submissionId, String businessId) {
        String endpoint = "Form1099K/List?SubmissionId=" + submissionId + 
            (businessId != null ? "&BusinessId=" + businessId : "");
        return makeForm1099KApiCall(endpoint, HttpMethod.GET, null, Object.class);
    }

    @Override
    public Object deleteForm1099K(UUID submissionId, UUID recordId) {
        String endpoint = "Form1099K/Delete?SubmissionId=" + submissionId + 
            (recordId != null ? "&RecordId=" + recordId : "");
        return makeForm1099KApiCall(endpoint, HttpMethod.DELETE, null, Object.class);
    }

    @Override
    public Object requestPdfURLsForm1099K(RequestPdfURLsForm1099KRequestDTO request) {
        return makeForm1099KApiCall("Form1099K/RequestPdfURLs", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object requestDraftPdfUrlForm1099K(RequestDraftPdfUrlForm1099KRequestDTO request) {
        return makeForm1099KApiCall("Form1099K/RequestDraftPdfUrl", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object getPDFForm1099K(UUID submissionId, UUID recordId) {
        String endpoint = "Form1099K/GetPDF?SubmissionId=" + submissionId + 
            (recordId != null ? "&RecordId=" + recordId : "");
        return makeForm1099KApiCall(endpoint, HttpMethod.GET, null, Object.class);
    }

    @Override
    public Object generateFromTxnsForm1099K(GenerateFromTxnsForm1099KRequestDTO request) {
        return makeForm1099KApiCall("Form1099K/GenerateFromTxns", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object approveForm1099K(ApproveForm1099KRequestDTO request) {
        return makeForm1099KApiCall("Form1099K/Approve", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object getByRecordIdsForm1099K(GetbyRecordIdsForm1099KRequestDTO request) {
        return makeForm1099KApiCall("Form1099K/GetbyRecordIds", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object requestDistUrlForm1099K(RequestDistUrlForm1099KRequestDTO request) {
        return makeForm1099KApiCall("Form1099K/RequestDistUrl", HttpMethod.POST, request, Object.class);
    }

    @Override
    public Object uploadAttachmentForm1099K(UploadAttachmentForm1099KRequestDTO request) {
        return makeForm1099KApiCall("Form1099K/UploadAttachment", HttpMethod.POST, request, Object.class);
    }

    private <T> T makeForm1099KApiCall(String endpointPath, HttpMethod httpMethod, Object requestBody, 
                                     Class<T> responseType) {
        return executeApiCall(endpointPath, httpMethod, requestBody, responseType, "Form 1099-K");
    }

    /**
     * Centralized TaxBandits API execution with consistent error handling.
     * Extracts upstream error details from HttpStatusCodeException for meaningful client responses.
     */
    private <T> T executeApiCall(String endpointPath, HttpMethod httpMethod, Object requestBody,
                                 Class<T> responseType, String operationName) {
        String accessToken = taxBanditsAuthService.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new TaxBanditsApiException("Access token is null or empty. OAuth authentication may have failed.",
                HttpStatus.UNAUTHORIZED);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity;
        try {
            if (requestBody != null && (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT)) {
                String requestBodyJson = objectMapper.writeValueAsString(requestBody);
                entity = new HttpEntity<>(requestBodyJson, headers);
            } else {
                entity = new HttpEntity<>(headers);
            }
        } catch (Exception e) {
            log.error("Failed to serialize request for {}: {}", operationName, e.getMessage());
            throw new TaxBanditsApiException("Invalid request payload", e);
        }

        String baseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl : apiBaseUrl + "/";
        String apiUrl = baseUrl + endpointPath;

        log.debug("TaxBandits API call: {} {} -> {}", httpMethod, operationName, endpointPath);

        try {
            ResponseEntity<T> response = restTemplate.exchange(apiUrl, httpMethod, entity, responseType);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
            String responseBody = e.getResponseBodyAsString();
            String upstreamMsg = (responseBody != null && !responseBody.isBlank())
                ? responseBody
                : e.getMessage();
            log.warn("TaxBandits API error ({}): {} - {}", operationName, status, upstreamMsg);
            throw new TaxBanditsApiException(
                "Failed to call TaxBandits API: " + operationName + " - " + status.value() + " " + status.getReasonPhrase(),
                status,
                upstreamMsg,
                apiUrl
            );
        } catch (Exception e) {
            log.error("TaxBandits API call failed ({}): {}", operationName, e.getMessage());
            throw new TaxBanditsApiException("Failed to call TaxBandits API: " + operationName, e);
        }
    }
}

