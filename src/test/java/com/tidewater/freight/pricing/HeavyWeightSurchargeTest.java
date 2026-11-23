package com.tidewater.freight.pricing;

import com.tidewater.freight.ShipmentRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeavyWeightSurchargeTest {

    private static final double EPS = 1e-9;

    private final HeavyWeightSurcharge modifier = new HeavyWeightSurcharge();

    @ParameterizedTest
    @CsvSource({"29.9,100.0", "30,100.0", "30.1,120.0", "31,120.0"})
    void appliesSurchargeOnlyAboveThreshold(double weightKg, double expectedCost) {
        assertEquals(expectedCost,
                modifier.apply(100.0, new ShipmentRequest("DOMESTIC", weightKg, "STANDARD")), EPS);
    }
}
