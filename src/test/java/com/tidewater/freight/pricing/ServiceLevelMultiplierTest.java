package com.tidewater.freight.pricing;

import com.tidewater.freight.ShipmentRequest;
import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.validation.ValidationError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceLevelMultiplierTest {

    private static final double EPS = 1e-9;

    private final ServiceLevelMultiplier modifier = new ServiceLevelMultiplier();

    @Test
    void standardLevelLeavesCostUnchanged() {
        assertEquals(100.0,
                modifier.apply(100.0, new ShipmentRequest("DOMESTIC", 10, "STANDARD")), EPS);
    }

    @Test
    void expressLevelMultipliesCost() {
        assertEquals(150.0,
                modifier.apply(100.0, new ShipmentRequest("DOMESTIC", 10, "EXPRESS")), EPS);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"express", "PRIORITY"})
    void rejectsInvalidLevelWithTheTypedFailure(String level) {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> modifier.apply(100.0, new ShipmentRequest("DOMESTIC", 10, level)));

        assertEquals(List.of(ValidationError.SERVICE_LEVEL_INVALID), ex.errors());
    }
}
