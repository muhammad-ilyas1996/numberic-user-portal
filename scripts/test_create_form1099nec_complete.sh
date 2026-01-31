#!/bin/bash
# Complete curl command for creating Form 1099-NEC with all required fields

curl -X POST "http://localhost:8081/numbricsservice/api/taxbandits/form1099nec/create" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
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
        "IsBusinessTerminated": false,
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
      "IsForced": false,
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
        "B3EPP": 0.00,
        "B4FedTaxWH": 0.00,
        "IsFATCA": false,
        "Is2ndTINnot": false
      }
    }]
  }' | python3 -m json.tool

