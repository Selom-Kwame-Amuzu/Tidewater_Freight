package com.tidewater.freight.pricing;

import com.tidewater.freight.ShipmentRequest;

public interface CostModifier {
    double apply(double cost, ShipmentRequest request);
}
