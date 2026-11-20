package com.tidewater.freight;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.pricing.DomesticZonePolicy;
import com.tidewater.freight.pricing.InternationalZonePolicy;
import com.tidewater.freight.pricing.RegionalZonePolicy;
import com.tidewater.freight.pricing.ZonePolicy;
import com.tidewater.freight.pricing.ZonePolicyProvider;
import com.tidewater.freight.pricing.ZonePolicyRegistry;
import com.tidewater.freight.validation.ServiceLevelRule;
import com.tidewater.freight.validation.ShipmentValidator;
import com.tidewater.freight.validation.ValidationError;
import com.tidewater.freight.validation.WeightRule;
import com.tidewater.freight.validation.ZoneRequiredRule;
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

    private record FixedZonePolicy(String zone, double ratePerKg, double handlingFee) implements ZonePolicy {
    }

    @Mock
    private ZonePolicyProvider policies;

    private ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShippingCalculator(policies, defaultValidator());
    }

    private static ShipmentValidator defaultValidator() {
        return new ShipmentValidator(List.of(
                new ZoneRequiredRule(),
                new WeightRule(),
                new ServiceLevelRule()));
    }

    private static ZonePolicyRegistry defaultRegistry() {
        return new ZonePolicyRegistry(List.of(
                new DomesticZonePolicy(),
                new RegionalZonePolicy(),
                new InternationalZonePolicy()));
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
    void worksWithTheRealPolicyRegistry() {
        ShippingCalculator real = new ShippingCalculator(defaultRegistry(), defaultValidator());

        assertEquals(50.0, real.calculateCost(new ShipmentRequest("DOMESTIC", 10, "STANDARD")), EPS);
        assertEquals(50.0, real.calculateCost(new ShipmentRequest("REGIONAL", 5, "STANDARD")), EPS);
        assertEquals(967.5, real.calculateCost(new ShipmentRequest("INTERNATIONAL", 40, "EXPRESS")), EPS);
    }

    @Test
    void newZonePolicyWorksWithoutChangingTheCalculator() {
        ZonePolicy moon = new FixedZonePolicy("MOON", 12.0, 3.0);
        ZonePolicyProvider extended = zone -> {
            if (zone.equals("MOON")) {
                return moon;
            }
            return defaultRegistry().find(zone);
        };
        ShippingCalculator real = new ShippingCalculator(extended, defaultValidator());

        assertEquals(123.0, real.calculateCost(new ShipmentRequest("MOON", 10, "STANDARD")), EPS);
    }

    @Test
    void unknownZoneThrowsInsteadOfReturningZero() {
        ShippingCalculator real = new ShippingCalculator(defaultRegistry(), defaultValidator());

        UnknownZoneException ex = assertThrows(UnknownZoneException.class,
                () -> real.calculateCost(new ShipmentRequest("MOON", 10, "STANDARD")));
        assertEquals("MOON", ex.zone());
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
    void blankZoneIsRejectedBeforeRepositoryIsCalled(String zone) {
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
    void invalidWeightIsRejected(double weight) {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> calculator.calculateCost(new ShipmentRequest("DOMESTIC", weight, "STANDARD")));

        assertTrue(ex.errors().contains(ValidationError.WEIGHT_INVALID));
        verifyNoInteractions(policies);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "express", "PRIORITY"})
    void invalidServiceLevelIsRejected(String serviceLevel) {
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
    }
}
