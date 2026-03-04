package com.pietrozanetti.flag.controller;

import com.pietrozanetti.flag.domain.FeatureFlag;
import com.pietrozanetti.flag.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/flags")
@RequiredArgsConstructor
public class InternalFlagController {

    private final FeatureFlagService service;

    /**
     * Endpoint interno para o evaluation-service obter uma flag por nome sem JWT (chamada server-to-server).
     */
    @GetMapping("/{name}")
    public ResponseEntity<FeatureFlag> getByName(@PathVariable String name) {
        return service.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
