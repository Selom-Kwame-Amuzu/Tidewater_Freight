package com.tidewater.freight.validation;

import com.tidewater.freight.ShipmentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoneRequiredRuleTest {

    private final ZoneRequiredRule rule = new ZoneRequiredRule();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void rejectsNullAndBlankZones(String zone) {
        assertEquals(Optional.of(ValidationError.ZONE_REQUIRED),
                rule.check(new ShipmentRequest(zone, 10, "STANDARD")));
    }

    @Test
    void acceptsNonBlankZonesIncludingOnesNotYetKnown() {
        assertTrue(rule.check(new ShipmentRequest("DOMESTIC", 10, "STANDARD")).isEmpty());
        assertTrue(rule.check(new ShipmentRequest("MOON", 10, "STANDARD")).isEmpty());
    }
}
