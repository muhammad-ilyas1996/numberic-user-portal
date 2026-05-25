# LLC Formation — State catalog (Postman)

**Login required** — har request par `Authorization: Bearer {{token}}`.

## 1. Login

```
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
  "username": "your-username",
  "password": "your-password"
}
```

Response se `token` save karo (Postman variable `token`).

## 2. SQL migration (once)

`scripts/create_llc_formation_state_catalog.sql`

## 3. Seed data

### A) Bina body

```
POST {{baseUrl}}/api/llc-northwest/llc-formation/states/bulk-seed-defaults
Authorization: Bearer {{token}}
```

### B) Full JSON body

```
POST {{baseUrl}}/api/llc-northwest/llc-formation/states/bulk-seed
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body: `postman/llc-formation-states-bulk-seed.json` (poori file paste)

## 4. GET states

```
GET {{baseUrl}}/api/llc-northwest/llc-formation/states
Authorization: Bearer {{token}}
```

```
GET {{baseUrl}}/api/llc-northwest/llc-formation/states/TX
Authorization: Bearer {{token}}
```
