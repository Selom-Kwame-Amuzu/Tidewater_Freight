package com.tidewater.freight.pricing;

import com.tidewater.freight.ShipmentRequest;

public final class HeavyWeightSurcharge implements CostModifier {

    private static final double THRESHOLD_KG = 30.0;
    private static final double SURCHARGE = 20.0;

    @Override
    public double apply(double cost, ShipmentRequest request) {
        return request.weightKg() > THRESHOLD_KG ? cost + SURCHARGE : cost;
    }
}
