package com.pietrozanetti.evaluation.dto;

import lombok.Data;

@Data
public class FlagDto {

    private String name;
    private boolean enabled;
    private int rolloutPercentage;
}
