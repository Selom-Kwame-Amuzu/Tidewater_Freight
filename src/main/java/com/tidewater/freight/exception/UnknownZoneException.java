package com.tidewater.freight.exception;

public final class UnknownZoneException extends ShippingCalculationException {

    private final String zone;

    public UnknownZoneException(String zone) {
        super("Unknown shipping zone: " + zone);
        this.zone = zone;
    }

    public String zone() {
        return zone;
    }
}
