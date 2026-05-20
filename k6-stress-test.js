import http from 'k6/http';
import { check, sleep } from 'k6';

/*
=================================================================================
JSON (Jastip Online Nasional) — k6 Stress/Load Testing Script
=================================================================================
This script simulates concurrent checkouts ("War" scenario) to verify the 
concurrency safety and performance of the Jastip checkout system.

How to Run:
1. Install k6 (Windows: 'winget install gnu.k6' or download installer).
2. Start the Spring Boot application (using local profile with active DB).
3. Ensure you have a valid Buyer ID and Product ID in your database.
4. Replace the values of BUYER_ID and PRODUCT_ID in the config below.
5. Run the test command in your terminal:
   k6 run k6-stress-test.js
=================================================================================
*/

// Configuration parameters
const BASE_URL = 'http://localhost:8080';
const BUYER_ID = '33333333-3333-3333-3333-333333333333'; // Replace with a valid user UUID
const PRODUCT_ID = '44444444-4444-4444-4444-444444444444'; // Replace with a valid product UUID

export const options = {
  stages: [
    { duration: '5s', target: 50 },  // Ramp up: ramp up to 50 concurrent virtual users
    { duration: '10s', target: 50 }, // Stress: sustain 50 virtual users making concurrent checkouts
    { duration: '5s', target: 0 },   // Ramp down: scale down VUs to 0
  ],
  thresholds: {
    http_req_failed: ['rate<0.1'],   // Error rate should be less than 10% (under high load, some will fail due to out-of-stock, which is correct)
    http_req_duration: ['p(95)<500'], // 95% of requests should complete within 500ms
  },
};

export default function () {
  const url = `${BASE_URL}/api/orders/checkout`;
  
  const payload = JSON.stringify({
    buyerId: BUYER_ID,
    productId: PRODUCT_ID,
    quantity: 1,
    shippingAddress: 'Jl. Salemba Raya No. 4, Jakarta Pusat',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);

  // We expect:
  // - 201 Created: Stock successfully reserved
  // - 409 Conflict: Out of stock (expected behavior, NOT a failure)
  // - 400 Bad Request: Invalid payload or validation fail
  check(res, {
    'status is 201 or 409': (r) => r.status === 201 || r.status === 409,
    'response time is healthy (< 500ms)': (r) => r.timings.duration < 500,
  });

  // Brief pause between requests to simulate realistic user action
  sleep(0.1);
}
