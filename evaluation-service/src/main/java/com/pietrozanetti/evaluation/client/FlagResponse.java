package com.pietrozanetti.evaluation.client;

import lombok.Data;

@Data
public class FlagResponse {

    private String id;
    private String name;
    private boolean enabled;
    private int rolloutPercentage;
}
