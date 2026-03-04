package com.pietrozanetti.flag.service;

import com.pietrozanetti.flag.domain.FeatureFlag;
import com.pietrozanetti.flag.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final FeatureFlagRepository repository;

    public FeatureFlag create(FeatureFlag flag) {
        return repository.save(flag);
    }

    public List<FeatureFlag> findAll() {
        return repository.findAll();
    }
}
