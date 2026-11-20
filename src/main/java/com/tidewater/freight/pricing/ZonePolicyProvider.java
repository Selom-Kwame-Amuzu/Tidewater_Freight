package com.tidewater.freight.pricing;

public interface ZonePolicyProvider {
    ZonePolicy find(String zone);
}
