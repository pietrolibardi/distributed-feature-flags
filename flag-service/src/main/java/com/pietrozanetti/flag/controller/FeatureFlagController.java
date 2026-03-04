package com.pietrozanetti.flag.controller;

import com.pietrozanetti.flag.domain.FeatureFlag;
import com.pietrozanetti.flag.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flags")
@RequiredArgsConstructor
public class FeatureFlagController {

    private final FeatureFlagService service;

    @PostMapping
    public FeatureFlag create(@RequestBody FeatureFlag flag) {
        return service.create(flag);
    }

    @GetMapping
    public List<FeatureFlag> list() {
        return service.findAll();
    }
}
