package com.tidewater.freight;

import com.tidewater.freight.pricing.CostModifier;
import com.tidewater.freight.pricing.DomesticZonePolicy;
import com.tidewater.freight.pricing.HeavyWeightSurcharge;
import com.tidewater.freight.pricing.InternationalZonePolicy;
import com.tidewater.freight.pricing.RegionalZonePolicy;
import com.tidewater.freight.pricing.ServiceLevelMultiplier;
import com.tidewater.freight.pricing.ZonePolicy;
import com.tidewater.freight.pricing.ZonePolicyRegistry;
import com.tidewater.freight.validation.ServiceLevelRule;
import com.tidewater.freight.validation.ShipmentValidator;
import com.tidewater.freight.validation.WeightRule;
import com.tidewater.freight.validation.ZoneRequiredRule;

import java.util.List;

public final class TestFixtures {

    private TestFixtures() {
    }

    public record FixedZonePolicy(String zone, double ratePerKg, double handlingFee) implements ZonePolicy {
    }

    public static ShipmentValidator defaultValidator() {
        return new ShipmentValidator(List.of(
                new ZoneRequiredRule(),
                new WeightRule(),
                new ServiceLevelRule()));
    }

    public static ZonePolicyRegistry defaultRegistry() {
        return new ZonePolicyRegistry(List.of(
                new DomesticZonePolicy(),
                new RegionalZonePolicy(),
                new InternationalZonePolicy()));
    }

    public static List<CostModifier> defaultModifiers() {
        return List.of(new HeavyWeightSurcharge(), new ServiceLevelMultiplier());
    }
}
