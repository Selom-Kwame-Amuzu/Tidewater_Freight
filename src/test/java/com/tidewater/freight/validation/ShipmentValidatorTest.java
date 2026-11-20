package com.tidewater.freight.validation;

import com.tidewater.freight.ShipmentRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void delegatesToEveryRuleInOrder() {
        ShipmentRule first = mock(ShipmentRule.class);
        ShipmentRule second = mock(ShipmentRule.class);
        when(first.check(any())).thenReturn(Optional.of(ValidationError.ZONE_REQUIRED));
        when(second.check(any())).thenReturn(Optional.empty());
        ShipmentValidator validator = new ShipmentValidator(List.of(first, second));
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", 10, "STANDARD");

        List<ValidationError> errors = validator.validate(request);

        assertEquals(List.of(ValidationError.ZONE_REQUIRED), errors);
        var order = inOrder(first, second);
        order.verify(first).check(request);
        order.verify(second).check(request);
    }

    @Test
    void noRulesMeansNoViolations() {
        ShipmentValidator validator = new ShipmentValidator(List.of());

        assertTrue(validator.validate(new ShipmentRequest(null, -1, null)).isEmpty());
    }

    @Test
    void rejectsNullRequest() {
        ShipmentValidator validator = new ShipmentValidator(List.of());

        assertThrows(NullPointerException.class, () -> validator.validate(null));
    }
}
