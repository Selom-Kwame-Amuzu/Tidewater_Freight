# <u>Rationale</u>

## <u>Key decisions</u>

- **<u>Typed failures instead of sentinel values:</u>** the inherited `catch (Exception) { return 0; }` made failures indistinguishable from a free shipment. `UnknownZoneException`, `InvalidShipmentException`, and the `ShippingCalculationException` base now make failures explicit. No exception is swallowed.
- **<u>Validation as a rule chain:</u>** `ShipmentRule` + `ShipmentValidator` keep zone, weight, and service-level checks independent and ordered. Typed `ValidationError` values replace string messages so tests and callers do not depend on prose.
- **<u>Constructor injection:</u>** `ShippingCalculator` receives `ZonePolicyProvider` and `ShipmentValidator`. This removes the hard-wired repository and gives tests a real seam for both happy paths and failures.
- **<u>Purpose-based packages:</u>** `validation`, `exception`, and `pricing` group related types without hiding multiple public types in one file.
- **<u>Failure handling at the boundary:</u>** `Demo` catches `ShippingCalculationException` and prints a clear error. The domain calculator never prints and never returns a fake price.

## <u>How the new zone is absorbed</u>

Zone knowledge now lives in one place: a `ZonePolicy` implementation containing the zone name, rate per kg, and handling fee. `ZonePolicyRegistry` maps zone strings to policies, and `ShippingCalculator` depends only on the `ZonePolicyProvider` interface.

Adding the announced new zone means adding a new policy and registering it. `ShippingCalculator` is not edited. The `newZonePolicyWorksWithoutChangingTheCalculator` test proves this by injecting a `MOON` policy and asserting the resulting cost.

## <u>What happened to the inherited code</u>

| Inherited item | Decision | Why |
| --- | --- | --- |
| Rate map in `InMemoryRateRepository` | **Rewritten** into `ZonePolicy` implementations | Base rate and zone fee were split across two classes; one policy now owns the complete zone rule. |
| `ShippingCalculator` zone `if/else` | **Removed** | It violated OCP and had to change for every new zone. |
| Arithmetic and heavy/express modifiers | **Kept** with the same values and thresholds | The business rules were not wrong; only their structure and failure handling were. |
| Console output inside calculation | **Removed** | Console I/O belongs in the demo/boundary, not the domain service. |
| Three AI-generated tests | **Rewritten** | They stubbed the system under test, asserted a primitive was non-null, or checked only `> 0`. |

## <u>Improvement beyond the brief</u>

**<u>Parameterized edge-value tests</u>** cover invalid weights (`0`, negative, `NaN`, both infinities) and invalid service levels (null, blank, lowercase, unknown). This guards a class of numeric and string-input bugs the inherited suite never considered, and it makes the validation contract explicit in executable form.

A supporting safety guard is `List.copyOf` in `ShipmentValidator`: the rule list and returned errors are immutable, so callers cannot mutate validation behaviour or results after construction.

## <u>Verification</u>

- `mvn test` — 30 tests passing.
- `mvn verify` — JaCoCo report at `target/site/jacoco/index.html`.
- PIT mutation coverage — 30 of 35 mutants killed (86%), 100% test strength on covered mutants; subpackages are included by the existing `com.tidewater.freight.*` target.
- Demo — `MOON` now reports a failure instead of printing `0.0`.
