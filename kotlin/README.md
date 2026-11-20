# XA Bank Time Deposit — Kotlin solution

Solution for the time deposit refactoring kata, built with Kotlin 1.9, Spring Boot 3.3, JPA,
PostgreSQL, Flyway, Testcontainers and a contract-first OpenAPI specification.

## Prerequisites

- Docker (with Docker Compose). Nothing else — JDK and Maven run inside containers.

## Run the application

```bash
cd kotlin
docker compose up --build
```

Builds the app image (multi-stage `Dockerfile`), starts PostgreSQL and the app on http://localhost:8080.
Flyway creates the schema and loads sample data (`src/main/resources/db/sample-data`).
Stop with `docker compose down`.

## Run the tests

```bash
cd kotlin
docker compose run --rm tests
```

Runs `mvn verify` in a Maven container; the integration tests start PostgreSQL via Testcontainers using
the host Docker socket. Covered: calculator characterization tests, unit tests for the application and
web layers, end-to-end tests (HTTP → application → JPA → PostgreSQL).

With a local JDK 17 + Maven the same works via `mvn verify`.

## Using the API (Swagger)

The contract lives in [`src/main/resources/static/openapi.yaml`](src/main/resources/static/openapi.yaml)
and is served at http://localhost:8080/openapi.yaml.

1. Open **Swagger UI**: http://localhost:8080/swagger-ui.html (static page from the `swagger-ui` webjar that loads
   `openapi.yaml`; no API docs are generated from code, so the app exposes only the two contract endpoints)
2. **`GET /time-deposits`** → *Try it out* → *Execute*: returns one page
   `{items, page, size, totalElements, totalPages}`; `items` are deposits ordered by id with
   `id`, `planType`, `balance`, `days`, `withdrawals`. Parameters: `page` (zero-based, default 0) and
   `size` (default 100, max 1000).
3. **`POST /time-deposits/balance-updates`** → *Try it out* → *Execute*: applies this month's interest
   to every deposit that has not received it yet (`204 No Content`). Calling it again in the same month
   changes nothing.
4. Run `GET /time-deposits` again to see the new balances.

Same with curl:

```bash
curl "http://localhost:8080/time-deposits?page=0&size=2"
curl "http://localhost:8080/time-deposits?page=1&size=2"
curl -X POST http://localhost:8080/time-deposits/balance-updates
```

## Design

### Hexagonal architecture

```
adapter.inbound.web            REST: implements TimeDepositsApi generated from openapi.yaml
        │
application.port.inbound       GetTimeDepositsQuery, UpdateAllBalancesUseCase
application.service            use case implementations
application.port.outbound      TimeDepositRepository
        │
adapter.outbound.persistence   Spring Data JPA reads, JDBC batch writes; implements the outbound port

domain (framework-free)        TimeDeposit, TimeDepositCalculator, Withdrawal, TimeDepositDetails,
                               interest.* (InterestPolicy strategies)
```

The domain stays in the original `org.ikigaidigital` package because moving `TimeDeposit` would be a
breaking change.

### Interest calculation (Strategy + Template Method)

- `InterestPolicy` — one strategy per plan type, picked by `TimeDepositCalculator` via a map lookup.
- `MonthlyRateInterestPolicy` — template for "yearly rate paid monthly"; holds the 30-day grace period
  shared by all existing plans. `Basic`/`Student`/`Premium` only declare rate and extra eligibility.
- Adding a plan or a different rule for a plan = a new `InterestPolicy`, no change to the calculator
  (Open/Closed). Duplicate policies for one plan type are rejected. The calculator also accepts a custom
  policy list via its constructor; the no-arg constructor keeps the original API.
- `monthlyInterest(deposit): BigDecimal` exposes the rounded interest; `updateBalance` adds exactly this
  amount, so its behaviour is unchanged.
- `updateBalance` signature and behaviour are unchanged. This is proven by characterization tests written
  **before** the refactoring (`TimeDepositCalculatorTest`), including the floating-point rounding quirk
  (`1818.00 * 0.01 / 12` rounds to `1.51`, not `1.52`, because the interest is computed in `Double`).

### Database

Flyway migration `V1__create_time_deposit_tables.sql` creates `timeDeposits` and `withdrawals` with the
column names from the requirements (camelCase, quoted identifiers). `V2` adds `lastInterestMonth`
(see idempotency below). Hibernate runs with `ddl-auto: validate`.

### Scalability and idempotency

