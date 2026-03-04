package com.pietrozanetti.flag.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureFlag {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String name;

    private boolean enabled;

    private int rolloutPercentage;
}
