# LLC Formation — Diagnostic Postman Flow

## Import

Postman → **Import** → `postman/LLC_Formation_Diagnostic_Flow.postman_collection.json`

## Variables

| Variable | Default |
|----------|---------|
| `baseUrl` | `http://localhost:8081/numbricsservice` |
| `jurisdictionCode` | `WY` |

Update **Login** username/password before running.

## Run order

1. **00 Login** → sets `{{token}}`
2. **01 Setup** (once) → bulk-seed states
3. **02–06** → wizard steps 1–4
4. **07 prepare** → NW company + rich cart JSON
5. **08 DIAGNOSTIC** → must pass: `registered_agent`, `principal_address`, `members`
6. **09–10** → NW filing-methods + schema
7. **11–12** optional Stripe
8. **13 submit-after-payment** → **required on localhost**
9. **14–15** → final verify

## Pass criteria

| Step | Expected |
|------|----------|
| 08 | `form_data` has `registered_agent`, `company_mailing_address.line1`, `official.manager` |
| 13 | `status=SUBMITTED`, `filingStatus=northwest_checkout_complete` |
| NW portal | After 13, refresh NW — addresses/agent/officials should populate |

## Live production

- Use step **12** payment intent + Stripe webhook at `https://www.numbrics.ai/numbricsservice/webhooks/stripe`
- Step 13 only for local dev when webhook unavailable
