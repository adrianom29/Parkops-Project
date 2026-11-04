// Add focused @org.junit.jupiter.api.Test methods here.
// Create fresh objects for each test and assert results AND unchanged state on rejection.
// The supplied wrapper is not a substitute for your own test design.

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

public class StudentTests { 

/*
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                            B1 test cases (A-F)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*/

    @Test
    void acceptedBookingHours(){
        try {
            Booking b1 = new Booking("TEST", "TEST", 1, 0, false);
            Booking b2 = new Booking("TEST", "TEST", 24, 0, false);
            assertEquals(1, b1.getDuration());
            assertEquals(24, b2.getDuration());
        } 
        catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void invalidBookingHours(){
        assertThrows(IllegalArgumentException.class, () -> new Booking("TEST", "TEST", 0, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Booking("TEST", "TEST", 25, 0, false));
    }

    @Test
    void nullAndInvalidIdentifiers(){
        assertThrows(IllegalArgumentException.class, () -> new Booking(null, "TEST", 10, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Booking("  ", "TEST", 10, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Booking("123INVALID", "TEST", 10, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Booking("TOO-LONGGGGGGGGGG", "TEST", 10, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Booking("HasLowercase", "TEST", 10, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Booking("TEST", null, 10, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Booking("TEST", "  ", 10, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Booking("TEST", "123", 10, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Booking("TEST", "TOO-LONGGGGGGGGGG", 10, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new Booking("TEST", "HasLowercase", 10, 0, false));
    }

    @Test 
    void occupiedD09(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();
        Map<String, Object> d09Before = sApp.spaceSnapshot("D09");
        assertThrows(IllegalArgumentException.class, () -> sApp.propose("D09")); //D09 is not available
        
        Map<String, Object> after = sApp.bookingSnapshot();
        Map<String, Object> d09After = sApp.spaceSnapshot("D09");
        assertEquals(before, after);
        assertEquals("A17", after.get("spaceId"));
        assertEquals(0, after.get("version"));
        assertEquals(d09Before, d09After);
        assertEquals(true, d09After.get("occupied"));
        assertEquals(0, sApp.proposalCount());
    }

    @Test
    void eligibleB12(){
        StudentApplication sApp = new StudentApplication();
        ProposalView p = sApp.propose("B12");
        assertEquals("PENDING", p.status());
        assertEquals("B1", p.bookingId());
        assertEquals("B12", p.targetId());
        assertEquals(0, p.bookingVersion());
        assertEquals(1, p.policyVersion());
    }

    @Test 
    void unknownTarget(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();
        assertThrows(IllegalArgumentException.class, () -> sApp.propose("Z99")); //valid ID, but no such space
        
        Map<String, Object> after = sApp.bookingSnapshot();
        assertEquals(before, after);
        assertEquals("A17", after.get("spaceId"));
        assertEquals(0, after.get("version"));
        assertEquals(0, sApp.proposalCount());
    }

    @Test
    void protectedSnapshot(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> snapshot = sApp.bookingSnapshot();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put("spaceId", "D09"));
        assertEquals("A17", snapshot.get("spaceId"));
        assertEquals(0, snapshot.get("version"));
    }

    @Test
    void bookingUnchangedAfterProposingOrRejecting(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();

        sApp.propose("B12");

        assertEquals(before, sApp.bookingSnapshot());
        assertThrows(IllegalArgumentException.class, () -> sApp.propose("D09"));
        assertEquals(before, sApp.bookingSnapshot());
        assertThrows(IllegalArgumentException.class, () -> sApp.propose("A17"));
        assertEquals(before, sApp.bookingSnapshot());
        assertThrows(IllegalArgumentException.class, () -> sApp.propose("Z99"));
        assertEquals(before, sApp.bookingSnapshot());
        assertThrows(IllegalArgumentException.class, () -> sApp.propose("b12"));
        assertEquals(before, sApp.bookingSnapshot());

        assertEquals("A17", sApp.bookingSnapshot().get("spaceId"));
        assertEquals(0, sApp.bookingSnapshot().get("version"));
    }

/*
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                            B2 test cases (A-N)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*/

    @Test //D2-A
    void readListProposeB12(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();
        ReplayProvider provider = Fixture.replay();

        sApp.run(provider, 6);

        assertEquals(1, sApp.proposalCount());
        assertEquals(before, sApp.bookingSnapshot());
    }

    @Test //D2-B
    void executePendingProposal(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();
        Map<String, Object> targetBefore = sApp.spaceSnapshot("B12");
        sApp.propose("B12");
        assertEquals(sApp.execute("P1"), "Proposal P1 unsuccessfully executed: status must be APPROVED");
        assertEquals(before, sApp.bookingSnapshot());
        assertEquals(targetBefore, sApp.spaceSnapshot("B12"));
        assertEquals(0, sApp.notificationCount());
        assertEquals(1, sApp.proposalCount());
    }

    @Test
    void executeInvalidProposal(){
        StudentApplication sApp = new StudentApplication();
        sApp.propose("B12");
        assertEquals(sApp.execute("P999"), "Invalid proposal Id");
    }
 
    @Test //D2-C
    void approveAndExecuteValidProposal(){
        StudentApplication sApp = new StudentApplication();
        sApp.propose("B12");
        assertEquals("APPROVED", sApp.evaluate("P1", true).status());
        assertEquals(sApp.execute("P1"), "Proposal P1 successfully executed");
        assertEquals(sApp.notificationCount(), 1);
        assertEquals("B12", sApp.bookingSnapshot().get("spaceId"));
        assertEquals(1, sApp.bookingSnapshot().get("version"));
    }

    @Test //D2-D
    void executeCommittedProposalAgain(){
        StudentApplication sApp = new StudentApplication();
        sApp.propose("B12");
        sApp.evaluate("P1", true);
        assertEquals(sApp.execute("P1"), "Proposal P1 successfully executed");
        Map<String, Object> afterCommit = sApp.bookingSnapshot();
        assertEquals(sApp.execute("P1"), "Proposal P1 already executed");
        assertEquals(sApp.notificationCount(), 1);
        assertEquals(afterCommit, sApp.bookingSnapshot());
        assertEquals("B12", sApp.bookingSnapshot().get("spaceId"));
        assertEquals(1, sApp.bookingSnapshot().get("version"));
    }
    
    @Test //D2-E
    void rejectAndExecutePendingProposal(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();
        sApp.propose("B12");
        assertEquals("REJECTED", sApp.evaluate("P1", false).status());
        assertEquals(sApp.execute("P1"), "Proposal P1 unsuccessfully executed: status must be APPROVED");
        assertEquals(before, sApp.bookingSnapshot());
        assertEquals(0, sApp.notificationCount());
        assertThrows(IllegalStateException.class, () -> sApp.evaluate("P1", true));
        assertEquals(before, sApp.bookingSnapshot());
    }
    
    @Test //D2-F
    void changeBookingVersionAfterProposal(){
        StudentApplication sApp = new StudentApplication();
        sApp.propose("B12");
        sApp.propose("C03");
        sApp.evaluate("P1", true);
        sApp.evaluate("P2", true);
        assertEquals("Proposal P1 successfully executed", sApp.execute("P1"));

        Map<String, Object> before = sApp.bookingSnapshot();
        assertEquals("Proposal P2 unsuccessfully executed: booking version must be 1", sApp.execute("P2"));
        assertEquals(before, sApp.bookingSnapshot());
        assertEquals(1, sApp.notificationCount());
    }
    
    @Test //D2-G
    void closeTargetAfterProposal(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();
        sApp.propose("B12");
        sApp.closeSpace("B12");
        
        sApp.evaluate("P1", true);
        assertEquals("Proposal P1 unsuccessfully executed: Target space not available", sApp.execute("P1"));
        assertEquals(before, sApp.bookingSnapshot());
        assertEquals(0, sApp.notificationCount());
    }
    
    @Test //D2-H
    void malinformedJSON(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();
        ReplayProvider provider = new ReplayProvider(List.of(
                "{\"tool\":\"READ_BOOKING\",\"bookingId\":\"B1\",\"targetId\":\"\"}",
                "{\"tool\":\"LIST_ALTERNATIVES\",\"bookingId\":\"B1\",\"targetId\":\"\"}",
                "{\"tool\":\"PROPOSE_REASSIGNMENT\",\"bookingId\":\"B1\",\"targetId\":\"B12\",\"approved\":true}"
                ));


        assertEquals("STEP_LIMIT: no proposal (calls = 3, invalid = 1)", sApp.run(provider, 3));
        assertEquals(0, sApp.proposalCount());
        assertEquals(before, sApp.bookingSnapshot());
        assertEquals(0, sApp.notificationCount());
    }
   
    @Test //D2-I
    void unknownTool(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();
        ReplayProvider provider = new ReplayProvider(List.of(
                "{\"tool\":\"READ_BOOKING\",\"bookingId\":\"B1\",\"targetId\":\"\"}",
                "{\"tool\":\"LIST_ALTERNATIVES\",\"bookingId\":\"B1\",\"targetId\":\"\"}",
                "{\"tool\":\"UNKNOWN_TOOL\",\"bookingId\":\"B1\",\"targetId\":\"\"}"
                ));

        assertEquals("STEP_LIMIT: no proposal (calls = 3, invalid = 1)", sApp.run(provider, 3));
        assertEquals(before, sApp.bookingSnapshot());
        assertEquals(0, sApp.proposalCount());
        assertEquals(0, sApp.notificationCount());
    }
   
    @Test //D2-J
    void stepLimit(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();
        ReplayProvider provider = new ReplayProvider(List.of(
                "{\"tool\":\"READ_BOOKING\",\"bookingId\":\"B1\",\"targetId\":\"\"}",
                "{\"tool\":\"READ_BOOKING\",\"bookingId\":\"B1\",\"targetId\":\"\"}",
                "{\"tool\":\"READ_BOOKING\",\"bookingId\":\"B1\",\"targetId\":\"\"}",
                "{\"tool\":\"READ_BOOKING\",\"bookingId\":\"B1\",\"targetId\":\"\"}",
                "{\"tool\":\"READ_BOOKING\",\"bookingId\":\"B1\",\"targetId\":\"\"}",
                "{\"tool\":\"READ_BOOKING\",\"bookingId\":\"B1\",\"targetId\":\"\"}"
                ));

        assertEquals("STEP_LIMIT: no proposal (calls = 6, invalid = 0)", sApp.run(provider, 6));
        assertEquals(before, sApp.bookingSnapshot());
        assertEquals(0, sApp.proposalCount());
        assertEquals(6, provider.calls());
    }
  
    @Test //D2-K
    void providerError(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();
        ReplayProvider provider = new ReplayProvider(List.of());

        assertEquals("PROVIDER_ERROR: Replay exhausted after 0 responses (calls = 1, invalid = 0)", sApp.run(provider, 6));
        assertEquals(before, sApp.bookingSnapshot());
        assertEquals(0, sApp.proposalCount());
    }
    
    @Test //D2-L
    void ticksWithUnchangedVersions(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();

        assertEquals("Successfully scanned and emitted work item B1:v0:p1", sApp.tick(0));
        assertEquals("NOT_SCANNED: Boundary not reached yet", sApp.tick(59));
        assertEquals("SCANNED: work item B1:v0:p1 has already been emitted", sApp.tick(60));
        assertEquals(before, sApp.bookingSnapshot());
        assertEquals(0, sApp.proposalCount());
    }
    
    @Test //D2-M
    void tickBelowPreviousTime(){
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();

        assertEquals("Successfully scanned and emitted work item B1:v0:p1", sApp.tick(60));
        assertEquals("REJECTED: time cannot be less than previous tick", sApp.tick(59));
        assertEquals("NOT_SCANNED: Boundary not reached yet", sApp.tick(119));
        assertEquals("SCANNED: work item B1:v0:p1 has already been emitted", sApp.tick(120));
        assertEquals(before, sApp.bookingSnapshot());
        assertEquals(0, sApp.proposalCount());
    }
     
    @Test //D2-N
    void realModelRun(){
        String host = System.getenv("OLLAMA_CHAT_URL");
        String model = System.getenv("OLLAMA_MODEL");
        StudentApplication sApp = new StudentApplication();
        Map<String, Object> before = sApp.bookingSnapshot();

        if (host == null || host.isBlank() || model == null || model.isBlank()) {
            assertEquals(0, sApp.proposalCount());
            assertEquals(before, sApp.bookingSnapshot());
            return;
        }

        OllamaProvider provider = new OllamaProvider(URI.create(host), model, Duration.ofSeconds(30));
        String result = sApp.run(provider, 6);

        assertTrue(result.startsWith("AWAITING_APPROVAL")
                || result.startsWith("NO_ACTION")
                || result.startsWith("STEP_LIMIT")
                || result.startsWith("PROVIDER_ERROR"), result);

        if (result.startsWith("AWAITING_APPROVAL")) {
            assertEquals(1, sApp.proposalCount());
            assertTrue(result.contains("=B12") || result.contains("=C03"), result);
        } 
        else {
            assertEquals(0, sApp.proposalCount());
        }

        assertEquals(before, sApp.bookingSnapshot());
    }

/*
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                            B3 test cases (A-J)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test //D3-A
    void policy1FixureB1(){
        
    }

    @Test //D3-B
    void activatePolicy2(){
        
    }

    @Test //D3-C
    void version1ApprovedProposalThenPolicy2(){
        
    }
    
    @Test //D3-D
    void version2ProposalToB12(){

    }
    
    @Test //D3-E
    void version2ProposalToC09(){
        
    }
    
    @Test //D3-F
    void repeatD3E(){
        
    }
    
    @Test //D3-G
    void nonAccessibilityRequiringBooking(){
        
    }
    
    @Test //D3-H
    void ignorePolicyProposeB12(){
        
    }
    
    @Test //D3-I
    void periodicTickAfterPolicyVersionChange(){
        
    }
    
    @Test //D3-J
    void contractedAttemptBudgetReachesLimit(){
        
    }
*/
}
