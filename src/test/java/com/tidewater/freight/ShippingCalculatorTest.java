package com.tidewater.freight;

import com.tidewater.freight.TestFixtures.FixedZonePolicy;
import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.pricing.CostModifier;
import com.tidewater.freight.pricing.ZonePolicyProvider;
import com.tidewater.freight.validation.ShipmentValidator;
import com.tidewater.freight.validation.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
    private static final ShipmentRequest REQUEST = new ShipmentRequest("DOMESTIC", 10, "STANDARD");

    @Mock
    private ZonePolicyProvider policies;

    private ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShippingCalculator(
                policies, TestFixtures.defaultValidator(), TestFixtures.defaultModifiers());
    }

    static Stream<Arguments> nullCollaboratorSets() {
        ShipmentValidator validator = TestFixtures.defaultValidator();
        List<CostModifier> modifiers = TestFixtures.defaultModifiers();
        return Stream.of(
                Arguments.of(null, validator, modifiers),
                Arguments.of(TestFixtures.defaultRegistry(), null, modifiers),
                Arguments.of(TestFixtures.defaultRegistry(), validator, null));
    }

    @ParameterizedTest
    @MethodSource("nullCollaboratorSets")
    void rejectsNullCollaborators(ZonePolicyProvider provider, ShipmentValidator validator,
                                  List<CostModifier> modifiers) {
        assertThrows(NullPointerException.class,
                () -> new ShippingCalculator(provider, validator, modifiers));
    }

    @Test
    void calculatesDomesticStandardCostExactly() {
        when(policies.find("DOMESTIC")).thenReturn(new FixedZonePolicy("DOMESTIC", 5.0, 0.0));

        assertEquals(50.0, calculator.calculateCost(REQUEST), EPS);
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
    void appliesEveryModifierInOrder() {
        when(policies.find("DOMESTIC")).thenReturn(new FixedZonePolicy("DOMESTIC", 5.0, 0.0));
        CostModifier first = mock(CostModifier.class);
        CostModifier second = mock(CostModifier.class);
        when(first.apply(50.0, REQUEST)).thenReturn(60.0);
        when(second.apply(60.0, REQUEST)).thenReturn(90.0);
        ShippingCalculator withCustomModifiers = new ShippingCalculator(
                policies, TestFixtures.defaultValidator(), List.of(first, second));

        assertEquals(90.0, withCustomModifiers.calculateCost(REQUEST), EPS);

        var order = inOrder(first, second);
        order.verify(first).apply(50.0, REQUEST);
        order.verify(second).apply(60.0, REQUEST);
    }

    @Test
    void providerFailurePropagatesWithItsCause() {
        IllegalStateException failure = new IllegalStateException("policy service unavailable");
        when(policies.find("DOMESTIC")).thenThrow(failure);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> calculator.calculateCost(REQUEST));

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
