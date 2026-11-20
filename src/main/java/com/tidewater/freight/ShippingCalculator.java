package com.tidewater.freight;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.validation.ShipmentValidator;
import com.tidewater.freight.validation.ValidationError;

import java.util.List;
import java.util.Objects;

public final class ShippingCalculator {

    private static final double REGIONAL_HANDLING_FEE = 10.0;
    private static final double INTERNATIONAL_CUSTOMS_FEE = 25.0;
    private static final double HEAVY_WEIGHT_THRESHOLD_KG = 30.0;
    private static final double HEAVY_SURCHARGE = 20.0;
    private static final double EXPRESS_MULTIPLIER = 1.5;

    private final RateRepository repository;
    private final ShipmentValidator validator;

    public ShippingCalculator(RateRepository repository, ShipmentValidator validator) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    public double calculateCost(ShipmentRequest request) {
        Objects.requireNonNull(request, "request");

        List<ValidationError> errors = validator.validate(request);
        if (!errors.isEmpty()) {
            throw new InvalidShipmentException(errors);
        }

        double rate = repository.findRate(request.zone());
        double cost = rate * request.weightKg();

        if (request.zone().equals("REGIONAL")) {
            cost += REGIONAL_HANDLING_FEE;
        } else if (request.zone().equals("INTERNATIONAL")) {
            cost += INTERNATIONAL_CUSTOMS_FEE;
        }

        if (request.weightKg() > HEAVY_WEIGHT_THRESHOLD_KG) {
            cost += HEAVY_SURCHARGE;
        }

        if (request.serviceLevel().equals("EXPRESS")) {
            cost *= EXPRESS_MULTIPLIER;
        }

        return cost;
    }
}
