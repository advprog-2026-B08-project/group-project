import { check, sleep } from 'k6';
import http from 'k6/http';

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
const BUYER_ID = 'f9990d45-4a72-4d79-8146-fc6aefc4b294'; // Replace with a valid user UUID
const PRODUCT_ID = 'f3eb8241-e485-4a46-ad09-4104fa8a5385'; // Replace with a valid product UUID

export const options = {
  stages: [
    { duration: '5s', target: 50 },  // Ramp up: ramp up to 50 concurrent virtual users
    { duration: '30s', target: 50 }, // Stress: sustain 50 virtual users making concurrent checkouts
    { duration: '5s', target: 0 },   // Ramp down: scale down VUs to 0
  ],
  thresholds: {
    http_req_failed: ['rate<0.30'],    // Allow up to 30% (400/409 responses are expected, not real errors)
    http_req_duration: ['p(95)<2000'], // 95% of requests should complete within 2s (lock contention expected)
  },
};

// Each Virtual User (VU) will maintain its own logged-in state
let isLoggedIn = false;

export default function () {
  // If this VU is not logged in yet, perform login first
  if (!isLoggedIn) {
    // 1. Get the login page to extract the CSRF token
    const loginPageRes = http.get(`${BASE_URL}/login`);
    
    // Find the hidden CSRF input field value
    const csrfToken = loginPageRes.html().find('input[name="_csrf"]').attr('value');

    // 2. Perform POST request to authenticate (with redirects disabled to capture cookies reliably)
    const loginRes = http.post(`${BASE_URL}/login`, {
      username: 'titiper@gmail.com', // Default seeded buyer email
      password: 'titiper',           // Default seeded password
      _csrf: csrfToken,
    }, {
      redirects: 0,
    });

    const redirectUrl = loginRes.headers['Location'] || '';
    console.log(`[DEBUG] Login Status: ${loginRes.status}, Redirect Location: ${redirectUrl}`);
    
    // Check if we successfully logged in (302 redirect to homepage)
    if (loginRes.status === 302 && redirectUrl.includes('/homepage')) {
      isLoggedIn = true;
    } else {
      isLoggedIn = false;
      console.log(`[WARNING] Login failed for a VU. Retrying in next iteration.`);
      sleep(0.5);
      return; // Skip checkout this iteration and retry login next time
    }
    
    // Wait briefly after logging in
    sleep(0.5);
  }

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

  if (res.status !== 201 && res.status !== 409) {
    console.log(`[DEBUG] Checkout failed. Status: ${res.status}, Body: ${res.body.substring(0, 100)}...`);
    // If we got redirected to the login page (status 200 HTML), we are no longer authenticated
    if (res.status === 200 && res.body.includes('Login')) {
      console.log(`[WARNING] Session lost or expired. Resetting logged-in status.`);
      isLoggedIn = false;
    }
  }

  // We expect:
  // - 201 Created: Stock successfully reserved
  // - 409 Conflict: Out of stock (expected behavior, NOT a failure)
  // - 400 Bad Request: Invalid payload or validation fail (if balance is insufficient)
  check(res, {
    'status is 201, 409, or 400': (r) => r.status === 201 || r.status === 409 || r.status === 400,
    'response time is healthy (< 500ms)': (r) => r.timings.duration < 500,
  });

  // Brief pause between requests to simulate realistic user action
  sleep(0.1);
}
