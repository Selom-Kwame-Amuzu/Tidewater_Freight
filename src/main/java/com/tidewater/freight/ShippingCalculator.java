package com.tidewater.freight;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.pricing.CostModifier;
import com.tidewater.freight.pricing.ZonePolicy;
import com.tidewater.freight.pricing.ZonePolicyProvider;
import com.tidewater.freight.validation.ShipmentValidator;
import com.tidewater.freight.validation.ValidationError;

import java.util.List;
import java.util.Objects;

public final class ShippingCalculator {

    private final ZonePolicyProvider policies;
    private final ShipmentValidator validator;
    private final List<CostModifier> modifiers;

    public ShippingCalculator(ZonePolicyProvider policies, ShipmentValidator validator,
                              List<CostModifier> modifiers) {
        this.policies = Objects.requireNonNull(policies, "policies");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.modifiers = List.copyOf(modifiers);
    }

    public double calculateCost(ShipmentRequest request) {
        Objects.requireNonNull(request, "request");

        List<ValidationError> errors = validator.validate(request);
        if (!errors.isEmpty()) {
            throw new InvalidShipmentException(errors);
        }

        ZonePolicy policy = policies.find(request.zone());
        double cost = policy.ratePerKg() * request.weightKg() + policy.handlingFee();

        for (CostModifier modifier : modifiers) {
            cost = modifier.apply(cost, request);
        }

        return cost;
    }
}
