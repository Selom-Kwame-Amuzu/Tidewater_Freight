package com.tidewater.freight.pricing;

public final class RegionalZonePolicy implements ZonePolicy {

    @Override
    public String zone() {
        return "REGIONAL";
    }

    @Override
    public double ratePerKg() {
        return 8.0;
    }

    @Override
    public double handlingFee() {
        return 10.0;
    }
}
