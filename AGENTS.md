# <u>AGENTS.md</u>

## <u>What this repo is</u>

- **<u>Applied lab starter:</u>** a shipping-cost service whose code and tests pass on purpose; the job is to review and improve it, not rewrite it from scratch.
- **<u>Grading weights:</u>** analysis 20%, refactoring 18%, exception handling 13%, tests 22%, reasoning 15%, one extra improvement 12%. Compiling plus green tests is not the goal.

## <u>Workflow required by the brief</u>

- **<u>Before changing code:</u>** write down and share an analysis with a reviewer — code smells/SOLID violations, which AI-generated tests pass without proving anything, and which problem is most serious.
- **<u>Deliverables:</u>** written analysis, refactored code, robust exception handling, JUnit 5 + Mockito tests, rationale (what was kept, fixed, or rewritten and why), and one justified improvement beyond the brief. Put the written parts in a README or short markdown doc.

## <u>Commands</u>

- **<u>Toolchain:</u>** JDK 21 + Maven; there is no `mvnw`. Use `mvn` from PATH.

| Purpose | Command | Output |
| --- | --- | --- |
| Demo | `mvn compile && mvn exec:java -Dexec.mainClass=com.tidewater.freight.Demo` | — |
| Tests | `mvn test` | — |
| Coverage | `mvn verify` | `target/site/jacoco/index.html` |
| Mutation testing | `mvn test-compile org.pitest:pitest-maven:mutationCoverage` | `target/pit-reports/index.html` |

- **<u>PIT note:</u>** pinned in `pom.xml` with `pitest-junit5-plugin`; targets are `com.tidewater.freight.*`.

## <u>Design constraints</u>

- **<u>Extensibility:</u>** zone rules live in `pricing` as `ZonePolicy` implementations registered in `ZonePolicyRegistry`; adding a zone means adding a policy, not editing `ShippingCalculator`.
- **<u>Structure:</u>** `validation`, `exception`, and `pricing` packages; dependencies are constructor-injected. Keep failures explicit — no catch-all returning a fake price.
- **<u>Test approach:</u>** use injected seams and assert real behaviour and exact values. Never mock the system under test or use `assertNotNull` on primitives.
- **<u>Test pyramid:</u>** a large unit base, a smaller Mockito-seam layer, and a small real-collaborator integration layer. No end-to-end/stdout tests; see `RATIONALE.md`.
- **<u>Mockito scope:</u>** mock only collaborators whose behaviour must be controlled (e.g. `ZonePolicyProvider` failure injection, `ShipmentRule` delegation order); use real objects for policies, records, and enums.
- **<u>Domain strings:</u>** known zones are uppercase `DOMESTIC`, `REGIONAL`, `INTERNATIONAL`; `EXPRESS` is the only service-level modifier.
- **<u>Written work:</u>** `ANALYSIS.md` and `RATIONALE.md` are the sources of truth for the review and refactoring decisions.
