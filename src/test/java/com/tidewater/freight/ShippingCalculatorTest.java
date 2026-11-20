package com.tidewater.freight;

import com.tidewater.freight.TestFixtures.FixedZonePolicy;
import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.pricing.ZonePolicyProvider;
import com.tidewater.freight.validation.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingCalculatorTest {

    private static final double EPS = 1e-9;

    @Mock
    private ZonePolicyProvider policies;

    private ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShippingCalculator(policies, TestFixtures.defaultValidator());
    }

    @Test
    void calculatesDomesticStandardCostExactly() {
        when(policies.find("DOMESTIC")).thenReturn(new FixedZonePolicy("DOMESTIC", 5.0, 0.0));

        assertEquals(50.0, calculator.calculateCost(new ShipmentRequest("DOMESTIC", 10, "STANDARD")), EPS);
    }

    @Test
    void calculatesRegionalStandardCostWithHandlingFee() {
        when(policies.find("REGIONAL")).thenReturn(new FixedZonePolicy("REGIONAL", 8.0, 10.0));

        assertEquals(50.0, calculator.calculateCost(new ShipmentRequest("REGIONAL", 5, "STANDARD")), EPS);
    }

    @Test
    void calculatesInternationalExpressCostWithAllSurcharges() {
        when(policies.find("INTERNATIONAL")).thenReturn(new FixedZonePolicy("INTERNATIONAL", 15.0, 25.0));

        assertEquals(967.5, calculator.calculateCost(new ShipmentRequest("INTERNATIONAL", 40, "EXPRESS")), EPS);
    }

    @Test
    void appliesExpressMultiplierWithoutSurcharges() {
        when(policies.find("DOMESTIC")).thenReturn(new FixedZonePolicy("DOMESTIC", 5.0, 0.0));

        assertEquals(75.0, calculator.calculateCost(new ShipmentRequest("DOMESTIC", 10, "EXPRESS")), EPS);
    }

    @Test
    void heavySurchargeStartsAboveThirtyKilograms() {
        when(policies.find("REGIONAL")).thenReturn(new FixedZonePolicy("REGIONAL", 8.0, 10.0));

        assertEquals(250.0, calculator.calculateCost(new ShipmentRequest("REGIONAL", 30, "STANDARD")), EPS);
        assertEquals(278.0, calculator.calculateCost(new ShipmentRequest("REGIONAL", 31, "STANDARD")), EPS);
    }

    @Test
    void providerFailurePropagatesWithItsCause() {
        IllegalStateException failure = new IllegalStateException("policy service unavailable");
        when(policies.find("DOMESTIC")).thenThrow(failure);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> calculator.calculateCost(new ShipmentRequest("DOMESTIC", 10, "STANDARD")));

        assertSame(failure, thrown);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankZoneIsRejectedBeforeProviderIsCalled(String zone) {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> calculator.calculateCost(new ShipmentRequest(zone, 10, "STANDARD")));

        assertTrue(ex.errors().contains(ValidationError.ZONE_REQUIRED));
        verifyNoInteractions(policies);
    }

    static Stream<Double> invalidWeights() {
        return Stream.of(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @ParameterizedTest
    @MethodSource("invalidWeights")
    void invalidWeightIsRejectedBeforeProviderIsCalled(double weight) {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> calculator.calculateCost(new ShipmentRequest("DOMESTIC", weight, "STANDARD")));

        assertTrue(ex.errors().contains(ValidationError.WEIGHT_INVALID));
        verifyNoInteractions(policies);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "express", "PRIORITY"})
    void invalidServiceLevelIsRejectedBeforeProviderIsCalled(String serviceLevel) {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> calculator.calculateCost(new ShipmentRequest("DOMESTIC", 10, serviceLevel)));

        assertTrue(ex.errors().contains(ValidationError.SERVICE_LEVEL_INVALID));
        verifyNoInteractions(policies);
    }

    @Test
    void collectsAllInputErrorsInOneException() {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> calculator.calculateCost(new ShipmentRequest(" ", 0, "PRIORITY")));

        assertEquals(List.of(ValidationError.ZONE_REQUIRED, ValidationError.WEIGHT_INVALID,
                ValidationError.SERVICE_LEVEL_INVALID), ex.errors());
        verifyNoInteractions(policies);
    }
}
