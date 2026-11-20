package com.tidewater.freight.pricing;

public interface ZonePolicy {
    String zone();

    double ratePerKg();

    double handlingFee();
}
