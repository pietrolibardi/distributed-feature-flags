package com.pietrozanetti.evaluation.domain;

import lombok.Data;

@Data
public class Flag {

    private String name;
    private boolean enabled;
    private int rolloutPercentage;
}
