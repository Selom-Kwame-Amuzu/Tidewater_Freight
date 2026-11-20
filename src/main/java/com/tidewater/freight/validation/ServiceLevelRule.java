package com.tidewater.freight.validation;

import com.tidewater.freight.ShipmentRequest;

import java.util.Optional;
import java.util.Set;

public final class ServiceLevelRule implements ShipmentRule {

    private static final Set<String> ALLOWED_LEVELS = Set.of("STANDARD", "EXPRESS");

    @Override
    public Optional<ValidationError> check(ShipmentRequest request) {
        String serviceLevel = request.serviceLevel();
        return serviceLevel == null || !ALLOWED_LEVELS.contains(serviceLevel)
                ? Optional.of(ValidationError.SERVICE_LEVEL_INVALID)
                : Optional.empty();
    }
}
