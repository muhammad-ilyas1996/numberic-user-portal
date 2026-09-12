# Postman Collection – Numberics Invoice APIs

## 1. View-detail 404

- **Method:** Must be **POST** (GET will give 404).
- **URL:** `{{baseUrl}}/v1/invoice/view-detail`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body (raw JSON):** `{"id": 2}` (use the invoice ID you want to fetch)

If you get **404**:
- Response body will be: `{"error": "Invoice not found for id: 2"}` → that invoice ID does not exist or is inactive in DB.
- If you get **401** → login again and use the new token.
- If you get **405** → you are using GET; use POST.

---

## 2. WhatsApp message not received (Twilio Sandbox)

Your config uses Twilio’s **WhatsApp Sandbox** number: `whatsapp:+14155238886`.

For the sandbox, **the recipient must join the sandbox once** before they can receive messages:

1. Open **Twilio Console** → **Messaging** → **Try it out** → **Send a WhatsApp message**.
2. You will see the sandbox number and a **“join &lt;code&gt;”** instruction (e.g. “join yellow-tiger”).
3. From the recipient’s WhatsApp (**+923343071241**), send a message to the sandbox number:  
   **join &lt;code&gt;**  
   (e.g. `join yellow-tiger`).
4. After that, send the invoice again from the API; the message should be delivered.

**Check in Twilio:**
- **Logs** → Messaging → see if the message shows “delivered” or any error.
- Server log: after this update, a line like  
  `WhatsApp sent to +923343071241 for invoice 2; Twilio SID: SM...`  
  confirms the API accepted the send; delivery depends on Twilio and opt-in.

**Production (Numbrics WhatsApp sender):** webhook  
`https://www.numbrics.ai/numbricsservice/webhooks/whatsapp` (HTTP POST), config  
`twilio.whatsapp.number=whatsapp:+15559136356`, and `users.phone` must match the sender’s WhatsApp number.  
**No new WhatsApp API** — inbound uses existing webhook; linked users get full Taalr (same as in-app).

---

## 3. Taalr Automation (Receipt + Invoice)

Import `Numberics_Invoice_APIs.postman_collection.json` → folder **Taalr Automation (Receipt + Invoice)**.

### App chat (easiest)

1. **Auth → Login** (sets `{{token}}`)
2. Set collection variables: `testCustomerName`, `testCustomerEmail`, `testInvoiceAmount`
3. Run **`00 Reset Taalr session`**
4. Run **`01 Invoice one-shot`** → reply should show invoice preview
5. Run **`04 Confirm YES — send invoice`**

Receipt OCR without WhatsApp:

1. **`11 Receipt upload`** — pick a JPEG/PNG in form-data → sets `{{receiptId}}`
2. **`13 Receipt save OCR`** — persists to DB
3. **`14 List my receipts`** — verify

### WhatsApp simulate

1. Set `whatsappFrom` = `whatsapp:+YOUR_PHONE` (must match `users.phone` in DB)
2. **`20 WhatsApp webhook — invoice text`**
3. Check WhatsApp for preview reply
4. **`21 WhatsApp webhook — confirm YES`**

### Tips

- Response `"model": "taalr-action"` = automation handled (not general Claude chat)
- `taalr.actions.enabled=false` on server → these requests fall through to normal chat
- Invoice `NO` leaves a DRAFT in DB (expected)
