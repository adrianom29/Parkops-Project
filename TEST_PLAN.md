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