package com.pietrozanetti.evaluation.controller;

import com.pietrozanetti.evaluation.dto.FlagDto;
import com.pietrozanetti.evaluation.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/flags")
@RequiredArgsConstructor
public class InternalFlagController {

    private final EvaluationService evaluationService;

    @PostMapping
    public void updateFlag(@RequestBody FlagDto flag) {
        evaluationService.updateFlag(flag);
    }
}
