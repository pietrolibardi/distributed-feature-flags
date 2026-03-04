package com.pietrozanetti.evaluation.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.pietrozanetti.evaluation.domain.Flag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class FlagClient {

    private final Cache<String, Object> flagCache;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${flag-service-url}")
    private String flagServiceUrl;

    public FlagResponse getFlag(String name, String authorization) {

        Object cached = flagCache.getIfPresent(name);

        if (cached != null) {
            if (cached instanceof Flag flag) {
                return toFlagResponse(flag);
            }
            return (FlagResponse) cached;
        }

        HttpHeaders headers = new HttpHeaders();
        if (authorization != null && !authorization.isBlank()) {
            headers.set("Authorization", authorization);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        // Sem token: chamada server-to-server usa endpoint interno (não exige JWT)
        String path = (authorization != null && !authorization.isBlank())
                ? "/flags/" + name
                : "/internal/flags/" + name;
        try {
            ResponseEntity<FlagResponse> response = restTemplate.exchange(
                    flagServiceUrl + path,
                    HttpMethod.GET,
                    entity,
                    FlagResponse.class
            );
            FlagResponse flag = response.getBody();
            if (flag != null) {
                flagCache.put(name, flag);
            }
            return flag;
        } catch (HttpClientErrorException.NotFound e) {
            return null; // flag não existe
        }
    }

    public void invalidate(String name) {
        flagCache.invalidate(name);
    }

    private static FlagResponse toFlagResponse(Flag flag) {
        FlagResponse response = new FlagResponse();
        response.setName(flag.getName());
        response.setEnabled(flag.isEnabled());
        response.setRolloutPercentage(flag.getRolloutPercentage());
        return response;
    }
}
