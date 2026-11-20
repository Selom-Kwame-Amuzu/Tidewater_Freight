package com.tidewater.freight.validation;

import com.tidewater.freight.ShipmentRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceLevelRuleTest {

    private final ServiceLevelRule rule = new ServiceLevelRule();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "express", "Express", "PRIORITY", "STANDARD "})
    void rejectsUnknownBlankAndWrongCaseLevels(String serviceLevel) {
        assertEquals(Optional.of(ValidationError.SERVICE_LEVEL_INVALID),
                rule.check(new ShipmentRequest("DOMESTIC", 10, serviceLevel)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"STANDARD", "EXPRESS"})
    void acceptsKnownLevels(String serviceLevel) {
        assertTrue(rule.check(new ShipmentRequest("DOMESTIC", 10, serviceLevel)).isEmpty());
    }
}
