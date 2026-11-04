import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

//Data storage for proposals. Issues identities P1, P2, ... and never reuses one.
final class ProposalStore {

    private final Map<String, Proposal> proposals = new LinkedHashMap<>();
    private int nextNumber = 1;

    Proposal create(String bookingId, String targetId, int bookingVersion, int policyVersion) {
        Proposal proposal = new Proposal("P" + nextNumber, bookingId, targetId, bookingVersion, policyVersion);
        nextNumber++;
        proposals.put(proposal.getId(), proposal);
        return proposal;
    }

    Optional<Proposal> find(String proposalId) {
        return Optional.ofNullable(proposals.get(proposalId));
    }

    int count() {
        return proposals.size();
    }

    //True if a pending or approved proposal already exists for exactly this booking/policy state.
    boolean hasActive(String bookingId, int bookingVersion, int policyVersion) {
        for (Proposal p : proposals.values()) {
            if (p.isActive()
                    && p.getBookingId().equals(bookingId)
                    && p.getBookingVersion() == bookingVersion
                    && p.getPolicyVersion() == policyVersion) {
                return true;
            }
        }
        return false;
    }
}
