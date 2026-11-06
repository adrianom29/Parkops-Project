# Change log

## Starter 2026-09-15
Supplied infrastructure and unfinished exercise boundary.

## Student changes
Record D1 implementation, D1 feedback fixes, D2 additions and D3 changes here.

## D1

### 2026-10-05
- Error handling for DomainRules.requireDuration and DomainRules.requireIdentifier
- created Booking class and its constructor 
- made getters in Booking class
- Created Space class and its constructor
- Made getters in space class as well as isAvailable method
- Created Proposal class with its constructor
- Made getter methods for proposal class
- Created ProposalStatus.java which is an enum for all possible statuses (PENDING, APPROVED, REJECTED, EXECUTED)
- Made constructor in StudentAppliction.java where booking and four spaces are built from Fixture.java instead of map used by starter. 
- Implemented bookingSnapshot() and propose() methods in StudentAppliction.java

### 2026-10-06
- Made JUnit test case for valid booking hours (1 and 24)
- Made JUnit test case for invalid booking hours (0 and 25)
- Made JUnit test case for invalid booking id and space id
- Made JUnit test case for eligible B12
- Made JUnit test case for occupied D09
- Made JUnit test case for null/unknown targetID
- Made JUnit test case for unchanged booking state after proposing or rejecting
- Designed UML Diagram
- Designed Sequence Diagram

### 2026-10-06
- Filled out TEST_PLAN.md for D1.A-F
- Updated Main.java for D1.3 
    - Testing with B12, D09, and unknown target (Z99)
- Created results/d1.txt and filled it with commands and their outputs

### 2026-10-07
- Started working on D1.5
    - Class responsibilties
    - Reused and Implemented Infrastructure
    - Diagrams

### 2026-10-08
- Finished D1.5
    - Alternative considered
    - Operation contracts
    - Current Limitation
- Put copies of all files/folders for sumbission in a zip file and submitted

## D2

### 2026-10-24
- Restricted limit to be between 1 and 6 in StudentApplication.run(). Throws IllegalArgumentException if out of range.

### 2026-10-26
- StudentApplication.run() rejects null providers
- Made system message and added to ChatMessage history list
- started loop
    - called provider.next(history) and added result to history as assistant message
    - Implemented cases for READ_BOOKING tool
- made parseRequest helper method in StudentApplication.java

### 2026 10 27
- In run() tool loop:
    - FINISH returns NO_ACTION without changing a booking
    - LIST_ALTERNATIVES requires a preceding read and returns candidates from the current state; reject or restart the investigation if the captured versions are stale. 
    - PROPOSE_REASSIGNMENT requires the read and list steps, validates the chosen target and captured versions, creates one pending proposal, and returns AWAITING_APPROVAL.
    - Tool loop rejects unknown tool
- Wrote junit test case D2-A

### 2026 10 28
- Started execute(), approve(), and reject() functions in StudentApplication
- added toView() function to Proposal.java that creates ProposalView object
- Wrote junit test case D2-B
- combined approve() and reject() into evaluate(String, proposalId, boolean approve) to avoid duplicate code
- Wrote junit test case D2-C 
- Wrote junit test case D2-D
- Wrote junit test case D2-E
- Wrote junit test case D2-I
- Wrote junit test case D2-J
- Wrote junit test case D2-K

### 2026 10 29
- Made specific IllegalStateException in evaluate() when proposal has already been appoved or rejected.
- Added policy check in execute()
- Added booking check in execute()
- Added open and close setters in Space.java
- Added openSpace and closeSpace methods in StudentApplication.java for client calls
- Added setBookingVersion setter in Proposal.java
- Wrote junit test case D2-F
- Wrote junit test case D2-G
- Wrote junit test case D2-H

### 2026 10 30
- tick() method in StudentApplication.java

### 2026 11 1
- retry() method in StudentApplication.java
- Wrote junit test case D2-L
- Wrote junit test case D2-M
- Wrote junit test case D2-N

### 2026 11 2
- Created and filled in results/d2.txt with outputs of tests (all passed)
- Added result of ./run.ps1 run live command into results/d2.txt
- Wrote test plans for all D2 test cases A-N in TEST_PLAN.md

### 2026 11 4
- Added result of ./run.ps1 run scripted and ./run.ps1 run agent-scripted commands into results/d2.txt
- Changes to Junit test cases
- Added new result of ./run.ps1 run live command into results/d2.txt for updated junit tests
- D2.4 refactor: split StudentApplication into four layers behind clear boundaries (public methods and outputs unchanged)
    - Model protocol: Tool (enum of the four tools), ToolRequest (validated request), ToolProtocol (prompt text, reply parsing/validation, observation formatting)
    - Coordination: AgentRunner (bounded loop + observation history), ReassignmentTools (read/list/propose only, no approve/execute), PeriodicScanner (ticks, work items, retry); StudentApplication is now a facade that owns the operator approve/reject/execute flow
    - Domain rules: ReassignmentPolicy (eligibility, execution rechecks, closed-space detection); Proposal now owns its state transitions (approve/reject/markExecuted) and Booking.reassignTo() replaces setSpaceId/setVersion; removed Proposal.setStatus/setBookingVersion
    - Data storage: ParkingInventory (booking + spaces), ProposalStore (proposals + ids), NotificationLog (one notification record per committed proposal instead of an int counter)
- Runned Junit tests again to confirm they all still pass after refactor 

### 2026 11 5
- Work on DESIGN.pdf:
    - Component Explanation section chart which displays all layer names (Coordination, Model Protocol, Domain Rules, and Data Storage), classes, and responisbilities
    - Dependency Explanation
    - State Transition Table that demonstrates lifestyle of Proposal
    - Two Design Desisions: 
        1. Versions are captured at proposal time and rechecked during execution
        2. Model is kept away from approval and execution
    - Unsuccessful or Rejected model/tool Interaction
    - Limitations
- Made Gemini project and created an API key. Used Gemini API through implemented coordinator