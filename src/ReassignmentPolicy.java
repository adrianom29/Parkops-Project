import java.util.Optional;


//Domain rules for reassigning a booking under one versioned policy.
//Pure decisions: reads domain objects, never changes them or any storage.

final class ReassignmentPolicy {

    private final int version;

    ReassignmentPolicy(int version) {
        if (version < 1) {
            throw new IllegalArgumentException("Illegal policy version");
        }
        this.version = version;
    }

    int version() {
        return version;
    }

    // Reason the target cannot be proposed for the booking, or empty if it is eligible. target may be null (unknown). 
    Optional<String> proposalBlocker(Booking booking, Space target) {
        if (target == null) {
            return Optional.of("Target space must not be null or unknown");
        }
        if (target.getId().equals(booking.getSpaceId())) {
            return Optional.of("Target cannot be booking's current space");
        }
        if (!target.isAvailable()) {
            return Optional.of("Target space (" + target.getId() + ") is not available");
        }
        return Optional.empty();
    }

    boolean isEligible(Booking booking, Space target) {
        return proposalBlocker(booking, target).isEmpty();
    }

    
    //Recheck before commit: proposal still matches this policy version, the booking's
    //current version and identity, and the target is still eligible right now.
    
    Optional<String> executionBlocker(Proposal proposal, Booking booking, Space target) {
        if (proposal.getPolicyVersion() != version) {
            return Optional.of(String.format("policy version must be %d", version));
        }
        if (proposal.getBookingVersion() != booking.getVersion()) {
            return Optional.of(String.format("booking version must be %d", booking.getVersion()));
        }
        if (!proposal.getBookingId().equals(booking.getId())) {
            return Optional.of(String.format("proposal bookingId must be %s", booking.getId()));
        }
        if (!isEligible(booking, target)) {
            return Optional.of("Target space not available");
        }
        return Optional.empty();
    }

    //A booking needs reassignment work when its current space is closed. 
    boolean needsReassignment(Booking booking, Space current) {
        return current != null && !current.isOpen();
    }
}
