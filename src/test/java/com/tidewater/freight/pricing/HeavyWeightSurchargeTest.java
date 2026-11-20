package com.tidewater.freight.pricing;

import com.tidewater.freight.ShipmentRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeavyWeightSurchargeTest {

    private static final double EPS = 1e-9;

    private final HeavyWeightSurcharge modifier = new HeavyWeightSurcharge();

    @Test
    void leavesCostUnchangedAtOrBelowThreshold() {
        assertEquals(100.0, modifier.apply(100.0, new ShipmentRequest("DOMESTIC", 29.9, "STANDARD")), EPS);
        assertEquals(100.0, modifier.apply(100.0, new ShipmentRequest("DOMESTIC", 30, "STANDARD")), EPS);
    }

    @Test
    void addsSurchargeAboveThreshold() {
        assertEquals(120.0, modifier.apply(100.0, new ShipmentRequest("DOMESTIC", 30.1, "STANDARD")), EPS);
        assertEquals(120.0, modifier.apply(100.0, new ShipmentRequest("DOMESTIC", 31, "STANDARD")), EPS);
    }
}
