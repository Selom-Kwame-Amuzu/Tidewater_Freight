package com.tidewater.freight.pricing;

import com.tidewater.freight.ServiceLevel;
import com.tidewater.freight.ShipmentRequest;
import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.validation.ValidationError;

import java.util.List;

public final class ServiceLevelMultiplier implements CostModifier {

    @Override
    public double apply(double cost, ShipmentRequest request) {
        ServiceLevel level = ServiceLevel.from(request.serviceLevel())
                .orElseThrow(() -> new InvalidShipmentException(
                        List.of(ValidationError.SERVICE_LEVEL_INVALID)));
        return cost * level.multiplier();
    }
}
