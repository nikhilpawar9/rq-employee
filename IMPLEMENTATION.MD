# Implementation Notes

## Overview

This is my solution for the ReliaQuest Employee API challenge. Built a REST API wrapper around the mock employee service.

## How It Works

The API has 7 endpoints that talk to the mock service at localhost:8112. I split things into models, a client for HTTP calls, service layer for business logic, and controllers for the REST endpoints.

The mock API randomly returns 429 errors. I added retry logic that tries up to 3 times with increasing delays before giving up.

## Structure

```
api/src/main/java/com/reliaquest/api/
├── model/              # Employee, EmployeeInput, ApiResponse
├── client/             # EmployeeApiClient (handles HTTP + retries)
├── service/            # EmployeeService (business logic)
├── controller/         # EmployeeController (REST endpoints)
├── exception/          # Custom exceptions + global handler
└── config/             # RestTemplate setup
```

## Implementation Notes

**Rate Limiting**: Catches 429 errors and retries with exponential backoff (1s, 2s, 3s delays). After 3 attempts, throws RateLimitException.

**Delete by ID**: The mock API's delete endpoint needs the employee name, not ID. So I fetch the employee first to get the name, then delete.

**Name Search**: Fetches all employees and filters in memory. Works fine for this dataset size.

**Validation**: Added Jakarta validation - names/titles can't be blank, salary must be positive, age between 18-60

**Testing**: 15 unit tests using Mockito. 



## If This Was Production

I will add caching, use a circuit breaker instead of simple retries, add proper monitoring, and probably use WebClient instead of RestTemplate. 
Also pagination for large datasets and integration tests.
