package com.tidewater.freight.validation;

import com.tidewater.freight.ShipmentRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeightRuleTest {

    private final WeightRule rule = new WeightRule();

    static Stream<Double> nonFiniteAndNonPositiveWeights() {
        return Stream.of(0.0, -0.0, -1.0, Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @ParameterizedTest
    @MethodSource("nonFiniteAndNonPositiveWeights")
    void rejectsNonFiniteAndNonPositiveWeights(double weight) {
        assertEquals(Optional.of(ValidationError.WEIGHT_INVALID),
                rule.check(new ShipmentRequest("DOMESTIC", weight, "STANDARD")));
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.MIN_VALUE, 0.0001, 1.0, 30.0, 31.0, 1.0e9})
    void acceptsEveryFinitePositiveWeight(double weight) {
        assertTrue(rule.check(new ShipmentRequest("DOMESTIC", weight, "STANDARD")).isEmpty());
    }
}
