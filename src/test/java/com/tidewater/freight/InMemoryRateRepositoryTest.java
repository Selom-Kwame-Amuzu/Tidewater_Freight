package com.tidewater.freight;

import com.tidewater.freight.exception.UnknownZoneException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRateRepositoryTest {

    private final InMemoryRateRepository repository = new InMemoryRateRepository();

    @Test
    void returnsConfiguredRates() {
        assertEquals(5.0, repository.findRate("DOMESTIC"));
        assertEquals(8.0, repository.findRate("REGIONAL"));
        assertEquals(15.0, repository.findRate("INTERNATIONAL"));
    }

    @Test
    void rejectsUnknownZone() {
        UnknownZoneException ex = assertThrows(UnknownZoneException.class,
                () -> repository.findRate("MOON"));

        assertEquals("MOON", ex.zone());
    }

    @Test
    void rejectsNullZone() {
        assertThrows(NullPointerException.class, () -> repository.findRate(null));
    }
}
