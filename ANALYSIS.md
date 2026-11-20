# <u>Analysis of the Inherited Code and Tests</u>

## <u>Most serious problem</u>

**<u>Failures are swallowed and converted into a valid-looking price of `0.0`.</u>**

`ShippingCalculator.calculateCost` wraps the whole calculation in `catch (Exception e) { return 0; }`. An unknown zone, a null service level, or a repository failure all produce the same silent result. Callers cannot distinguish "this shipment costs nothing" from "the calculation failed", no diagnostic is emitted, and the demo prints `0.0` for the `MOON` case as if it were a real quote.

### <u>Why it is the most serious</u>

It turns every other bug into a plausible business answer. It is also why the inherited suite can stay green while proving almost nothing: two tests check only that a value is positive or non-null, and the third stubs the value it asserts.

## <u>Code smells, anti-patterns, and SOLID violations</u>

### <u>SOLID violations</u>

- **<u>Single Responsibility Principle (SRP):</u>** `ShippingCalculator` owns rate lookup, zone fee rules, the heavy-item surcharge, the express multiplier, console output, and exception policy. A change to any one of these forces edits to the same class.
- **<u>Open/Closed Principle (OCP):</u>** zone rules live in an `if/else` chain inside `calculateCost`, while base rates live in a separate map. Adding the announced new zone requires editing existing control flow rather than adding a new rule.
- **<u>Dependency Inversion Principle (DIP):</u>** the calculator depends on the `RateRepository` abstraction in name only — it constructs `new InMemoryRateRepository()` itself in a private final field. There is no injection seam, so callers and tests cannot supply another implementation.

### <u>Smells and anti-patterns</u>

- **<u>Hard-wired dependency:</u>** the class cannot be unit-tested in isolation; Mockito can only be used to mock the class under test.
- **<u>Broad catch-and-return-zero:</u>** no custom exception, logging, or error signal. Failure is hidden rather than handled.
- **<u>No input validation:</u>** negative, zero, or `NaN` weight and null or lowercase service levels are silently priced instead of rejected.
- **<u>Stringly-typed inputs and magic values:</u>** raw uppercase strings and unnamed numbers (`10`, `25`, `30`, `20`, `1.5`) are scattered through the method; typos silently change pricing.
- **<u>Console I/O in the domain layer:</u>** `System.out.println` couples the calculation to presentation and makes tests noisy.
- **<u>Split policy:</u>** a zone's base rate is in `InMemoryRateRepository`, but its fee rules are in `ShippingCalculator`. There is no single place that describes the complete rule set for a zone.

## <u>Why the inherited tests do not prove anything</u>

| Test | What it checks | Verdict |
| --- | --- | --- |
| `calculatesACost` | Asserts `cost > 0` for DOMESTIC/10kg/STANDARD (correct answer: `50`). | **<u>Weak.</u>** It would pass for `1`, `70`, or `967.5`; it only catches the coarsest failure such as `0`. |
| `calculatesExpressCost` | Mocks `ShippingCalculator`, stubs `calculateCost(...)` to return `967.5`, then asserts `967.5`. | **<u>Empty.</u>** Production code never runs; any implementation would pass. |
| `returnsACostForAShipment` | Calls `assertNotNull(cost)` on a primitive `double`. | **<u>Vacuous.</u>** The value is autoboxed to `Double`, so it can never be null. It passes for `0` or `-1`. |

No test covers unknown zones, invalid inputs, the heavy-surcharge boundary (30kg vs 31kg), express interaction, or whether a repository failure is surfaced instead of swallowed. As a result, mutation testing would leave many mutants alive.

## <u>Ranking of problems</u>

1. **<u>Swallowed exception returning `0`</u>** — a correctness and observability failure that hides every other bug and makes the green suite misleading.
2. **<u>Hard-wired repository</u>** — the root cause of the untestable design and the mock-the-SUT tests.
3. **<u>Scattered zone rules and magic values</u>** — blocks the coming new zone from being added without rewriting existing logic.
