package com.tidewater.freight.validation;

import com.tidewater.freight.ServiceLevel;
import com.tidewater.freight.ShipmentRequest;

import java.util.Optional;

public final class ServiceLevelRule implements ShipmentRule {

    @Override
    public Optional<ValidationError> check(ShipmentRequest request) {
        return ServiceLevel.from(request.serviceLevel()).isPresent()
                ? Optional.empty()
                : Optional.of(ValidationError.SERVICE_LEVEL_INVALID);
    }
}
