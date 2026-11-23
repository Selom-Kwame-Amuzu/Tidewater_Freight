# <u>Rationale</u>

## <u>Key decisions</u>

- **<u>Typed failures instead of sentinel values:</u>** the inherited `catch (Exception) { return 0; }` made failures indistinguishable from a free shipment. `UnknownZoneException`, `InvalidShipmentException`, and the `ShippingCalculationException` base now make failures explicit. No exception is swallowed.
- **<u>Validation as a rule chain:</u>** `ShipmentRule` + `ShipmentValidator` keep zone, weight, and service-level checks independent and ordered. Typed `ValidationError` values replace string messages so tests and callers do not depend on prose.
- **<u>Constructor injection:</u>** `ShippingCalculator` receives `ZonePolicyProvider`, `ShipmentValidator`, and an ordered `List<CostModifier>`. This removes the hard-wired repository and gives tests a real seam for both happy paths and failures.
- **<u>Price modifiers as a chain:</u>** the heavy-weight surcharge and the service-level multiplier were extracted from `calculateCost` into `HeavyWeightSurcharge` and `ServiceLevelMultiplier`. Each owns its threshold/value and applies itself to the running cost. The calculator just folds the injected list in order, so a new modifier needs no calculator edit. Order is part of the wiring, preserving "surcharge first, then multiplier".
- **<u>One home for service levels:</u>** the `ServiceLevel` enum owns both the exact strings and their multipliers. `ServiceLevelRule` validates through `ServiceLevel.from(...)`, and `ServiceLevelMultiplier` reads the same enum, removing the duplicated `"EXPRESS"` literal that previously had to be kept in sync across two classes.
- **<u>Purpose-based packages:</u>** `validation`, `exception`, and `pricing` group related types without hiding multiple public types in one file.
- **<u>Failure handling at the boundary:</u>** `Demo` catches `ShippingCalculationException` and prints a clear error. The domain calculator never prints and never returns a fake price.

## <u>How new rules are absorbed</u>

Zone knowledge now lives in one place: a `ZonePolicy` implementation containing the zone name, rate per kg, and handling fee. `ZonePolicyRegistry` maps zone strings to policies, and `ShippingCalculator` depends only on the `ZonePolicyProvider` interface.

Adding the announced new zone means adding a new policy and registering it. `ShippingCalculator` is not edited. The `newPolicyCanBeRegisteredWithoutChangingTheCalculator` integration test proves this by adding a `MOON` policy to the registry and asserting the resulting cost.

The same pattern now covers the other two rule families: a new service level is an enum constant (validation and pricing both derive from it), and a new pricing adjustment is a `CostModifier` added to the injected list. Only `Demo` wires the concrete rules; the calculator stays closed for modification and open for extension.

## <u>What happened to the inherited code</u>

| Inherited item | Decision | Why |
| --- | --- | --- |
| Rate map in `InMemoryRateRepository` | **Rewritten** into `ZonePolicy` implementations | Base rate and zone fee were split across two classes; one policy now owns the complete zone rule. |
| `ShippingCalculator` zone `if/else` | **Removed** | It violated OCP and had to change for every new zone. |
| Base-rate arithmetic | **Kept** with the same values | The business rule was not wrong; only its structure and failure handling were. |
| Heavy-surcharge and EXPRESS `if` blocks in `calculateCost` | **Extracted** into `CostModifier` implementations | They made the calculator change for every new pricing rule and duplicated the `"EXPRESS"` string. |
| Console output inside calculation | **Removed** | Console I/O belongs in the demo/boundary, not the domain service. |
| Three AI-generated tests | **Rewritten** | They stubbed the system under test, asserted a primitive was non-null, or checked only `> 0`. |

## <u>Deliberate trade-offs</u>

- **<u>`double` for money:</u>** the inherited arithmetic uses `double` and the brief does not ask for currency precision. It was kept so the documented quotes (`50.0`, `967.5`) stay exact and the change stays focused on structure and failure handling. A real invoicing system should move to `BigDecimal` with an explicit rounding policy; that is a behaviour-changing decision for a separate task, not a silent swap here.
- **<u>No end-to-end tests:</u>** see the test strategy below — console formatting is not domain behaviour.

## <u>Improvement beyond the brief</u>

