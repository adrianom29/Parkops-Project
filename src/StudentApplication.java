
import java.util.*;

public final class StudentApplication {

    private static final int POLICY_VERSION = 1;
    private final Booking booking;
    private final Map<String, Space> spaces = new LinkedHashMap<>();
    private final HashMap<String, Proposal> proposals = new HashMap<>();
    private int nextProposalNumber = 1;
    private int notifications = 0;
    private int prevTick = -1;
    private int nextScanTime = 0;
    private HashSet<String> workItems = new HashSet<>();
    private static final int TICK_PERIOD = 60;

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
                "requiresAccessible", booking.accessibilityRequirement());
    }

    public Map<String, Object> spaceSnapshot(String spaceId) {
        Space space = spaces.get(spaceId);
        if (space == null) {
            throw new IllegalArgumentException("Unknown space: " + spaceId);
        }
        return Map.of(
                "id", space.getId(),
                "open", space.isOpen(),
                "occupied", space.isOccupied(),
                "accessible", space.isAccessible());
    }

    public int proposalCount() {
        return proposals.size();
    }

    public int notificationCount(){
        return notifications;
    }

    public ProposalView propose(String targetId) {
        // Validate everything before creating anything; the booking is never moved here.
        DomainRules.requireIdentifier(targetId);

        Space target = spaces.get(targetId);
        if (target == null) {
            throw new IllegalArgumentException("Target space must not be null or unknown");
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
        proposals.put(proposalId, proposal);

        return proposal.toView();
    }

    public void openSpace(String spaceId){
        Space s = spaces.get(spaceId);
        s.open();
    }

    public void closeSpace(String spaceId){
        Space s = spaces.get(spaceId);
        s.close();
    }

    public String run(ModelProvider provider, int limit) {
        // TODO D2: validate protocol, dispatch tools, retain observation history,
        // bound invocations and stop at pending approval. No automatic commit.
        // Add separate operator approve/reject/execute operations and tick(time).

        if (provider == null) {
            return "REJECTED: provider must not be null";
        }

        if (limit < 1 || limit > 6) {
            return "REJECTED: Limit must be between 1 and 6";
        }

        List<ChatMessage> history = new ArrayList<>();

        //system message
        history.add(new ChatMessage("system",
                "You are a parking reassignment assistant for ParkOps.\r\n"
                + "Reply with exactly ONE JSON object and nothing else: no prose, no markdown.\r\n"
                + "The object must have exactly these three string fields:\r\n"
                + "  {\"tool\": \"...\", \"bookingId\": \"...\", \"targetId\": \"...\"}\r\n"
                + "\r\n"
                + "Allowed tools:\r\n"
                + "- READ_BOOKING: read the booking. targetId must be \"\".\r\n"
                + "- LIST_ALTERNATIVES: list candidate spaces. Only after READ_BOOKING. targetId must be \"\".\r\n"
                + "- PROPOSE_REASSIGNMENT: propose moving the booking to targetId (a space id like \"B12\").\r\n"
                + "- FINISH: stop without proposing. targetId must be \"\".\r\n"
                + "\r\n"
                + "Rules:\r\n"
                + "- bookingId must always be the booking you were given.\r\n"
                + "- Call READ_BOOKING before LIST_ALTERNATIVES.\r\n"
                + "- You may only PROPOSE. You cannot approve or execute; a human operator does that.\r\n"
                + "- After each tool call you will receive an OBSERVATION with the result. Use it to choose the next step.\r\n"
        ));
        //user message
        history.add(new ChatMessage("user",
                "Booking B1 is in space A17, which is closed. Find a suitable replacement space and propose a reassignment.\r\n"));

        int bookingVersion = -1;
        int policyVersion = -1;
        List<String> candidates = null;
        int invalid = 0;

        for (int i = 0; i < limit; i++) {
            String reply;
            try {
                reply = provider.next(history);
            } catch (InterruptedException e) {
                return "PROVIDER_ERROR: interrupted (calls = " + (i + 1) + ", invalid = " + invalid + ")";
            } catch (Exception e) {
                return "PROVIDER_ERROR: " + e.getMessage() + " (calls = " + (i + 1) + ", invalid = " + invalid + ")";
            }

            if (reply == null) {
                invalid++;
                history.add(new ChatMessage("user", "REJECTED: empty reply"));
                continue;
            }
            history.add(new ChatMessage("assistant", reply));

            Map<String, Object> request;
            try {
                request = parseRequest(reply);
            } catch (IllegalArgumentException e) {
                invalid++;
                history.add(new ChatMessage("user", "REJECTED: " + e.getMessage()));
                continue;
            }

            String tool = (String) request.get("tool");
            String targetId = (String) request.get("targetId");

            if (tool.equals("FINISH")) {
                return "NO_ACTION: calls = " + (i + 1) + ", invalid = " + invalid;
            } else if (tool.equals("READ_BOOKING")) {
                bookingVersion = booking.getVersion();
                policyVersion = POLICY_VERSION;
                candidates = null;
                history.add(new ChatMessage("user",
                        MiniJson.stringify(Map.of(
                                "booking", bookingSnapshot(),
                                "policyVersion", POLICY_VERSION))
                ));
            } else if (tool.equals("LIST_ALTERNATIVES")) {
                if (bookingVersion == -1) {
                    history.add(new ChatMessage("user", "REJECTED: READ_BOOKING must be before LIST_ALTERNATIVES"));
                    invalid++;
                } else if (bookingVersion != booking.getVersion() || policyVersion != POLICY_VERSION) {
                    history.add(new ChatMessage("user", "Stale version, investigation restarting, READ_BOOKING must be called again"));
                    bookingVersion = -1;
                    policyVersion = -1;
                    candidates = null;
                } else {
                    candidates = new ArrayList<>();
                    for (Space space : spaces.values()) {
                        if (space.isAvailable() && !space.getId().equals(booking.getSpaceId())) {
                            candidates.add(space.getId());
                        }
                    }
                    history.add(new ChatMessage("user", MiniJson.stringify(Map.of("candidates", candidates))));
                }
            } else if (tool.equals("PROPOSE_REASSIGNMENT")) {
                if (bookingVersion == -1 || candidates == null) {
                    invalid++;
                    history.add(new ChatMessage("user", "REJECTED: READ_BOOKING then LIST_ALTERNATIVES must be before PROPOSE_REASSIGNMENT"));
                } else if (bookingVersion != booking.getVersion() || policyVersion != POLICY_VERSION) {
                    history.add(new ChatMessage("user", "Stale version, investigation restarting, READ_BOOKING must be called again"));
                    bookingVersion = -1;
                    policyVersion = -1;
                    candidates = null;
                } else if (!candidates.contains(targetId)) {
                    invalid++;
                    history.add(new ChatMessage("user", "REJECTED: targetId was not a listed candidate"));
                } else {
                    try {
                        ProposalView p = propose(targetId);
                        return "AWAITING_APPROVAL: " + p + " calls = " + (i + 1) + " invalid=" + invalid;
                    } catch (IllegalArgumentException ex) {
                        invalid++;
                        history.add(new ChatMessage("user", "REJECTED: " + ex.getMessage()));
                    }
                }
            } //default (if unknown tool)
            else {
                invalid++;
                history.add(new ChatMessage("user", "REJECTED: unknown tool " + tool));
            }
        }
        return "STEP_LIMIT: no proposal (calls = " + limit + ", invalid = " + invalid + ")";
        //throw new UnsupportedOperationException("D2 model/tool coordination");
    }

    private Map<String, Object> parseRequest(String reply) {     //helper method used in run()
        Map<String, Object> request = MiniJson.object(MiniJson.parse(reply));
        if (!request.keySet().equals(Set.of("tool", "bookingId", "targetId"))
                || !request.values().stream().allMatch(v -> v instanceof String)
                || !booking.getId().equals(request.get("bookingId"))) {

            throw new IllegalArgumentException("Malformed request");
        }
        if (!request.get("tool").equals("PROPOSE_REASSIGNMENT")
                && !request.get("targetId").equals("")) {

            throw new IllegalArgumentException("targetId must be empty for " + request.get("tool"));
        }

        return request;
    }

    public ProposalView evaluate(String proposalId, boolean approve){       //either approves or rejects proposal (called by client)
        Proposal p = proposals.get(proposalId);
        if (p == null){
            throw new IllegalArgumentException("Invalid proposalId");
        }
        if (p.getStatus() == ProposalStatus.APPROVED || 
            p.getStatus() == ProposalStatus.REJECTED) {
            throw new IllegalStateException("Proposal has already been evaluated, cannot be evaluated twice");
        }
        if (p.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal status must be PENDING in order to be evaluated");
        }

        if (approve){
            p.setStatus(ProposalStatus.APPROVED);
            return p.toView();
        }
        p.setStatus(ProposalStatus.REJECTED);
        return p.toView();
    }

    public String execute(String proposalId) {
        Proposal p = proposals.get(proposalId);

        if (p == null) {
            return "Invalid proposal Id";
        }
        if (p.getStatus() == ProposalStatus.EXECUTED) {
            return "Proposal " + p.getId() + " already executed";
        }
        if (p.getStatus() != ProposalStatus.APPROVED) {
            return "Proposal " + p.getId() + " unsuccessfully executed: status must be APPROVED";
        }
        if (p.getPolicyVersion() != POLICY_VERSION){
            return String.format("Proposal %s unsuccessfully executed: policy version must be %d", p.getId(), POLICY_VERSION);
        }
        if (p.getBookingVersion() != booking.getVersion()){
            return String.format("Proposal %s unsuccessfully executed: booking version must be %d", p.getId(), booking.getVersion());
        }
        if (!p.getBookingId().equals(booking.getId())){
            return String.format("Proposal %s unsuccessfully executed: proposal bookingId must be %s", p.getId(), booking.getId());
        } 
        
        Space targetSpace = spaces.get(p.getSpaceId());
        if (targetSpace == null || !targetSpace.isAvailable()){
            return "Proposal " + p.getId() + " unsuccessfully executed: Target space not available";
        }

        booking.setSpaceId(targetSpace.getId());
        booking.setVersion(booking.getVersion() + 1);
        p.setStatus(ProposalStatus.EXECUTED);

        notifications++;
        return "Proposal " + p.getId() + " successfully executed";
    }

    public String tick(int time){
        if (time < 0){
            return "REJECTED: time must be positive";
        }
        if (time < prevTick){
            return "REJECED: time cannot be less than previous tick";
        }
        if (prevTick != -1 && time < nextScanTime){
            prevTick = time;
            return "Boundary not reached yet";
        }
        prevTick = time;

        Space s = spaces.get(booking.getSpaceId());
        if (s == null || s.isOpen()){
            return "SCANNED: no bookings affected";
        }

        String key = String.format("%s:v%d:p%d", booking.getId(), booking.getVersion(), POLICY_VERSION);
        if (workItems.contains(key)){
            return "work item " + key + " has already been emitted";
        }

        for (Proposal p: proposals.values()){
            if (p.getBookingId().equals(booking.getId()) &&
                p.getPolicyVersion() == POLICY_VERSION &&
                p.getBookingVersion() == booking.getVersion() &&
                (p.getStatus() == ProposalStatus.APPROVED || p.getStatus() == ProposalStatus.PENDING)){

                return "SCANNED: " + key + " not emitted";
            }
        }
        
        workItems.add(key);
        return "Successfully scanned and emitted work item " + key;
    }


    // TODO D3: evolve policy, preserve regressions, refactor and specify contracts.
}
