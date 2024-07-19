## Create Invoice

### Endpoint

POST -> https://app.sandbox.midtrans.com/snap/v1/transactions

### Response Body

```json
{
  "transaction_details": {
    "order_id": "t-2",
    "gross_amount": 10000
  },
  "customer_details": {
    "first_name": "Levi",
    "email": "levi@example.com",
    "phone": "085156231354"
  },
  "page_expiry": {
    "duration": 5,
    "unit": "minutes"
  },
  "enabled_payments": [
    "credit_card"
  ]
}
```

### Header

Authorization Basic {ServerKey}

## Check Payment Status

### Endpoint

GET -> https://api.sandbox.midtrans.com/v2/{order_id}/status


