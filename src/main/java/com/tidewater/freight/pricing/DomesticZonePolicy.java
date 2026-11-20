package com.tidewater.freight.pricing;

public final class DomesticZonePolicy implements ZonePolicy {

    @Override
    public String zone() {
        return "DOMESTIC";
    }

    @Override
    public double ratePerKg() {
        return 5.0;
    }

    @Override
    public double handlingFee() {
        return 0.0;
    }
}
