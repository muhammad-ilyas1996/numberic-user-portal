#!/bin/bash
# Test Create Form 1099-NEC with verbose output

BASE="http://localhost:8081/numbricsservice/api/taxbandits"

echo "=== Step 1: Testing Create Form 1099-NEC (Verbose) ==="
echo ""

# First check HTTP status and raw response
RESPONSE=$(curl -v -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE/form1099nec/create" \
  -H "Content-Type: application/json" \
  -d '{
    "SubmissionManifest": {
      "TaxYear": "2024",
      "IRSFilingType": "FIRE",
      "IsFederalFiling": true,
      "IsStateFiling": false,
      "IsPostal": false,
      "IsOnlineAccess": true,
      "IsScheduleFiling": false
    },
    "ReturnHeader": {
      "Business": {
        "BusinessNm": "Test Company Inc",
        "IsEIN": true,
        "EINorSSN": "12-3456789",
        "Email": "test@example.com",
        "ContactNm": "John Doe",
        "Phone": "555-123-4567",
        "IsForeign": false,
        "USAddress": {
          "Address1": "123 Main St",
          "City": "New York",
          "State": "NY",
          "ZipCd": "10001"
        },
        "SigningAuthority": {
          "Name": "John Doe",
          "Phone": "555-123-4567",
          "BusinessMemberType": "CEO"
        }
      }
    },
    "ReturnData": [{
      "SequenceId": "1",
      "IsPostal": false,
      "IsOnlineAccess": true,
      "Recipient": {
        "TINType": "SSN",
        "TIN": "123-45-6789",
        "FirstPayeeNm": "Jane",
        "SecondPayeeNm": "Smith",
        "IsForeign": false,
        "USAddress": {
          "Address1": "456 Oak Ave",
          "City": "Los Angeles",
          "State": "CA",
          "ZipCd": "90001"
        },
        "Email": "jane.smith@example.com"
      },
      "NECFormData": {
        "B1NEC": 5000.00,
        "B2IsDirectSales": false,
        "B4FedTaxWH": 0.00,
        "IsFATCA": false,
        "Is2ndTINnot": false
      }
    }]
  }' 2>&1)

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | tail -1 | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE/d' | grep -A 100 "< HTTP" | tail -n +2)

echo "HTTP Status Code: $HTTP_CODE"
echo ""
echo "Response Headers:"
echo "$RESPONSE" | grep "< HTTP" | head -1
echo "$RESPONSE" | grep "< " | grep -v "< HTTP"
echo ""
echo "Response Body:"
echo "$BODY"
echo ""

if [ ! -z "$BODY" ]; then
    echo "Trying to parse as JSON:"
    echo "$BODY" | python3 -m json.tool 2>&1 || echo "Not valid JSON"
fi

