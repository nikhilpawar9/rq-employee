package com.reliaquest.api.client;

import com.reliaquest.api.exception.RateLimitException;
import com.reliaquest.api.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeApiClient {

    private final RestTemplate restTemplate;

    @Value("${employee.api.base-url}")
    private String baseUrl;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    public <T, R> T execute(
            String path, HttpMethod method, R body, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        int attempt = 1;
        while (attempt <= MAX_RETRIES) {
            try {
                HttpEntity<R> request = body != null ? new HttpEntity<>(body) : null;
                ResponseEntity<ApiResponse<T>> response =
                        restTemplate.exchange(baseUrl + path, method, request, responseType);

                if (response.getBody() != null) {
                    return response.getBody().getData();
                }
                return null;
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt >= MAX_RETRIES) {
                    log.error("Max retries reached, still getting rate limited");
                    throw new RateLimitException("Service unavailable due to rate limiting", e);
                }
                log.warn("Got rate limited, retrying... attempt {}/{}", attempt, MAX_RETRIES);
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
                attempt++;
            }
        }
        throw new RuntimeException("Failed after retries");
    }
}
