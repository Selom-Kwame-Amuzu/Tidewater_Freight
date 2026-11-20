package com.tidewater.freight.validation;

import com.tidewater.freight.ShipmentRequest;

import java.util.Optional;

public final class WeightRule implements ShipmentRule {

    @Override
    public Optional<ValidationError> check(ShipmentRequest request) {
        double weight = request.weightKg();
        return !Double.isFinite(weight) || weight <= 0
                ? Optional.of(ValidationError.WEIGHT_INVALID)
                : Optional.empty();
    }
}
