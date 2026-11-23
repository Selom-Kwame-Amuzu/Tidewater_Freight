# Tidewater Freight — Shipping Cost Service

A small, reviewed shipping-cost service that calculates a quote from shipment
weight, destination zone, and service level. The inherited AI-generated version
was refactored for Clean Code and SOLID, given explicit failure handling, and
covered by a test suite that asserts real behaviour.

## What it does

`ShippingCalculator.calculateCost(ShipmentRequest)` validates the request, looks
up the zone's rate rule, computes the base cost, and applies price modifiers in
order:

| Zone | Rate / kg | Handling fee |
| --- | --- | --- |
| `DOMESTIC` | 5.0 | 0.0 |
| `REGIONAL` | 8.0 | 10.0 |
| `INTERNATIONAL` | 15.0 | 25.0 |

- Weights must be finite and greater than zero; zones must be non-blank; service
  levels are `STANDARD` (×1.0) or `EXPRESS` (×1.5).
- Heavy surcharge: `+20` above 30 kg, applied **before** the service multiplier.
- Example: `INTERNATIONAL`, 40 kg, `EXPRESS` → `(40 × 15 + 25 + 20) × 1.5 = 967.5`.

Invalid input raises `InvalidShipmentException` (with typed `ValidationError`
codes); an unknown zone raises `UnknownZoneException`. No failure is converted
into a fake price.

## Structure

```text
com.tidewater.freight
├── ShippingCalculator.java      # orchestration only: validate → price → modify
├── ShipmentRequest.java         # input record
├── ServiceLevel.java            # allowed levels and their multipliers
├── Demo.java                    # wiring and boundary error reporting
├── pricing/
│   ├── ZonePolicy / ZonePolicyProvider / ZonePolicyRegistry
│   ├── DomesticZonePolicy / RegionalZonePolicy / InternationalZonePolicy
│   ├── CostModifier
│   ├── HeavyWeightSurcharge / ServiceLevelMultiplier
├── validation/
│   ├── ShipmentRule / ShipmentValidator / ValidationError
│   └── ZoneRequiredRule / WeightRule / ServiceLevelRule
└── exception/
    ├── ShippingCalculationException
    ├── InvalidShipmentException
    └── UnknownZoneException
```

All collaborators are constructor-injected; nothing is hard-wired inside the
calculator.

### Adding a new rule

- **New zone:** add a `ZonePolicy` implementation and register it in `Demo`.
  `ShippingCalculator` is not edited.
- **New price adjustment:** add a `CostModifier` and place it in the ordered
  list in `Demo`. Order matters; the calculator just folds the list.
- **New service level:** add a constant to `ServiceLevel`. Validation and
  pricing both derive from the enum, so no other production class changes.
- **New validation check:** add a `ShipmentRule` to the validator's list.

## Running and verifying

JDK 21 and Maven are required; there is no wrapper script.

```bash
# See the service run (no tests)
mvn compile
mvn exec:java -Dexec.mainClass=com.tidewater.freight.Demo

# Run the 105-test suite
mvn test

# Coverage report: target/site/jacoco/index.html
mvn verify

# Mutation report: target/pit-reports/index.html
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

Expected demo output:

```text
Result DOMESTIC 10.0kg STANDARD -> 50.0
Result INTERNATIONAL 40.0kg EXPRESS -> 967.5
Result MOON 10.0kg STANDARD -> FAILED: Unknown shipping zone: MOON
```

The suite is a two-layer pyramid: a broad unit base, a Mockito-seam layer for
orchestration, and a small real-collaborator integration layer. There are no
end-to-end tests; the rationale explains why.

## Written work

- [ANALYSIS.md](ANALYSIS.md) — pre-refactor review of the inherited code and tests, with the most serious problem ranked first.
- [RATIONALE.md](RATIONALE.md) — refactoring decisions, what was kept/fixed/rewritten, how the design absorbs new rules, test strategy, and the improvement beyond the brief.
