package org.broscorp.cryptobot.service;

import lombok.RequiredArgsConstructor;
import org.broscorp.cryptobot.dto.ApiResponse;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ApiService {
    protected final RestTemplate restTemplate;

    public ApiResponse getJsonFromApi(String apiUrl) {
        return restTemplate.getForObject(apiUrl, ApiResponse.class);
    }
}
