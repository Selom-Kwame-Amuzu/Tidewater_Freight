package com.tidewater.freight;

import java.util.Optional;

public enum ServiceLevel {

    STANDARD(1.0),
    EXPRESS(1.5);

    private final double multiplier;

    ServiceLevel(double multiplier) {
        this.multiplier = multiplier;
    }

    public double multiplier() {
        return multiplier;
    }

    public static Optional<ServiceLevel> from(String value) {
        if (value == null) {
            return Optional.empty();
        }
        for (ServiceLevel level : values()) {
            if (level.name().equals(value)) {
                return Optional.of(level);
            }
        }
        return Optional.empty();
    }
}
