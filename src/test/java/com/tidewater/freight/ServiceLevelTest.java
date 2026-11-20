package com.tidewater.freight;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceLevelTest {

    private static final double EPS = 1e-9;

    @Test
    void exposesTheMultiplierForEachLevel() {
        assertEquals(1.0, ServiceLevel.STANDARD.multiplier(), EPS);
        assertEquals(1.5, ServiceLevel.EXPRESS.multiplier(), EPS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STANDARD", "EXPRESS"})
    void parsesKnownLevelsExactly(String value) {
        assertEquals(Optional.of(ServiceLevel.valueOf(value)), ServiceLevel.from(value));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "express", "Express", "PRIORITY", "STANDARD "})
    void rejectsNullBlankAndUnknownLevels(String value) {
        assertTrue(ServiceLevel.from(value).isEmpty());
    }
}
