package com.pietrozanetti.flag.service;

import com.pietrozanetti.flag.domain.FeatureFlag;
import com.pietrozanetti.flag.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final FeatureFlagRepository repository;
    private final RestTemplate restTemplate;

    @Value("${evaluation-service-url}")
    private String evaluationServiceUrl;

    public FeatureFlag create(FeatureFlag flag) {
        FeatureFlag saved = repository.save(flag);
        restTemplate.postForObject(
                evaluationServiceUrl + "/internal/flags",
                saved,
                Void.class
        );
        return saved;
    }

    public List<FeatureFlag> findAll() {
        return repository.findAll();
    }

    public Optional<FeatureFlag> findByName(String name) {
        return repository.findByName(name);
    }
}
