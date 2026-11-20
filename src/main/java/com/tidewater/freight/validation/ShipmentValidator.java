package com.tidewater.freight.validation;

import com.tidewater.freight.ShipmentRequest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ShipmentValidator {

    private final List<ShipmentRule> rules;

    public ShipmentValidator(List<ShipmentRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<ValidationError> validate(ShipmentRequest request) {
        Objects.requireNonNull(request, "request");
        return rules.stream()
                .map(rule -> rule.check(request))
                .flatMap(Optional::stream)
                .toList();
    }
}
