import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


// Application coordination facade: the single entry point used by Main, operators and tests.
// It wires the layers together and owns the operator workflow (approve/reject/execute):
//   model protocol  - ToolProtocol, Tool, ToolRequest (used by AgentRunner)
//   coordination    - this class, AgentRunner, PeriodicScanner
//   domain rules    - ReassignmentPolicy, Booking, Space, Proposal
//   data storage    - ParkingInventory, ProposalStore, NotificationLog

public final class StudentApplication {

    private static final int POLICY_VERSION = 1;

    private final ParkingInventory inventory = ParkingInventory.fromFixture();
    private final ReassignmentPolicy policy = new ReassignmentPolicy(POLICY_VERSION);
    private final ProposalStore proposals = new ProposalStore();
    private final NotificationLog notifications = new NotificationLog();
    private final PeriodicScanner scanner = new PeriodicScanner();

    public StudentApplication() {
    }

    //queries

    public Map<String, Object> bookingSnapshot() {
        Booking booking = inventory.booking();
        return Map.of(
                "id", booking.getId(),
                "spaceId", booking.getSpaceId(),
                "durationHours", booking.getDuration(),
                "version", booking.getVersion(),
                "requiresAccessible", booking.accessibilityRequirement());
    }

    public Map<String, Object> spaceSnapshot(String spaceId) {
        Space space = requireSpace(spaceId);
        return Map.of(
                "id", space.getId(),
                "open", space.isOpen(),
                "occupied", space.isOccupied(),
                "accessible", space.isAccessible());
    }

    public int proposalCount() {
        return proposals.count();
    }

    public int notificationCount() {
        return notifications.count();
    }

    //proposal creation

    //Validates everything before creating anything; the booking is never moved here.
    public ProposalView propose(String targetId) {
        DomainRules.requireIdentifier(targetId);
        Booking booking = inventory.booking();

        Optional<String> blocker = policy.proposalBlocker(booking, inventory.space(targetId));
        if (blocker.isPresent()) {
            throw new IllegalArgumentException(blocker.get());
        }
        return proposals.create(booking.getId(), targetId, booking.getVersion(), policy.version()).toView();
    }

    //model loop (D2.1)

    public String run(ModelProvider provider, int limit) {
        return new AgentRunner(new ModelTools()).run(provider, limit);
    }

    //operator actions (D2.2)

    //Operator approves (true) or rejects (false) a pending proposal. Never executes.
    public ProposalView evaluate(String proposalId, boolean approve) {
        Proposal p = proposals.find(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposalId"));
        if (approve) {
            p.approve();
        } else {
            p.reject();
        }
        return p.toView();
    }

    // Separate operator action: commits an approved proposal only if every recheck still holds.
    public String execute(String proposalId) {
        Optional<Proposal> found = proposals.find(proposalId);
        if (found.isEmpty()) {
            return "Invalid proposal Id";
        }
        Proposal p = found.get();

        if (p.getStatus() == ProposalStatus.EXECUTED) {
            return "Proposal " + p.getId() + " already executed";
        }
        if (p.getStatus() != ProposalStatus.APPROVED) {
            return "Proposal " + p.getId() + " unsuccessfully executed: status must be APPROVED";
        }

        Booking booking = inventory.booking();
        Optional<String> blocker = policy.executionBlocker(p, booking, inventory.space(p.getSpaceId()));
        if (blocker.isPresent()) {
            return "Proposal " + p.getId() + " unsuccessfully executed: " + blocker.get();
        }

        booking.reassignTo(p.getSpaceId());
        p.markExecuted();
        notifications.record(p);
        return "Proposal " + p.getId() + " successfully executed";
    }

    public void openSpace(String spaceId) {
        requireSpace(spaceId).open();
    }

    public void closeSpace(String spaceId) {
        requireSpace(spaceId).close();
    }

    //periodic ticks (D2.3)

    public String tick(int time) {
        Booking booking = inventory.booking();
        return scanner.tick(time, booking, inventory.space(booking.getSpaceId()), policy, proposals);
    }

    public String retry(String bookingId) {
        return scanner.retry(bookingId, inventory.booking().getVersion(), policy.version());
    }

    //helpers

    private Space requireSpace(String spaceId) {
        Space space = inventory.space(spaceId);
        if (space == null) {
            throw new IllegalArgumentException("Unknown space: " + spaceId);
        }
        return space;
    }

    // The read/list/propose operations handed to the model loop; no approve or execute.
    private final class ModelTools implements ReassignmentTools {

        public String goalBookingId() {
            return inventory.booking().getId();
        }

        public String currentSpaceId() {
            return inventory.booking().getSpaceId();
        }

        public int bookingVersion() {
            return inventory.booking().getVersion();
        }

        public int policyVersion() {
            return policy.version();
        }

        public Map<String, Object> readBooking() {
            return bookingSnapshot();
        }

        public List<String> listAlternatives() {
            Booking booking = inventory.booking();
            List<String> candidates = new ArrayList<>();
            for (Space space : inventory.spaces()) {
                if (policy.isEligible(booking, space)) {
                    candidates.add(space.getId());
                }
            }
            return List.copyOf(candidates);
        }

        public ProposalView propose(String targetId) {
            return StudentApplication.this.propose(targetId);
        }
    }

    // TODO D3: evolve policy, preserve regressions, refactor and specify contracts.
}
