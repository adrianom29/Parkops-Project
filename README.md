# ParkOps — supervised parking reassignment

EECS 3311 B, Fall 2026, individual project (D1–D3).

A booking (B1) sits in a closed space (A17). A model may read the booking, list
eligible spaces and propose a move. A human operator approves or rejects the
proposal, and a separate execute step rechecks it before moving the booking once.
All data is synthetic and all notifications are simulated.

## Requirements

- JDK 21 (scripts compile with `--release 21`). They use `COURSE_JAVA_HOME`, then
  `JAVA_HOME`, then the `javac` on `PATH`.
- JUnit Jupiter 6.0.3, only for `test`.
- Bash (macOS, Linux or Git Bash), or Windows PowerShell.

## Commands

Run from the project root.

    bash run.sh compile
    bash run.sh lint                 # -Xlint:all
    bash run.sh check                # plain Java checks, no JUnit needed
    bash run.sh smoke                # infrastructure checks, local fake HTTP only
    bash setup-junit.sh              # once; downloads and verifies the JUnit jar
    bash run.sh test                 # JUnit tests

    bash run.sh run d3-demo          # D3: policy change demonstration
    bash run.sh run agent-scripted   # D2: agent loop with replayed replies
    bash run.sh run scripted         # D1: pending proposal and rejections
    bash run.sh run                  # prints the synthetic fixture
    bash run.sh run live             # agent loop with Gemini (needs a key)

In PowerShell, replace `bash run.sh` with `./run.ps1`, and `bash setup-junit.sh`
with `./setup-junit.ps1`. To use an already cached JUnit jar, set `JUNIT_JAR` to
its absolute path.

In Git Bash on Windows, if `test` reports that `org.junit.jupiter.api` does not
exist, give the jar as a relative path:

    JUNIT_JAR=lib/junit-platform-console-standalone-6.0.3.jar bash run.sh test

All modes except `live` are deterministic and call no model.

### Live model

`run live` calls the Google Gemini API. The key is read from `GEMINI_API_KEY` in a
`.env` file in the project root, then from the environment variable of that name,
and otherwise is asked for at a hidden prompt. `GEMINI_MODEL` optionally overrides
the default model, `gemini-3.5-flash-lite`. Never submit `.env` or a key.

One JUnit test (D2-N) uses an Ollama endpoint when `OLLAMA_CHAT_URL` (ending
`/api/chat`) and `OLLAMA_MODEL` are both set.

## Supported scenarios

- **Fixture:** booking B1 (requires an accessible space, in closed A17) and spaces
  A17, B12, C03 and D09.
- **Policy 1 (default):** a target must be known, open, unoccupied and not the
  current space. Eligible for B1: B12 and C03.
- **Policy 2 (D3):** also requires an accessible target when the booking requires
  one. Eligible for B1: C03 only. A booking that does not require accessibility
  keeps B12 and C03.
- **Agent loop:** `READ_BOOKING`, `LIST_ALTERNATIVES`, `PROPOSE_REASSIGNMENT` and
  `FINISH`, with 1 to 6 model calls. It can create a pending proposal but cannot
  approve or execute.
- **Operator workflow:** approve or reject once; execute only an approved proposal
  whose booking version, policy version and target still pass; a commit releases the
  old space and occupies the target; repeating an
  execution does not move the booking or notify again.
- **Periodic checks:** caller-supplied ticks with a 60-second boundary, one work
  item per booking, booking version and policy version, and operator retry.

Approval, rejection, execution, ticks, retry and policy activation are methods on
`StudentApplication`; `d3-demo` and the tests call them.

## Project layout

| Area | Files in `src/` |
|---|---|
| Entry points | `Main`, `StudentApplication` |
| Coordination | `AgentRunner`, `AttemptBudget`, `PeriodicScanner`, `ReassignmentTools` |
| Model protocol | `ToolProtocol`, `Tool`, `ToolRequest` |
| Domain | `ReassignmentPolicy`, `DomainRules`, `Booking`, `Space`, `Proposal`, `ProposalStatus` |
| Storage | `ParkingInventory`, `ProposalStore`, `NotificationLog` |
| Model providers | `ModelProvider`, `ReplayProvider`, `GeminiProvider`, `GeminiCredentials`, `OllamaProvider` |

Tests are in `test/junit/` and `test/plain/`. Test cases are mapped in
`TEST_PLAN.md`, captured output is in `results/`, and the design is explained in
`DESIGN.pdf`.

## Known limitations

- One booking and four spaces, held in memory. Nothing persists between runs.
- No interactive operator interface.
- `run agent-scripted` and `run live` start at policy 1; neither has an option to
  activate policy 2.
- Policy 2 cannot be switched back to policy 1 within a run.
- Not thread safe.
- All classes are in the default package, so protection of domain objects between
  layers is a convention rather than a compiler guarantee (see `results/d3.txt`,
  D3.3).
- OpenJML has not been run. The contracts on `AttemptBudget` and
  `DomainRules.requireIdentifier` are supported only by a manual argument and
  ordinary tests, and cover only those operations.
- Without the two Ollama variables, D2-N returns early and is reported as passed
  **without calling any model**. A passing test run is not evidence of live model
  behaviour.

## Reused starter files and attribution

Supplied by the course starter: `ChatMessage`, `ModelProvider`, `ProposalView`,
`ReplayProvider`, `OllamaProvider`, `MiniJson`, `Fixture`, the build and JUnit
setup scripts, `data/`, `test/plain/`, `InfrastructureTest` and
`SuppliedChecksTest`. `Main`, `StudentApplication`, `DomainRules` and
`StudentTests` began as starter skeletons that were then implemented.

`GeminiProvider` and `GeminiCredentials` come from the course Gemini connection
pack; `GeminiCredentials` was extended to also read `.env` and the environment.

See `STARTER_PROVENANCE.md` for the starter's origin and `AI_USE.md` for AI
assistance. `START_HERE.md` is the original starter guidance, not a description of
the finished project.
