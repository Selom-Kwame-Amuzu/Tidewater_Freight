package com.tidewater.freight.validation;

import com.tidewater.freight.ShipmentRequest;

import java.util.Optional;

public interface ShipmentRule {
    Optional<ValidationError> check(ShipmentRequest request);
}
