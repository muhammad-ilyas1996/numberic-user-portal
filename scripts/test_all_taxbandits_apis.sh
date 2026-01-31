#!/bin/bash
# =============================================================================
# TaxBandits API - ALL Endpoints Test Script (Full 45+ APIs)
#
# Usage:
#   bash scripts/test_all_taxbandits_apis.sh
#   ./scripts/test_all_taxbandits_apis.sh
#
# Include destructive (transmit, delete):
#   INCLUDE_DESTRUCTIVE=1 bash scripts/test_all_taxbandits_apis.sh
#
# On server:
#   BASE_URL="http://72.62.3.78:8081/numbricsservice" bash scripts/test_all_taxbandits_apis.sh
#
# Requires: curl, python3
# =============================================================================

BASE_URL="${BASE_URL:-http://localhost:8081/numbricsservice}"
INCLUDE_DESTRUCTIVE="${INCLUDE_DESTRUCTIVE:-0}"
PASSED=0
FAILED=0

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

run_test() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local http_code

    printf "\n${YELLOW}[TEST] %s${NC}\n" "$name"
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" -d "$data" 2>/dev/null || echo -e "\n000")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" 2>/dev/null || echo -e "\n000")
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [[ "$http_code" =~ ^2[0-9][0-9]$ ]]; then
        echo -e "${GREEN}  ✓ PASS${NC} (HTTP $http_code)"
        PASSED=$((PASSED + 1))
        echo "$body" | head -c 500
        [ ${#body} -gt 500 ] && echo "... (truncated)"
        echo ""
        return 0
    else
        echo -e "${RED}  ✗ FAIL${NC} (HTTP $http_code)"
        FAILED=$((FAILED + 1))
        echo "$body" | head -c 500
        echo ""
        return 1
    fi
}

# Extract JSON field (uses python3, fallback to grep)
extract_json() {
    local json="$1"
    local key="$2"
    if command -v python3 &>/dev/null; then
        echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('$key',''))" 2>/dev/null || echo ""
    else
        echo "$json" | grep -o "\"$key\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | head -1 | sed 's/.*: *"\([^"]*\)".*/\1/'
    fi
}

extract_nested() {
    local json="$1"
    local path="$2"
    echo "$json" | python3 -c "
import sys,json
try:
    d=json.load(sys.stdin)
    for p in '$path'.split('.'):
        d=d.get(p,{}) if isinstance(d,dict) else (d[int(p)] if isinstance(d,list) and p.isdigit() else {})
    print(d if isinstance(d,str) else '')
except: print('')
" 2>/dev/null || echo ""
}

echo "=============================================="
echo "  TaxBandits API - Full Test Suite (38+ APIs)"
echo "  Base URL: $BASE_URL"
echo "  INCLUDE_DESTRUCTIVE=$INCLUDE_DESTRUCTIVE (transmit, delete)"
echo "=============================================="

# -----------------------------------------------------------------------------
# FORM 1099-NEC
# -----------------------------------------------------------------------------
echo -e "\n${YELLOW}===== FORM 1099-NEC =====${NC}"

# 1. Test Auth
run_test "1099-NEC: test-auth" "GET" "$BASE_URL/api/taxbandits/form1099nec/test-auth" ""

# 2. ValidateForm - Use EIN for recipient (avoids SSN validation; TaxBandits sample uses EIN)
run_test "1099-NEC: validateform" "POST" "$BASE_URL/api/taxbandits/form1099nec/validateform" \
'{"SubmissionManifest":{"TaxYear":"2025","IRSFilingType":"IRIS","IsFederalFiling":true,"IsStateFiling":false,"IsPostal":false,"IsOnlineAccess":false,"IsScheduleFiling":false,"ScheduleFiling":null},"ReturnHeader":{"Business":{"BusinessNm":"Test Company LLC","IsEIN":true,"EINorSSN":"65-7368245","Email":"test@example.com","Phone":"1234567890","BusinessType":"ESTE","IsBusinessTerminated":false,"IsForeign":false,"USAddress":{"Address1":"123 Main St","City":"New York","State":"NY","ZipCd":"10001"}}},"ReturnData":[{"SequenceId":"1","IsForced":false,"Recipient":{"TINType":"EIN","TIN":"39-3817572","FirstPayeeNm":"Test Recipient LLC","IsForeign":false,"USAddress":{"Address1":"456 Oak Ave","City":"Los Angeles","State":"CA","ZipCd":"90001"}},"NECFormData":{"B1NEC":5000.0,"B2IsDirectSales":false,"B3EPP":0.0,"B4FedTaxWH":0.0,"IsFATCA":false,"Is2ndTINnot":false}}]}'

# 3. Create - EIN for recipient (TaxBandits Sample 1 format)
CREATE_PAYLOAD='{"SubmissionManifest":{"TaxYear":"2025","IRSFilingType":"IRIS","IsFederalFiling":true,"IsStateFiling":false,"IsPostal":false,"IsOnlineAccess":true,"IsScheduleFiling":false},"ReturnHeader":{"Business":{"BusinessNm":"Test Company LLC","IsEIN":true,"EINorSSN":"65-7368245","Email":"test@example.com","ContactNm":"John Doe","Phone":"1234567890","IsBusinessTerminated":false,"IsForeign":false,"USAddress":{"Address1":"123 Main St","City":"New York","State":"NY","ZipCd":"10001"},"SigningAuthority":{"Name":"John Doe","Phone":"1234567890","BusinessMemberType":"PRESIDENT"}}},"ReturnData":[{"SequenceId":"1","IsPostal":false,"IsOnlineAccess":true,"IsForced":true,"Recipient":{"TINType":"EIN","TIN":"39-3817572","FirstPayeeNm":"Test Recipient LLC","IsForeign":false,"USAddress":{"Address1":"456 Oak Ave","City":"Los Angeles","State":"CA","ZipCd":"90001"},"Email":"jane@example.com"},"NECFormData":{"B1NEC":5000.0,"B2IsDirectSales":false,"B3EPP":0.0,"B4FedTaxWH":0.0,"IsFATCA":false,"Is2ndTINnot":false}}]}'

printf "\n${YELLOW}[TEST] 1099-NEC: create${NC}\n"
CREATE_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/taxbandits/form1099nec/create" -H "Content-Type: application/json" -d "$CREATE_PAYLOAD" 2>/dev/null || echo -e "\n000")
HTTP_CODE=$(echo "$CREATE_RESP" | tail -n1)
CREATE_BODY=$(echo "$CREATE_RESP" | sed '$d')

SUBMISSION_ID=$(echo "$CREATE_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('SubmissionId',''))" 2>/dev/null || echo "")
BUSINESS_ID=$(echo "$CREATE_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('BusinessId',''))" 2>/dev/null || echo "")
RECORD_ID=$(echo "$CREATE_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); r=d.get('Form1099Records',{}); s=r.get('SuccessRecords',[]); print(s[0].get('RecordId','') if s else '')" 2>/dev/null || echo "")
RECIPIENT_ID=$(echo "$CREATE_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); r=d.get('Form1099Records',{}); s=r.get('SuccessRecords',[]); print(s[0].get('RecipientId','') if s else '')" 2>/dev/null || echo "")

if [[ "$HTTP_CODE" =~ ^2[0-9][0-9]$ ]] && [ -n "$SUBMISSION_ID" ]; then
    echo -e "${GREEN}  ✓ PASS${NC} (HTTP $HTTP_CODE) SubmissionId=$SUBMISSION_ID"
    PASSED=$((PASSED + 1))
else
    echo -e "${RED}  ✗ FAIL${NC} (HTTP $HTTP_CODE)"
    echo "$CREATE_BODY" | head -c 800
    FAILED=$((FAILED + 1))
fi

# 4-10. APIs that need Create IDs
if [ -n "$SUBMISSION_ID" ]; then
    run_test "1099-NEC: validate (by submissionId)" "GET" "$BASE_URL/api/taxbandits/form1099nec/validate?submissionId=$SUBMISSION_ID" ""
    run_test "1099-NEC: status" "GET" "$BASE_URL/api/taxbandits/form1099nec/status?submissionId=$SUBMISSION_ID" ""
fi

if [ -n "$SUBMISSION_ID" ] && [ -n "$RECORD_ID" ]; then
    run_test "1099-NEC: get" "GET" "$BASE_URL/api/taxbandits/form1099nec/get?submissionId=$SUBMISSION_ID&recordId=$RECORD_ID" ""
fi

if [ -n "$SUBMISSION_ID" ] && [ -n "$BUSINESS_ID" ]; then
    run_test "1099-NEC: list" "GET" "$BASE_URL/api/taxbandits/form1099nec/list?submissionId=$SUBMISSION_ID&businessId=$BUSINESS_ID" ""
fi

if [ -n "$SUBMISSION_ID" ] && [ -n "$RECORD_ID" ]; then
    run_test "1099-NEC: requestdraftpdfurl" "POST" "$BASE_URL/api/taxbandits/form1099nec/requestdraftpdfurl" "{\"RecordId\":\"$RECORD_ID\"}"
    run_test "1099-NEC: getpdf" "GET" "$BASE_URL/api/taxbandits/form1099nec/getpdf?submissionId=$SUBMISSION_ID&recordId=$RECORD_ID" ""
fi

# Update (needs full payload with SubmissionManifest)
if [ -n "$SUBMISSION_ID" ] && [ -n "$RECORD_ID" ] && [ -n "$BUSINESS_ID" ]; then
    UPDATE_PAYLOAD="{\"SubmissionId\":\"$SUBMISSION_ID\",\"RecordId\":\"$RECORD_ID\",\"SubmissionManifest\":{\"TaxYear\":\"2025\",\"IRSFilingType\":\"IRIS\",\"IsFederalFiling\":true,\"IsStateFiling\":false,\"IsPostal\":false,\"IsOnlineAccess\":true,\"IsScheduleFiling\":false},\"ReturnHeader\":{\"Business\":{\"BusinessId\":\"$BUSINESS_ID\",\"BusinessNm\":\"Test Company LLC Updated\",\"IsEIN\":true,\"EINorSSN\":\"65-7368245\",\"Email\":\"test@example.com\",\"ContactNm\":\"John Doe\",\"Phone\":\"1234567890\",\"IsBusinessTerminated\":false,\"IsForeign\":false,\"USAddress\":{\"Address1\":\"123 Main St\",\"City\":\"New York\",\"State\":\"NY\",\"ZipCd\":\"10001\"},\"SigningAuthority\":{\"Name\":\"John Doe\",\"Phone\":\"1234567890\",\"BusinessMemberType\":\"PRESIDENT\"}}},\"ReturnData\":[{\"SequenceId\":\"1\",\"RecordId\":\"$RECORD_ID\",\"IsPostal\":false,\"IsOnlineAccess\":true,\"IsForced\":false,\"Recipient\":{\"TINType\":\"EIN\",\"TIN\":\"39-3817572\",\"FirstPayeeNm\":\"Test Recipient LLC\",\"IsForeign\":false,\"USAddress\":{\"Address1\":\"456 Oak Ave\",\"City\":\"Los Angeles\",\"State\":\"CA\",\"ZipCd\":\"90001\"},\"Email\":\"jane@example.com\"},\"NECFormData\":{\"B1NEC\":6000.0,\"B2IsDirectSales\":false,\"B3EPP\":0.0,\"B4FedTaxWH\":0.0,\"IsFATCA\":false,\"Is2ndTINnot\":false}}]}"
    run_test "1099-NEC: update" "PUT" "$BASE_URL/api/taxbandits/form1099nec/update" "$UPDATE_PAYLOAD"
fi

# RequestPdfURLs, GetByRecordIds (RecordIds as [{"RecordId":"uuid"}])
if [ -n "$SUBMISSION_ID" ] && [ -n "$RECORD_ID" ]; then
    run_test "1099-NEC: requestpdfurls" "POST" "$BASE_URL/api/taxbandits/form1099nec/requestpdfurls" \
    "{\"SubmissionId\":\"$SUBMISSION_ID\",\"RecordIds\":[{\"RecordId\":\"$RECORD_ID\"}]}"
    run_test "1099-NEC: getbyrecordids" "POST" "$BASE_URL/api/taxbandits/form1099nec/getbyrecordids" \
    "{\"RecordIds\":[{\"RecordId\":\"$RECORD_ID\"}]}"
fi

# Approve (RecordIds as Guid[]), RequestDistUrl (NEC only - requires transmitted form)
if [ -n "$SUBMISSION_ID" ] && [ -n "$RECORD_ID" ]; then
    run_test "1099-NEC: approve" "POST" "$BASE_URL/api/taxbandits/form1099nec/approve" \
    "{\"SubmissionId\":\"$SUBMISSION_ID\",\"RecordIds\":[\"$RECORD_ID\"]}"
    [ -n "$RECIPIENT_ID" ] && [ -n "$BUSINESS_ID" ] && run_test "1099-NEC: requestdisturl" "POST" "$BASE_URL/api/taxbandits/form1099nec/requestdisturl" \
    "{\"TaxYear\":\"2024\",\"RecordId\":\"$RECORD_ID\",\"Business\":{\"BusinessId\":\"$BUSINESS_ID\"},\"Recipient\":{\"RecipientId\":\"$RECIPIENT_ID\"}}"
fi

# Transmit & Delete - DESTRUCTIVE (set INCLUDE_DESTRUCTIVE=1 to run)
if [ "$INCLUDE_DESTRUCTIVE" = "1" ] && [ -n "$SUBMISSION_ID" ] && [ -n "$RECORD_ID" ]; then
    run_test "1099-NEC: transmit" "POST" "$BASE_URL/api/taxbandits/form1099nec/transmit" "{\"SubmissionId\":\"$SUBMISSION_ID\"}"
    run_test "1099-NEC: delete" "DELETE" "$BASE_URL/api/taxbandits/form1099nec/delete?submissionId=$SUBMISSION_ID&recordId=$RECORD_ID" ""
fi

# -----------------------------------------------------------------------------
# FORM 1099-MISC (full flow)
# -----------------------------------------------------------------------------
echo -e "\n${YELLOW}===== FORM 1099-MISC =====${NC}"

run_test "1099-MISC: validateform" "POST" "$BASE_URL/api/taxbandits/form1099misc/validateform" \
'{"SubmissionManifest":{"TaxYear":"2025","IRSFilingType":"IRIS","IsFederalFiling":true,"IsStateFiling":false,"IsPostal":false,"IsOnlineAccess":false,"IsScheduleFiling":false},"ReturnHeader":{"Business":{"BusinessNm":"Test Co","IsEIN":true,"EINorSSN":"65-7368245","Email":"test@example.com","Phone":"1234567890","IsBusinessTerminated":false,"IsForeign":false,"USAddress":{"Address1":"123 Main St","City":"NYC","State":"NY","ZipCd":"10001"}}},"ReturnData":[{"SequenceId":"1","IsForced":false,"Recipient":{"TINType":"EIN","TIN":"39-3817572","FirstPayeeNm":"Test Recipient LLC","IsForeign":false,"USAddress":{"Address1":"456 Oak","City":"LA","State":"CA","ZipCd":"90001"}},"MISCFormData":{"B1Rents":1000.0,"B2Royalties":0.0,"B3OtherIncome":0.0,"B4FedTaxWH":0.0,"B5FishingBoatProceeds":0.0,"B6MedicalHealthCare":0.0,"B7NonemployeeComp":0.0,"B8SubstitutePayments":0.0,"B9DirectSales":false,"B10CropInsurance":0.0,"IsFATCA":false,"Is2ndTINnot":false}}]}'

MISC_CREATE='{"SubmissionManifest":{"TaxYear":"2025","IRSFilingType":"IRIS","IsFederalFiling":true,"IsStateFiling":false,"IsPostal":false,"IsOnlineAccess":true,"IsScheduleFiling":false},"ReturnHeader":{"Business":{"BusinessNm":"Test Company LLC","IsEIN":true,"EINorSSN":"65-7368245","Email":"test@example.com","ContactNm":"John Doe","Phone":"1234567890","IsBusinessTerminated":false,"IsForeign":false,"USAddress":{"Address1":"123 Main St","City":"New York","State":"NY","ZipCd":"10001"},"SigningAuthority":{"Name":"John Doe","Phone":"1234567890","BusinessMemberType":"PRESIDENT"}}},"ReturnData":[{"SequenceId":"1","IsPostal":false,"IsOnlineAccess":true,"IsForced":true,"Recipient":{"TINType":"EIN","TIN":"39-3817572","FirstPayeeNm":"Test Recipient LLC","IsForeign":false,"USAddress":{"Address1":"456 Oak Ave","City":"Los Angeles","State":"CA","ZipCd":"90001"},"Email":"jane@example.com"},"MISCFormData":{"B1Rents":5000.0,"B2Royalties":0.0,"B3OtherIncome":0.0,"B4FedTaxWH":0.0,"B5FishingBoatProceeds":0.0,"B6MedicalHealthCare":0.0,"B7NonemployeeComp":0.0,"B8SubstitutePayments":0.0,"B9DirectSales":false,"B10CropInsurance":0.0,"IsFATCA":false,"Is2ndTINnot":false}}]}'

printf "\n${YELLOW}[TEST] 1099-MISC: create${NC}\n"
MISC_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/taxbandits/form1099misc/create" -H "Content-Type: application/json" -d "$MISC_CREATE" 2>/dev/null || echo -e "\n000")
MISC_HTTP=$(echo "$MISC_RESP" | tail -n1)
MISC_BODY=$(echo "$MISC_RESP" | sed '$d')
MISC_SUB=$(echo "$MISC_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('SubmissionId',''))" 2>/dev/null || echo "")
MISC_BIZ=$(echo "$MISC_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('BusinessId',''))" 2>/dev/null || echo "")
MISC_REC=$(echo "$MISC_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); r=d.get('Form1099Records',{}); s=r.get('SuccessRecords',[]); print(s[0].get('RecordId','') if s else '')" 2>/dev/null || echo "")
MISC_RECIP=$(echo "$MISC_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); r=d.get('Form1099Records',{}); s=r.get('SuccessRecords',[]); print(s[0].get('RecipientId','') if s else '')" 2>/dev/null || echo "")

if [[ "$MISC_HTTP" =~ ^2[0-9][0-9]$ ]] && [ -n "$MISC_SUB" ]; then
    echo -e "${GREEN}  ✓ PASS${NC} (HTTP $MISC_HTTP)"
    PASSED=$((PASSED + 1))
    [ -n "$MISC_SUB" ] && run_test "1099-MISC: validate" "GET" "$BASE_URL/api/taxbandits/form1099misc/validate?submissionId=$MISC_SUB" ""
    [ -n "$MISC_SUB" ] && run_test "1099-MISC: status" "GET" "$BASE_URL/api/taxbandits/form1099misc/status?submissionId=$MISC_SUB" ""
    [ -n "$MISC_SUB" ] && [ -n "$MISC_REC" ] && run_test "1099-MISC: get" "GET" "$BASE_URL/api/taxbandits/form1099misc/get?submissionId=$MISC_SUB&recordId=$MISC_REC" ""
    [ -n "$MISC_SUB" ] && [ -n "$MISC_BIZ" ] && run_test "1099-MISC: list" "GET" "$BASE_URL/api/taxbandits/form1099misc/list?submissionId=$MISC_SUB&businessId=$MISC_BIZ" ""
    [ -n "$MISC_SUB" ] && [ -n "$MISC_REC" ] && run_test "1099-MISC: requestdraftpdfurl" "POST" "$BASE_URL/api/taxbandits/form1099misc/requestdraftpdfurl" "{\"RecordId\":\"$MISC_REC\"}"
    [ -n "$MISC_SUB" ] && [ -n "$MISC_REC" ] && run_test "1099-MISC: getpdf" "GET" "$BASE_URL/api/taxbandits/form1099misc/getpdf?submissionId=$MISC_SUB&recordId=$MISC_REC" ""
    [ -n "$MISC_SUB" ] && [ -n "$MISC_REC" ] && run_test "1099-MISC: requestpdfurls" "POST" "$BASE_URL/api/taxbandits/form1099misc/requestpdfurls" "{\"SubmissionId\":\"$MISC_SUB\",\"RecordIds\":[{\"RecordId\":\"$MISC_REC\"}]}"
    [ -n "$MISC_SUB" ] && [ -n "$MISC_REC" ] && run_test "1099-MISC: getbyrecordids" "POST" "$BASE_URL/api/taxbandits/form1099misc/getbyrecordids" "{\"RecordIds\":[{\"RecordId\":\"$MISC_REC\"}]}"
    [ -n "$MISC_SUB" ] && [ -n "$MISC_REC" ] && run_test "1099-MISC: approve" "POST" "$BASE_URL/api/taxbandits/form1099misc/approve" "{\"SubmissionId\":\"$MISC_SUB\",\"RecordIds\":[\"$MISC_REC\"]}"
else
    echo -e "${RED}  ✗ FAIL${NC} (HTTP $MISC_HTTP)"
    echo "$MISC_BODY" | head -c 800
    FAILED=$((FAILED + 1))
fi

# -----------------------------------------------------------------------------
# FORM 1099-K (full flow)
# -----------------------------------------------------------------------------
echo -e "\n${YELLOW}===== FORM 1099-K =====${NC}"

run_test "1099-K: validateform" "POST" "$BASE_URL/api/taxbandits/form1099k/validateform" \
'{"SubmissionManifest":{"TaxYear":"2025","IRSFilingType":"IRIS","IsFederalFiling":true,"IsStateFiling":false,"IsPostal":false,"IsOnlineAccess":false,"IsScheduleFiling":false},"ReturnHeader":{"Business":{"BusinessNm":"Test Co","FirstNm":"John","LastNm":"Doe","IsEIN":true,"EINorSSN":"65-7368245","ContactNm":"John Doe","Email":"test@example.com","Phone":"1234567890","BusinessType":"ESTE","IsBusinessTerminated":false,"IsForeign":false,"USAddress":{"Address1":"123 Main St","City":"NYC","State":"NY","ZipCd":"10001"},"SigningAuthority":{"Name":"John Doe","Phone":"1234567890","BusinessMemberType":"ADMINISTRATOR"}}},"ReturnData":[{"SequenceId":"1","IsForced":false,"Recipient":{"TINType":"EIN","TIN":"39-3817572","FirstPayeeNm":"Test Recipient LLC","IsForeign":false,"USAddress":{"Address1":"456 Oak","City":"LA","State":"CA","ZipCd":"90001"}},"KFormData":{"B1aGrossAmt":5000.0,"B1bCardNotPresentTxns":0.0,"B2MerchantCd":"5411","B3NumPymtTxns":10,"FilerIndicator":"PSE","IndicateTxnsReported":"Payment_Card","Is2ndTINnot":false}}]}'

K_CREATE='{"SubmissionManifest":{"TaxYear":"2025","IRSFilingType":"IRIS","IsFederalFiling":true,"IsStateFiling":false,"IsPostal":false,"IsOnlineAccess":true,"IsScheduleFiling":false},"ReturnHeader":{"Business":{"BusinessNm":"Test Company LLC","FirstNm":"John","LastNm":"Doe","IsEIN":true,"EINorSSN":"65-7368245","ContactNm":"John Doe","Email":"test@example.com","Phone":"1234567890","BusinessType":"ESTE","IsBusinessTerminated":false,"IsForeign":false,"USAddress":{"Address1":"123 Main St","City":"New York","State":"NY","ZipCd":"10001"},"SigningAuthority":{"Name":"John Doe","Phone":"1234567890","BusinessMemberType":"ADMINISTRATOR"}}},"ReturnData":[{"SequenceId":"1","IsForced":true,"Recipient":{"TINType":"EIN","TIN":"39-3817572","FirstPayeeNm":"Test Recipient LLC","IsForeign":false,"USAddress":{"Address1":"456 Oak Ave","City":"Los Angeles","State":"CA","ZipCd":"90001"},"Email":"jane@example.com"},"KFormData":{"B1aGrossAmt":5000.0,"B1bCardNotPresentTxns":0.0,"B2MerchantCd":"5411","B3NumPymtTxns":10,"FilerIndicator":"PSE","IndicateTxnsReported":"Payment_Card","Is2ndTINnot":false}}]}'

printf "\n${YELLOW}[TEST] 1099-K: create${NC}\n"
K_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/taxbandits/form1099k/create" -H "Content-Type: application/json" -d "$K_CREATE" 2>/dev/null || echo -e "\n000")
K_HTTP=$(echo "$K_RESP" | tail -n1)
K_BODY=$(echo "$K_RESP" | sed '$d')
K_SUB=$(echo "$K_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('SubmissionId',''))" 2>/dev/null || echo "")
K_BIZ=$(echo "$K_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('BusinessId',''))" 2>/dev/null || echo "")
K_REC=$(echo "$K_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); r=d.get('Form1099Records',{}); s=r.get('SuccessRecords',[]); print(s[0].get('RecordId','') if s else '')" 2>/dev/null || echo "")
K_RECIP=$(echo "$K_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); r=d.get('Form1099Records',{}); s=r.get('SuccessRecords',[]); print(s[0].get('RecipientId','') if s else '')" 2>/dev/null || echo "")

if [[ "$K_HTTP" =~ ^2[0-9][0-9]$ ]] && [ -n "$K_SUB" ]; then
    echo -e "${GREEN}  ✓ PASS${NC} (HTTP $K_HTTP)"
    PASSED=$((PASSED + 1))
    [ -n "$K_SUB" ] && run_test "1099-K: validate" "GET" "$BASE_URL/api/taxbandits/form1099k/validate?submissionId=$K_SUB" ""
    [ -n "$K_SUB" ] && run_test "1099-K: status" "GET" "$BASE_URL/api/taxbandits/form1099k/status?submissionId=$K_SUB" ""
    [ -n "$K_SUB" ] && [ -n "$K_REC" ] && run_test "1099-K: get" "GET" "$BASE_URL/api/taxbandits/form1099k/get?submissionId=$K_SUB&recordId=$K_REC" ""
    [ -n "$K_SUB" ] && [ -n "$K_BIZ" ] && run_test "1099-K: list" "GET" "$BASE_URL/api/taxbandits/form1099k/list?submissionId=$K_SUB&businessId=$K_BIZ" ""
    [ -n "$K_SUB" ] && [ -n "$K_REC" ] && run_test "1099-K: requestdraftpdfurl" "POST" "$BASE_URL/api/taxbandits/form1099k/requestdraftpdfurl" "{\"RecordId\":\"$K_REC\"}"
    [ -n "$K_SUB" ] && [ -n "$K_REC" ] && run_test "1099-K: getpdf" "GET" "$BASE_URL/api/taxbandits/form1099k/getpdf?submissionId=$K_SUB&recordId=$K_REC" ""
    [ -n "$K_SUB" ] && [ -n "$K_REC" ] && run_test "1099-K: requestpdfurls" "POST" "$BASE_URL/api/taxbandits/form1099k/requestpdfurls" "{\"SubmissionId\":\"$K_SUB\",\"RecordIds\":[{\"RecordId\":\"$K_REC\"}]}"
    [ -n "$K_SUB" ] && [ -n "$K_REC" ] && run_test "1099-K: getbyrecordids" "POST" "$BASE_URL/api/taxbandits/form1099k/getbyrecordids" "{\"RecordIds\":[{\"RecordId\":\"$K_REC\"}]}"
    [ -n "$K_SUB" ] && [ -n "$K_REC" ] && run_test "1099-K: approve" "POST" "$BASE_URL/api/taxbandits/form1099k/approve" "{\"SubmissionId\":\"$K_SUB\",\"RecordIds\":[\"$K_REC\"]}"
else
    echo -e "${RED}  ✗ FAIL${NC} (HTTP $K_HTTP)"
    echo "$K_BODY" | head -c 800
    FAILED=$((FAILED + 1))
fi

# -----------------------------------------------------------------------------
# Summary
# -----------------------------------------------------------------------------
echo -e "\n=============================================="
echo -e "  Summary - Total APIs tested"
echo -e "  ${GREEN}Passed: $PASSED${NC}  ${RED}Failed: $FAILED${NC}"
echo "=============================================="
