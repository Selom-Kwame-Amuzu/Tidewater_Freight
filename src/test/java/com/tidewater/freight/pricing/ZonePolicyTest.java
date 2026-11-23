package com.tidewater.freight.pricing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZonePolicyTest {

    private static final double EPS = 1e-9;

    static Stream<Arguments> allZonePolicies() {
        return Stream.of(
                Arguments.of(new DomesticZonePolicy(), "DOMESTIC", 5.0, 0.0),
                Arguments.of(new RegionalZonePolicy(), "REGIONAL", 8.0, 10.0),
                Arguments.of(new InternationalZonePolicy(), "INTERNATIONAL", 15.0, 25.0));
    }

    @ParameterizedTest
    @MethodSource("allZonePolicies")
    void reportsItsZone(ZonePolicy policy, String zone, double rate, double handlingFee) {
        assertEquals(zone, policy.zone());
    }

    @ParameterizedTest
    @MethodSource("allZonePolicies")
    void reportsItsRatePerKg(ZonePolicy policy, String zone, double rate, double handlingFee) {
        assertEquals(rate, policy.ratePerKg(), EPS);
    }

    @ParameterizedTest
    @MethodSource("allZonePolicies")
    void reportsItsHandlingFee(ZonePolicy policy, String zone, double rate, double handlingFee) {
        assertEquals(handlingFee, policy.handlingFee(), EPS);
    }
}
