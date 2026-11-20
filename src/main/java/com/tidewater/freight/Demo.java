package com.tidewater.freight;

import com.tidewater.freight.exception.ShippingCalculationException;
import com.tidewater.freight.pricing.DomesticZonePolicy;
import com.tidewater.freight.pricing.HeavyWeightSurcharge;
import com.tidewater.freight.pricing.InternationalZonePolicy;
import com.tidewater.freight.pricing.RegionalZonePolicy;
import com.tidewater.freight.pricing.ServiceLevelMultiplier;
import com.tidewater.freight.pricing.ZonePolicyProvider;
import com.tidewater.freight.pricing.ZonePolicyRegistry;
import com.tidewater.freight.validation.ServiceLevelRule;
import com.tidewater.freight.validation.ShipmentValidator;
import com.tidewater.freight.validation.WeightRule;
import com.tidewater.freight.validation.ZoneRequiredRule;

import java.util.List;

public class Demo {
    public static void main(String[] args) {
        ZonePolicyProvider policies = new ZonePolicyRegistry(List.of(
                new DomesticZonePolicy(),
                new RegionalZonePolicy(),
                new InternationalZonePolicy()));
        ShippingCalculator calculator = new ShippingCalculator(
                policies,
                new ShipmentValidator(List.of(
                        new ZoneRequiredRule(),
                        new WeightRule(),
                        new ServiceLevelRule())),
                List.of(new HeavyWeightSurcharge(), new ServiceLevelMultiplier()));

        printResult(calculator, "DOMESTIC", 10, "STANDARD");
        printResult(calculator, "INTERNATIONAL", 40, "EXPRESS");
        printResult(calculator, "MOON", 10, "STANDARD");
    }

    private static void printResult(ShippingCalculator calculator, String zone, double weightKg, String serviceLevel) {
        String label = "Result " + zone + " " + weightKg + "kg " + serviceLevel + " -> ";
        try {
            double cost = calculator.calculateCost(new ShipmentRequest(zone, weightKg, serviceLevel));
            System.out.println(label + cost);
        } catch (ShippingCalculationException e) {
            System.out.println(label + "FAILED: " + e.getMessage());
        }
    }
}
