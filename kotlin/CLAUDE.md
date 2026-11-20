# CLAUDE.md — agent rules for the Kotlin solution

Project instructions loaded automatically by Claude Code when working in `kotlin/`.

## Context
- Take-home kata: XA Bank time deposits. Requirements: `../README.md`.
- Stack: Kotlin 1.9, Spring Boot 3.3, Maven, PostgreSQL, Flyway, JPA, Testcontainers, OpenAPI (contract-first).

## Hard constraints (never break)
- `org.ikigaidigital.TimeDeposit`: do not change fields, types, package or constructor.
- `TimeDepositCalculator.updateBalance(xs: List<TimeDeposit>)`: signature and behaviour fixed.
  `TimeDepositCalculatorTest` is the characterization suite — it must stay green unchanged
  unless a test is added. Any refactoring of the calculator runs it first and last.
- Exactly two endpoints: `GET /time-deposits`, `POST /time-deposits/balance-updates`. No others.
- No input validation / error handling work (out of scope per requirements).

## Architecture (hexagonal)
- Domain (framework-free): `org.ikigaidigital` root + `org.ikigaidigital.interest`.
- Application: `application.port.inbound` (use cases), `application.port.outbound` (repositories), `application.service`.
- Adapters: `adapter.inbound.web` (implements generated `TimeDepositsApi`), `adapter.outbound.persistence` (JPA).
- Dependencies point inwards only. Domain never imports Spring/JPA.
- New interest rule = new `InterestPolicy` implementation; do not add `if/else` on plan types to the calculator.

## Contract-first
- Edit `src/main/resources/static/openapi.yaml` first; the API interface and DTOs are generated
  (`mvn generate-sources`). Never hand-write controllers that bypass the generated interface.
- Package names must not contain Kotlin keywords (`in`, `out` broke the generator → `inbound`/`outbound`).

## Workflow
- Small, atomic commits (Conventional Commits). Build must pass at each step.
- Only Docker may be required locally: app via `docker compose up --build`, tests via `docker compose run --rm tests`.
- Fetch library docs through the Context7 MCP server instead of relying on memory.
- No explanatory comments: names must explain the code. Only exception: a comment justifying an assumption
  (prefixed `Assumption:`), also listed in README.md.
- Money: `BigDecimal` at the DB and API boundary; `Double` only inside the shared domain class.
