package com.tidewater.freight.validation;

import com.tidewater.freight.ShipmentRequest;

import java.util.Optional;

public final class ZoneRequiredRule implements ShipmentRule {

    @Override
    public Optional<ValidationError> check(ShipmentRequest request) {
        String zone = request.zone();
        return zone == null || zone.isBlank()
                ? Optional.of(ValidationError.ZONE_REQUIRED)
                : Optional.empty();
    }
}
