package com.tidewater.freight.pricing;

import com.tidewater.freight.TestFixtures.FixedZonePolicy;
import com.tidewater.freight.exception.UnknownZoneException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZonePolicyRegistryTest {

    @Test
    void findsRegisteredPolicies() {
        ZonePolicyRegistry registry = new ZonePolicyRegistry(List.of(
                new FixedZonePolicy("DOMESTIC", 5.0, 0.0),
                new FixedZonePolicy("REGIONAL", 8.0, 10.0)));

        assertEquals(5.0, registry.find("DOMESTIC").ratePerKg());
        assertEquals(10.0, registry.find("REGIONAL").handlingFee());
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
