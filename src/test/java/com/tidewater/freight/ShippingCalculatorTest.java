package com.tidewater.freight;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.exception.UnknownZoneException;
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

    @Mock
    private RateRepository repository;

    private ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShippingCalculator(repository, defaultValidator());
    }

    private static ShipmentValidator defaultValidator() {
        return new ShipmentValidator(List.of(
                new ZoneRequiredRule(),
                new WeightRule(),
                new ServiceLevelRule()));
    }

    @Test
    void calculatesDomesticStandardCostExactly() {
        when(repository.findRate("DOMESTIC")).thenReturn(5.0);

        assertEquals(50.0, calculator.calculateCost(new ShipmentRequest("DOMESTIC", 10, "STANDARD")), EPS);
    }

    @Test
    void calculatesRegionalStandardCostWithHandlingFee() {
        when(repository.findRate("REGIONAL")).thenReturn(8.0);

        assertEquals(50.0, calculator.calculateCost(new ShipmentRequest("REGIONAL", 5, "STANDARD")), EPS);
    }

    @Test
    void calculatesInternationalExpressCostWithAllSurcharges() {
        when(repository.findRate("INTERNATIONAL")).thenReturn(15.0);

        assertEquals(967.5, calculator.calculateCost(new ShipmentRequest("INTERNATIONAL", 40, "EXPRESS")), EPS);
    }

    @Test
    void appliesExpressMultiplierWithoutSurcharges() {
        when(repository.findRate("DOMESTIC")).thenReturn(5.0);

        assertEquals(75.0, calculator.calculateCost(new ShipmentRequest("DOMESTIC", 10, "EXPRESS")), EPS);
    }

    @Test
    void heavySurchargeStartsAboveThirtyKilograms() {
        when(repository.findRate("REGIONAL")).thenReturn(8.0);

        assertEquals(250.0, calculator.calculateCost(new ShipmentRequest("REGIONAL", 30, "STANDARD")), EPS);
        assertEquals(278.0, calculator.calculateCost(new ShipmentRequest("REGIONAL", 31, "STANDARD")), EPS);
    }

    @Test
    void worksWithTheRealInMemoryRepository() {
        ShippingCalculator real = new ShippingCalculator(new InMemoryRateRepository(), defaultValidator());

        assertEquals(50.0, real.calculateCost(new ShipmentRequest("DOMESTIC", 10, "STANDARD")), EPS);
    }

    @Test
    void unknownZoneThrowsInsteadOfReturningZero() {
        ShippingCalculator real = new ShippingCalculator(new InMemoryRateRepository(), defaultValidator());

        UnknownZoneException ex = assertThrows(UnknownZoneException.class,
                () -> real.calculateCost(new ShipmentRequest("MOON", 10, "STANDARD")));
        assertEquals("MOON", ex.zone());
    }

    @Test
    void repositoryFailurePropagatesWithItsCause() {
        IllegalStateException failure = new IllegalStateException("rate service unavailable");
        when(repository.findRate("DOMESTIC")).thenThrow(failure);

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
        verifyNoInteractions(repository);
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
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "express", "PRIORITY"})
    void invalidServiceLevelIsRejected(String serviceLevel) {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> calculator.calculateCost(new ShipmentRequest("DOMESTIC", 10, serviceLevel)));

        assertTrue(ex.errors().contains(ValidationError.SERVICE_LEVEL_INVALID));
        verifyNoInteractions(repository);
    }

    @Test
    void collectsAllInputErrorsInOneException() {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> calculator.calculateCost(new ShipmentRequest(" ", 0, "PRIORITY")));

        assertEquals(List.of(ValidationError.ZONE_REQUIRED, ValidationError.WEIGHT_INVALID,
                ValidationError.SERVICE_LEVEL_INVALID), ex.errors());
    }
}
