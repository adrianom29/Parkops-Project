import java.util.HashSet;
import java.util.Set;


//Application coordination for periodic task detection. Time is supplied by the caller
//through explicit ticks (no sleeping, no wall clock). A scan runs at most once per
//60-second boundary; a work item is keyed by booking id, booking version and policy
//version, so the same state is never emitted twice unless an operator retries it.

final class PeriodicScanner {

    static final int PERIOD_SECONDS = 60;

    private int lastTick = -1;
    private int nextScanTime = 0;
    private final Set<String> emitted = new HashSet<>();

    String tick(int time, Booking booking, Space current, ReassignmentPolicy policy, ProposalStore proposals) {
        if (time < 0) {
            return "REJECTED: time cannot be a negative value";
        }
        if (time < lastTick) {
            return "REJECTED: time cannot be less than previous tick";
        }
        if (lastTick != -1 && time < nextScanTime) {
            lastTick = time;
            return "NOT_SCANNED: Boundary not reached yet";
        }
        lastTick = time;
        nextScanTime = time + PERIOD_SECONDS;

        if (!policy.needsReassignment(booking, current)) {
            return "SCANNED: no bookings affected";
        }

        String key = key(booking.getId(), booking.getVersion(), policy.version());
        if (emitted.contains(key)) {
            return "SCANNED: work item " + key + " has already been emitted";
        }
        if (proposals.hasActive(booking.getId(), booking.getVersion(), policy.version())) {
            return "SCANNED: " + key + " not emitted";
        }

        emitted.add(key);
        return "Successfully scanned and emitted work item " + key;
    }

    //Operator retry: allows the next scan to emit the same state again.
    String retry(String bookingId, int bookingVersion, int policyVersion) {
        String key = key(bookingId, bookingVersion, policyVersion);
        if (!emitted.remove(key)) {
            return "REJECTED: " + key + " has never been emitted, cannot retry";
        }
        return "Next scan will emit " + key;
    }

    private static String key(String bookingId, int bookingVersion, int policyVersion) {
        return String.format("%s:v%d:p%d", bookingId, bookingVersion, policyVersion);
    }
}
