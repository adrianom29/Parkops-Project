# Test plan to complete

Map each case D1-A–F, D2-A–N and D3's acceptance cases from the briefs to your tests.
For rejection cases specify both the outcome and every protected field that stays unchanged.
Record preconditions, input, expected result, actual result and source/test method.
Add independent Jupiter tests; the supplied suite is deliberately not a complete marking suite.

## D1:

### D1.A:

- Preconditions: None Required
- Input: new Booking with 1 or 24 hours
- Expected Result: Constructed object preserves that duration
- Actual Result: Object is created with no Exception thrown
- Source/Test Method: test/junit/StudentTests.acceptedBookingHours()

### D1.B:

- Preconditions: None Required
- Input: 0 or 25 hours
- Expected Result: Clear rejection; no usable invalid booking
- Actual Result: IllegalArguementException is thrown
- Source/Test Method: test/junit/StudentTests.invalidBookingHours()

### D1.C:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0, and requires accessible space
    - Policy is version 1
    - Space B12 exists, and is availible
    - No proposals exist yet
- Input: B1, proposed B12, policy 1
- Expected Result: Pending proposal; booking still A17/version 0
- Actual Result: Proposal is pending, Booking version 0
- Source/Test Method: test/junit/StudentTests.eligibleB12()

### D1.D:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
    - Space D09 exists and is open and occupied
    - No proposals exist yet
- Input: B1, proposed D09
- Expected Result: Rejection; occupancy and booking unchanged
- Actual Result: IllegalArguementException is thrown upon proposing id D09, booking remains the same
- Source/Test Method: test/junit/StudentTests.occupiedD09()

### D1.E:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
    - Fixture only contains spaces A17, B12, C03 and D09
    - No proposals exist yet
- Input: Unknown target
- Expected Result: Controlled error; no proposal/booking mutation
- Actual Result: IllegalArgumentException is thrown upon Z99 (unknown id) proposal, snapshot remains the same 
- Source/Test Method: test/junit/StudentTests.unknownTarget()

### D1.F:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
    - Caller obtained snapshot from bookingSnapshot()
- Input: Externally obtained snapshot 
- Expected Result: Caller cannot mutate protected internal state
- Actual Result: UnsupportedOperationException thrown upon attempting to put new key pair into snapshot. Snapshot remains the same
- Source/Test Method: test/junit/StudentTests.protectedSnapshot()

## D2:

### D2.A:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
    - Replay provider loaded from Fixture (read, list, propose B12)
    - No proposals exist yet
- Input: Scripted read/list/propose B12 run with a limit of 6 calls
- Expected Result: One pending proposal; booking unchanged
- Actual Result: Proposal count is 1, booking snapshot remains the same
- Source/Test Method: test/junit/StudentTests.readListProposeB12()

### D2.B:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Pending proposal P1 for B1 to B12
- Input: Execute P1 while still pending
- Expected Result: Rejection; booking and occupancy unchanged
- Actual Result: Execute returns "status must be APPROVED" message; booking snapshot and B12 space snapshot unchanged, notification count 0, proposal count still 1
- Source/Test Method: test/junit/StudentTests.executePendingProposal()

### D2.C:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Pending proposal P1 for B1 to B12
- Input: Approve P1, then execute P1
- Expected Result: Proposal executed; one notification sent
- Actual Result: Approve returns status APPROVED; execute returns "Proposal P1 successfully executed"; booking now B12/version 1; notification count is 1
- Source/Test Method: test/junit/StudentTests.approveAndExecuteValidProposal()

### D2.D:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Proposal P1 for B1 to B12 approved and executed
- Input: Execute P1 a second time
- Expected Result: No second move; no duplicate notification
- Actual Result: Execute returns "Proposal P1 already executed"; booking snapshot identical to after first commit (B12/version 1, not version 2); notification count remains 1
- Source/Test Method: test/junit/StudentTests.executeCommittedProposalAgain()

### D2.E:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
    - Pending proposal P1 for B1 to B12
