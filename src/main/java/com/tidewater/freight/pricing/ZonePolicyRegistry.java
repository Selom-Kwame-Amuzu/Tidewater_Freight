package com.tidewater.freight.pricing;

import com.tidewater.freight.exception.UnknownZoneException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ZonePolicyRegistry implements ZonePolicyProvider {

    private final Map<String, ZonePolicy> policies;

    public ZonePolicyRegistry(List<ZonePolicy> policies) {
        this.policies = policies.stream()
                .collect(Collectors.toUnmodifiableMap(ZonePolicy::zone, Function.identity()));
    }

    @Override
    public ZonePolicy find(String zone) {
        Objects.requireNonNull(zone, "zone");
        ZonePolicy policy = policies.get(zone);
        if (policy == null) {
            throw new UnknownZoneException(zone);
        }
        return policy;
    }
}
