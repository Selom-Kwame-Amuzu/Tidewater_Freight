package com.tidewater.freight;

public record ShipmentRequest(String zone, double weightKg, String serviceLevel) {
}
