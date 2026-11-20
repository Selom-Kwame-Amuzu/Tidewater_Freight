package com.tidewater.freight;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.pricing.ZonePolicy;
import com.tidewater.freight.pricing.ZonePolicyProvider;
import com.tidewater.freight.validation.ShipmentValidator;
import com.tidewater.freight.validation.ValidationError;

import java.util.List;
import java.util.Objects;

public final class ShippingCalculator {

    private static final double HEAVY_WEIGHT_THRESHOLD_KG = 30.0;
    private static final double HEAVY_SURCHARGE = 20.0;
    private static final double EXPRESS_MULTIPLIER = 1.5;

    private final ZonePolicyProvider policies;
    private final ShipmentValidator validator;

    public ShippingCalculator(ZonePolicyProvider policies, ShipmentValidator validator) {
        this.policies = Objects.requireNonNull(policies, "policies");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    public double calculateCost(ShipmentRequest request) {
        Objects.requireNonNull(request, "request");

        List<ValidationError> errors = validator.validate(request);
        if (!errors.isEmpty()) {
            throw new InvalidShipmentException(errors);
        }

        ZonePolicy policy = policies.find(request.zone());
        double cost = policy.ratePerKg() * request.weightKg() + policy.handlingFee();

        if (request.weightKg() > HEAVY_WEIGHT_THRESHOLD_KG) {
            cost += HEAVY_SURCHARGE;
        }

        if (request.serviceLevel().equals("EXPRESS")) {
            cost *= EXPRESS_MULTIPLIER;
        }

        return cost;
    }
}
