package com.tidewater.freight.validation;

import com.tidewater.freight.ShipmentRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentValidatorTest {

    @Test
    void collectsViolationsInRuleOrder() {
        ShipmentValidator validator = new ShipmentValidator(List.of(
                request -> Optional.of(ValidationError.ZONE_REQUIRED),
                request -> Optional.of(ValidationError.WEIGHT_INVALID),
                request -> Optional.empty()));

        assertEquals(List.of(ValidationError.ZONE_REQUIRED, ValidationError.WEIGHT_INVALID),
                validator.validate(new ShipmentRequest(null, 0, null)));
    }

    @Test
    void returnsAnImmutableResult() {
        ShipmentValidator validator = new ShipmentValidator(List.of(
                request -> Optional.of(ValidationError.WEIGHT_INVALID)));

        List<ValidationError> errors = validator.validate(new ShipmentRequest("DOMESTIC", -1, "STANDARD"));

        assertThrows(UnsupportedOperationException.class,
                () -> errors.add(ValidationError.ZONE_REQUIRED));
    }

    @Test
    void copiesTheRuleList() {
        List<ShipmentRule> rules = new ArrayList<>();
        rules.add(request -> Optional.of(ValidationError.ZONE_REQUIRED));
        ShipmentValidator validator = new ShipmentValidator(rules);

        rules.clear();

        assertEquals(List.of(ValidationError.ZONE_REQUIRED),
                validator.validate(new ShipmentRequest(null, 10, "STANDARD")));
    }
}
