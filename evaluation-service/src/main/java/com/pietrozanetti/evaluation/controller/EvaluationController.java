package com.pietrozanetti.evaluation.controller;

import com.pietrozanetti.evaluation.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/evaluate")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @GetMapping
    public boolean evaluate(
            @RequestParam String flag,
            @RequestParam String userId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return evaluationService.evaluate(flag, userId, authorization);
    }
}
