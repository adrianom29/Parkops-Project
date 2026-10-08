// Add focused @org.junit.jupiter.api.Test methods here.
// Create fresh objects for each test and assert results AND unchanged state on rejection.
// The supplied wrapper is not a substitute for your own test design.

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class StudentTests { 

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

}
