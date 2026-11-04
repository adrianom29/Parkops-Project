import java.util.ArrayList;
import java.util.List;

//Data storage for simulated notifications: at most one record per committed proposal.
final class NotificationLog {

    record Notification(String proposalId, String bookingId, String targetId) {
    }

    private final List<Notification> records = new ArrayList<>();

    void record(Proposal proposal) {
        for (Notification n : records) {
            if (n.proposalId().equals(proposal.getId())) {
                throw new IllegalStateException("Notification already recorded for " + proposal.getId());
            }
        }
        records.add(new Notification(proposal.getId(), proposal.getBookingId(), proposal.getSpaceId()));
    }

    int count() {
        return records.size();
    }

    List<Notification> records() {
        return List.copyOf(records);
    }
}
