package com.tidewater.freight.pricing;

import com.tidewater.freight.TestFixtures.FixedZonePolicy;
import com.tidewater.freight.exception.UnknownZoneException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZonePolicyRegistryTest {

    @ParameterizedTest
    @CsvSource({"DOMESTIC,5.0,0.0", "REGIONAL,8.0,10.0"})
    void findsRegisteredPolicies(String zone, double ratePerKg, double handlingFee) {
        ZonePolicyRegistry registry = new ZonePolicyRegistry(List.of(
                new FixedZonePolicy("DOMESTIC", 5.0, 0.0),
                new FixedZonePolicy("REGIONAL", 8.0, 10.0)));

        assertEquals(new FixedZonePolicy(zone, ratePerKg, handlingFee), registry.find(zone));
    }

    @Test
    void rejectsUnknownZone() {
        ZonePolicyRegistry registry = new ZonePolicyRegistry(List.of(
                new FixedZonePolicy("DOMESTIC", 5.0, 0.0)));

        UnknownZoneException ex = assertThrows(UnknownZoneException.class, () -> registry.find("MOON"));

        assertEquals("MOON", ex.zone());
    }

    @Test
    void rejectsNullZone() {
        ZonePolicyRegistry registry = new ZonePolicyRegistry(List.of(
                new FixedZonePolicy("DOMESTIC", 5.0, 0.0)));

        assertThrows(NullPointerException.class, () -> registry.find(null));
    }

    @Test
    void rejectsDuplicateZones() {
        List<ZonePolicy> policies = List.of(
                new FixedZonePolicy("DOMESTIC", 5.0, 0.0),
                new FixedZonePolicy("DOMESTIC", 6.0, 0.0));

        assertThrows(IllegalStateException.class, () -> new ZonePolicyRegistry(policies));
    }

    @Test
    void rejectsNullPolicyList() {
        assertThrows(NullPointerException.class, () -> new ZonePolicyRegistry(null));
    }
}
