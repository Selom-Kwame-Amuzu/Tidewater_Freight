package com.tidewater.freight.pricing;

public final class InternationalZonePolicy implements ZonePolicy {

    @Override
    public String zone() {
        return "INTERNATIONAL";
    }

    @Override
    public double ratePerKg() {
        return 15.0;
    }

    @Override
    public double handlingFee() {
        return 25.0;
    }
}
