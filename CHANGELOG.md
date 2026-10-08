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
    - Testing with B12, D09, and Z99 (unknown target)
- Created results/d1.txt and filled it with commands and their outputs