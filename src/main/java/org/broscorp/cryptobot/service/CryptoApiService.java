package org.broscorp.cryptobot.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.broscorp.cryptobot.dto.CurrencyDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CryptoApiService {
    private RestTemplate restTemplate;

    @Value("${api.url}")
    private String url;

    @PostConstruct
    private void init() {
        restTemplate = new RestTemplate();
    }

    public List<CurrencyDTO> getListFromApi() {
        ResponseEntity<List<CurrencyDTO>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        return responseEntity.getBody();
    }
}