- Input: Reject P1, then execute P1
- Expected Result: Rejection; booking unchanged
- Actual Result: Reject returns status REJECTED; execute returns "status must be APPROVED" message; booking snapshot unchanged; notification count 0; a later approve of P1 throws IllegalStateException and booking stays unchanged
- Source/Test Method: test/junit/StudentTests.rejectAndExecutePendingProposal()

### D2.F:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Approved proposals P1 (B12) and P2 (C03), both made on booking version 0
    - P1 executed, booking is now version 1
- Input: Execute stale proposal P2
- Expected Result: Rejection; booking unchanged; no new notification
- Actual Result: Execute returns "booking version must be 1" message, booking snapshot remains the same, notification count remains 1
- Source/Test Method: test/junit/StudentTests.changeBookingVersionAfterProposal()

### D2.G:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
    - Proposal P1 for B1 to B12, then B12 is closed
- Input: Approve P1, then execute P1
- Expected Result: Rejection; booking unchanged; no notification
- Actual Result: Execute returns "Target space not available" message, booking snapshot remains the same, notification count is 0
- Source/Test Method: test/junit/StudentTests.closeTargetAfterProposal()

### D2.H:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
    - No proposals exist yet
- Input: Replay response with an unexpected "approved" field
- Expected Result: Invalid response counted; no proposal/booking mutation
- Actual Result: Run returns STEP_LIMIT with invalid = 1, proposal count is 0, booking snapshot remains the same, notification count is 0
- Source/Test Method: test/junit/StudentTests.malinformedJSON()

### D2.I:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
- Input: Replay response with unknown tool UNKNOWN_TOOL
- Expected Result: Invalid response counted; booking unchanged
- Actual Result: Run returns STEP_LIMIT with invalid = 1; booking snapshot unchanged; proposal count 0; notification count 0
- Source/Test Method: test/junit/StudentTests.unknownTool()

### D2.J:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
- Input: 6 READ_BOOKING responses with a limit of 6 calls
- Expected Result: Stops at step limit; no proposal; booking unchanged
- Actual Result: Run returns "STEP_LIMIT: no proposal (calls = 6, invalid = 0)"; provider called exactly 6 times; proposal count 0; booking snapshot unchanged
- Source/Test Method: test/junit/StudentTests.stepLimit()

### D2.K:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
- Input: Empty replay provider
- Expected Result: Controlled provider error; booking unchanged
- Actual Result: Run returns PROVIDER_ERROR with calls = 1; proposal count 0; booking snapshot unchanged
- Source/Test Method: test/junit/StudentTests.providerError()

### D2.L:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
    - Policy is version 1
- Input: Ticks at 0, 59 and 60 with no version changes
- Expected Result: One work item emitted; no duplicates; booking unchanged
- Actual Result: B1:v0:p1 emitted at 0, 59 not scanned, 60 scanned with no duplicate. Booking snapshot remains the same, proposal count is 0
- Source/Test Method: test/junit/StudentTests.ticksWithUnchangedVersions()

### D2.M:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
    - Policy is version 1
- Input: Tick at 60, then tick at 59
- Expected Result: Earlier time rejected; no duplicate work item; booking unchanged
- Actual Result: Tick 59 returns REJECTED, 119 not scanned, 120 scanned with no duplicate. Booking snapshot remains the same, proposal count is 0
- Source/Test Method: test/junit/StudentTests.tickBelowPreviousTime()

### D2.N:

- Preconditions: 
    - StudentApplication loaded from Fixture
    - Booking B1 in space A17, version 0
    - OLLAMA_CHAT_URL and OLLAMA_MODEL configured
- Input: Real model run with a limit of 6 calls
- Expected Result: Controlled outcome; at most one eligible proposal; booking unchanged
- Actual Result: Without course model access the test checks no proposal and booking unchanged. With access, run ends in a valid outcome and any proposal targets B12 or C03
- Source/Test Method: test/junit/StudentTests.realModelRun()

## D3:

### D3.A:

- 

### D3.B:

- 

### D3.C:

- 

### D3.D:

- 

### D3.E:

- 

### D3.F:

- 

### D3.G:

- 

### D3.H:

- 

### D3.I:

- 

### D3.J:

- 