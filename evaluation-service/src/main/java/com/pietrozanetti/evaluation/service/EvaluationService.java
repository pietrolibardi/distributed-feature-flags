package com.pietrozanetti.evaluation.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.pietrozanetti.evaluation.client.FlagClient;
import com.pietrozanetti.evaluation.client.FlagResponse;
import com.pietrozanetti.evaluation.domain.Flag;
import com.pietrozanetti.evaluation.dto.FlagDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final FlagClient flagClient;
    private final Cache<String, Object> flagCache;

    public void updateFlag(FlagDto flagDto) {
        Flag flag = new Flag();
        flag.setName(flagDto.getName());
        flag.setEnabled(flagDto.isEnabled());
        flag.setRolloutPercentage(flagDto.getRolloutPercentage());

        flagCache.put(flag.getName(), flag);
    }

    public boolean evaluate(String flagName, String userId, String authorization) {

        FlagResponse flag = flagClient.getFlag(flagName, authorization);

        if (flag == null || !flag.isEnabled()) {
            return false;
        }

        int bucket = Math.abs(userId.hashCode()) % 100;

        return bucket < flag.getRolloutPercentage();
    }
}