**<u>Parameterized edge-value tests</u>** cover invalid weights (`0`, negative, `NaN`, both infinities) and invalid service levels (null, blank, lowercase, unknown). This guards a class of numeric and string-input bugs the inherited suite never considered, and it makes the validation contract explicit in executable form.

A supporting safety guard is `List.copyOf` in `ShipmentValidator`: the rule list and returned errors are immutable, so callers cannot mutate validation behaviour or results after construction.

## <u>Test strategy (test pyramid)</u>

The suite is deliberately two-layered, with no end-to-end tests.

| Layer | Tests | What it proves |
| --- | --- | --- |
| **<u>Unit base (71):</u>** `ZoneRequiredRuleTest`, `WeightRuleTest`, `ServiceLevelRuleTest`, `ServiceLevelTest`, `ZonePolicyTest`, `HeavyWeightSurchargeTest`, `ServiceLevelMultiplierTest`, `ShipmentValidatorTest`, `ZonePolicyRegistryTest` | Each rule, enum, policy, and modifier in isolation: null/blank/lowercase/unknown strings, `0`/negative/`NaN`/`±∞`/valid weights, exact rates/fees/multipliers, the 30 kg boundary, rule ordering, immutability, duplicate-zone rejection. | Every leaf component honours its own contract, so a failure points to one class. |
| **<u>Unit with injected seam (22):</u>** `ShippingCalculatorTest` | Calculator orchestration with a mocked `ZonePolicyProvider` and mocked `CostModifier`s; exact costs, modifier application order, dependency-failure propagation, null-guard enforcement, and `verifyNoInteractions` proving validation short-circuits before lookup. | Calculator logic in isolation without standing up real collaborators. |
| **<u>Integration (12):</u>** `ShippingPricingIntegrationTest` | Real registry + real validator + real calculator: full zone/service matrix, 30 vs 31 kg boundary, combined surcharge and express, unknown zone, aggregated validation errors, and a policy added at runtime. | Wiring is correct and the domain behaves as a whole. |

Mockito is used only for collaborators whose behaviour is worth controlling: `ZonePolicyProvider` (failure injection and interaction verification), `ShipmentRule` (delegation order in `ShipmentValidator`), and `CostModifier` (application order in `ShippingCalculator`). The system under test is never mocked, and value types, enums, and concrete policies/modifiers are always real. A shared `TestFixtures` helper removes duplicated policy/validator/modifier wiring between test classes.

Where several asserts checked one concept (a threshold's two sides, an enum's two multipliers, a policy's three fields), the suite now uses `@ParameterizedTest` with `@CsvSource`/`@MethodSource`: one assert per case, so a failure names the exact input. Mockito order/interaction verifications stay grouped with their result assertion because the sequence has to be observed within a single invocation.

**<u>Deliberate exclusion:</u>** there is no test for `Demo`. Capturing stdout to assert console formatting would test presentation, not behaviour, so the 5 PIT mutants in `Demo.main`/`printResult` remain uncovered by design. All 30 mutants in the domain classes are covered.

## <u>Verification</u>

- `mvn test` — 105 tests passing, 0 failures/errors.
- `mvn verify` — JaCoCo report at `target/site/jacoco/index.html`; 18 classes analysed.
- PIT mutation coverage — 36 of 41 mutants killed (88%) with 100% test strength on covered mutants. The 5 uncovered mutants are confined to `Demo` console wiring (see above).
- Demo output unchanged — `50.0`, `967.5`, and `FAILED: Unknown shipping zone: MOON`.

## <u>Live review walkthrough</u>

1. **The defect first:** the inherited `catch (Exception) { return 0; }` made `MOON` look like a free quote, while the green suite contained one `> 0` assertion, one mock of the system under test, and one `assertNotNull` on a primitive.
2. **Failures before structure:** typed exceptions and an ordered validation rule chain; unknown zones and bad input now fail loudly with distinct, testable types.
3. **Separate the rules:** zone data → `ZonePolicy`; level strings and multipliers → `ServiceLevel`; weight/express adjustments → ordered `CostModifier`s. All collaborators are injected, so `ShippingCalculator` is orchestration only.
4. **Prove extensibility:** the integration test adds a `MOON` policy at runtime and asserts the quote without editing the calculator — the exact change the business announced.
5. **Trust the suite:** 105 tests across the pyramid, with exact-value assertions. PIT kills 36/41 mutants at 100% test strength on covered mutants; the 5 uncovered are `Demo` console wiring, excluded by choice and documented above.