- **GET**: page-based pagination ordered by the primary key; a page costs three queries regardless of
  its size: the page, the total count and the page's withdrawals (one `IN` query, no N+1).
- **POST**: deposits are processed in batches (`time-deposits.balance-update.batch-size`, default 500),
  each batch in its own short transaction and written with one JDBC batch of conditional updates:
  `balance = balance + interest, lastInterestMonth = :month WHERE lastInterestMonth < :month`.
  The condition makes the update idempotent: repeated, parallel or resumed (after a failure) calls
  credit each deposit at most once per month. Covered by integration tests, including parallel calls.

### Assumptions

Each assumption is also justified in a comment next to the code it affects.

- **Database**: the requirements do not name one; PostgreSQL is used, matching the Testcontainers setup.
- **Update endpoint** is `POST /time-deposits/balance-updates` (it triggers a run rather than replacing a
  resource) and returns `204`.
- **Interest is monthly**, so it is applied at most once per deposit per calendar month (UTC); the month is
  stored in `lastInterestMonth`. `days` is not advanced by the update — the original calculator does not do
  it and the requirements do not ask for it.
- **Pagination**: GET returns a page (default 100, max 1000; out-of-range `page`/`size` are clamped) to keep
  the response bounded, with paging metadata next to `items`; each item has exactly the required schema.
- **Unknown plan types** earn no interest, as in the original implementation.
- **Student plan "no interest after 1 year"** = from day 366 on, as in the original implementation.
- **Money**: `BigDecimal` everywhere outside the shared `TimeDeposit`, stored as `DECIMAL(19,2)`. The only
  `Double` conversion is `BigDecimal → TimeDeposit` as input to the interest calculation; the calculator
  returns the interest as `BigDecimal` cents and the balance is updated in SQL, so stored and returned
  balances never go through `Double` and keep full precision (tested with `90071992547409.93`).
- **Withdrawals** are read-only data for the GET endpoint; they do not affect interest (not required).
  They are kept outside `TimeDeposit` (read model `TimeDepositDetails`) so the shared class is unchanged.

## AI-assisted development

### Setup

| Tool | Role |
|------|------|
| [Claude Code](https://claude.com/claude-code) (CLI, Windows) | coding agent: reads repo, edits files, runs Maven, Docker and git |
| Model: Claude Opus 5.5 (`claude-opus-5-5`) | LLM behind the agent |
| [Context7 MCP server](https://github.com/upstash/context7) | fetches current library docs (used for openapi-generator `kotlin-spring` options) |
| [`CLAUDE.md`](CLAUDE.md) | project rules loaded into every agent session (constraints, architecture, workflow) |
| Global rule `~/.claude/rules/context7.md` | "use Context7 for any library/framework question instead of memory" |
| caveman plugin | terse agent output to save tokens; no effect on code |

Reproduce: install Claude Code, add the Context7 MCP server (`claude mcp add context7 -- npx -y @upstash/context7-mcp`),
open a terminal in `kotlin/` and run `claude`. `CLAUDE.md` is picked up automatically.

Prompt used to start: *"read the readme and do the assignment, it must be in Kotlin"*.

### Agent workflow

1. Agent read the requirements and the existing Kotlin code, checked the toolchain.
2. Wrote characterization tests for the untouched calculator first — this caught the `Double` rounding
   quirk, which was then pinned as expected behaviour.
3. Refactored the calculator to strategies with the characterization suite as the safety net.
4. Built the contract, layers and adapters step by step, compiling after each step, then ran the
   Testcontainers tests and a manual smoke test (`docker compose up` + curl + Swagger UI).
5. Review feedback applied: comments only where they justify an assumption; only Docker is needed to
   run the app and the tests locally.
6. Commits kept few and focused (see `git log`).

### What was AI-assisted and why

- **Generated by the agent**: characterization tests, the calculator refactoring, OpenAPI contract,
  Maven and Docker setup, persistence/web adapters, Flyway scripts, tests, this README.
  Reason: mostly well-known patterns and boilerplate where an agent is fast, and every step is
  verified automatically (unit tests, Testcontainers tests, smoke test).
- **Author's part**: defining the task and the preferred stack, the rules in `CLAUDE.md`, reviewing
  the commits and the assumptions listed above.
- Issues the agent hit and fixed on its own: the generator produces invalid Kotlin for packages named
  `in`/`out` (renamed to `inbound`/`outbound`); a characterization test expectation that assumed
  "mathematical" rounding was corrected to the real `Double` behaviour.
