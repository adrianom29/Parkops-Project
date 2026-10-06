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
            Booking b1 = new Booking("TEST", "TEST", -1, 0, false);
            Booking b2 = new Booking("TEST", "TEST", 24, 0, false);
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

    

}
