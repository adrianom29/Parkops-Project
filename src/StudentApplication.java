
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StudentApplication {

    private static final int POLICY_VERSION = 1;

    private final Booking booking;
    private final Map<String, Space> spaces = new LinkedHashMap<>();
    private final List<Proposal> proposals = new ArrayList<>();
    private int nextProposalNumber = 1;

    public StudentApplication() {
        Map<String, Object> raw = Fixture.booking();
        this.booking = new Booking(
                (String) raw.get("id"),
                (String) raw.get("spaceId"),
                (Integer) raw.get("durationHours"),
                (Integer) raw.get("version"),
                (Boolean) raw.get("requiresAccessible"));

        for (Map<String, Object> s : Fixture.spaces()) {
            Space space = new Space(
                    (String) s.get("id"),
                    (Boolean) s.get("open"),
                    (Boolean) s.get("occupied"),
                    (Boolean) s.get("accessible"));
            spaces.put(space.getId(), space);
        }
    }

    public Map<String, Object> bookingSnapshot() {
        return Map.of(
                "id", booking.getId(),
                "spaceId", booking.getSpaceId(),
                "durationHours", booking.getDuration(),
                "version", booking.getVersion(),
                "requiresAccessible", booking.accessiblityRequirement());
    }

    public ProposalView propose(String targetId) {
        // Validate everything before creating anything; the booking is never moved here.
        DomainRules.requireIdentifier(targetId);

        Space target = spaces.get(targetId);
        if (target == null) {
            throw new IllegalArgumentException("Target space must not be null");
        }
        if (target.getId().equals(booking.getSpaceId())) {
            throw new IllegalArgumentException("Target cannot be booking's current space");
        }
        if (!target.isAvailable()) {
            throw new IllegalArgumentException("Target space (" + targetId + ") is not available");
        }

        String proposalId = "P" + nextProposalNumber;
        Proposal proposal = new Proposal(proposalId, booking.getId(), target.getId(),
                booking.getVersion(), POLICY_VERSION);
        nextProposalNumber++;
        proposals.add(proposal);

        return new ProposalView(proposal.getId(), proposal.getBookingId(), proposal.getSpaceId(),
                proposal.getBookingVersion(), proposal.getPolicyVersion(), proposal.getStatus().name());
    }

    public String run(ModelProvider provider, int limit) {
        // TODO D2: validate protocol, dispatch tools, retain observation history,
        // bound invocations and stop at pending approval. No automatic commit.
        // Add separate operator approve/reject/execute operations and tick(time).
        throw new UnsupportedOperationException("D2 model/tool coordination");
    }
    // TODO D3: evolve policy, preserve regressions, refactor and specify contracts.
}
