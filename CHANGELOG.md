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