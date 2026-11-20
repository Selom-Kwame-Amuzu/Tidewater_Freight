package com.tidewater.freight;

import com.tidewater.freight.TestFixtures.FixedZonePolicy;
import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.pricing.DomesticZonePolicy;
import com.tidewater.freight.pricing.InternationalZonePolicy;
import com.tidewater.freight.pricing.RegionalZonePolicy;
import com.tidewater.freight.pricing.ZonePolicy;
import com.tidewater.freight.pricing.ZonePolicyRegistry;
import com.tidewater.freight.validation.ValidationError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ShippingPricingIntegrationTest {

    private static final double EPS = 1e-9;

    private final ShippingCalculator calculator = new ShippingCalculator(
            TestFixtures.defaultRegistry(), TestFixtures.defaultValidator(), TestFixtures.defaultModifiers());

    static Stream<Arguments> pricedShipments() {
        return Stream.of(
                Arguments.of("DOMESTIC", 10.0, "STANDARD", 50.0),
                Arguments.of("DOMESTIC", 10.0, "EXPRESS", 75.0),
                Arguments.of("REGIONAL", 5.0, "STANDARD", 50.0),
                Arguments.of("REGIONAL", 5.0, "EXPRESS", 75.0),
                Arguments.of("INTERNATIONAL", 10.0, "STANDARD", 175.0),
                Arguments.of("INTERNATIONAL", 40.0, "STANDARD", 645.0),
                Arguments.of("INTERNATIONAL", 40.0, "EXPRESS", 967.5));
    }

    @ParameterizedTest
    @MethodSource("pricedShipments")
    void pricesRealPoliciesExactly(String zone, double weight, String serviceLevel, double expected) {
        assertEquals(expected,
                calculator.calculateCost(new ShipmentRequest(zone, weight, serviceLevel)), EPS);
    }

    @Test
    void appliesHeavySurchargeOnlyAboveThreshold() {
        assertEquals(250.0,
                calculator.calculateCost(new ShipmentRequest("REGIONAL", 30, "STANDARD")), EPS);
        assertEquals(278.0,
                calculator.calculateCost(new ShipmentRequest("REGIONAL", 31, "STANDARD")), EPS);
    }

    @Test
    void rejectsUnknownZoneWithTypedFailure() {
        UnknownZoneException ex = assertThrows(UnknownZoneException.class,
                () -> calculator.calculateCost(new ShipmentRequest("MOON", 10, "STANDARD")));

        assertEquals("MOON", ex.zone());
    }

    @Test
    void collectsAllValidationErrorsBeforePricing() {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> calculator.calculateCost(new ShipmentRequest(" ", 0, "PRIORITY")));

        assertEquals(List.of(ValidationError.ZONE_REQUIRED, ValidationError.WEIGHT_INVALID,
                ValidationError.SERVICE_LEVEL_INVALID), ex.errors());
    }

    @Test
    void newPolicyCanBeRegisteredWithoutChangingTheCalculator() {
        List<ZonePolicy> policies = new ArrayList<>(List.of(
                new DomesticZonePolicy(),
                new RegionalZonePolicy(),
                new InternationalZonePolicy()));
        policies.add(new FixedZonePolicy("MOON", 12.0, 3.0));
        ShippingCalculator extended = new ShippingCalculator(
                new ZonePolicyRegistry(policies), TestFixtures.defaultValidator(), TestFixtures.defaultModifiers());

        assertEquals(123.0,
                extended.calculateCost(new ShipmentRequest("MOON", 10, "STANDARD")), EPS);
    }
}
