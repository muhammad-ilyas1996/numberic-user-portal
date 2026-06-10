# LLC Formation — Full Northwest Flow (Postman)

## Import

1. Postman → **Import** → `postman/LLC_Formation_Northwest_Flow.postman_collection.json`
2. Collection variables check karo:
   - `baseUrl` = `http://localhost:8081/numbricsservice` (local)
   - Production: `https://www.numbrics.ai/numbricsservice`

## Run order

| # | Request | Notes |
|---|---------|--------|
| — | **Auth → Login** | Sets `{{token}}` — username/password edit karo |
| — | **Setup → bulk-seed-defaults** | Sirf ek dafa |
| 01 | Create draft | Sets `{{formationId}}` |
| 02–05 | Steps 1–4 | State, name, owner, OWN agent |
| 06 | Northwest prepare | Sets `companyId`, filing IDs — **no body** |
| 07 | GET formation | Auto-tests DB fields |
| 08 | filing-methods | NW live company check |
| 09–10 | Payment (optional) | Stripe intent |
| 11 | submit-after-payment | Local dev checkout |
| 12 | GET formation final | `SUBMITTED` + `northwest_checkout_complete` |

## State change

Collection variables:
- `jurisdictionCode` = `TX`, `WY`, `DE`, etc.
- Step4 agent `state` must match `jurisdictionCode`
- `jurisdictionFull` auto-set after prepare (e.g. `Wyoming`)

## Collection Runner

Postman → Collection → **Run** → select all requests in order (or 01–12 after Login + Setup done once).

## SQL (once)

`scripts/create_llc_formation_state_catalog.sql` if table missing.

## Troubleshooting

| Error | Fix |
|-------|-----|
| 401 | Re-run Login |
| Unknown state code | Run bulk-seed-defaults |
| INVALID_ENTITY_TYPE | App restart — entity type fix in code |
| Duplicate company name | Step 03 auto-unique name; re-run 03 + 06 |
| NW network error | Check internet / `api.corporatetools.com` DNS |
