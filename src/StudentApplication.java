
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        proposals.add(proposal);

        return new ProposalView(proposal.getId(), proposal.getBookingId(), proposal.getSpaceId(),
                proposal.getBookingVersion(), proposal.getPolicyVersion(), proposal.getStatus().name());
    }

    public String run(ModelProvider provider, int limit) {
        // TODO D2: validate protocol, dispatch tools, retain observation history,
        // bound invocations and stop at pending approval. No automatic commit.
        // Add separate operator approve/reject/execute operations and tick(time).
        
        if (provider == null) {
            return "REJECTED: provider must not be null";
        }

        if (limit < 1 || limit > 6){
            return "REJEDCTED: Limit must be between 1 and 6";
        }

        List<ChatMessage> history = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage("system",
            "You are a parking reassignment assistant for ParkOps.\r\n" + 
            "Reply with exactly ONE JSON object and nothing else: no prose, no markdown.\r\n" + 
            "The object must have exactly these three string fields:\r\n" + 
            "  {\"tool\": \"...\", \"bookingId\": \"...\", \"targetId\": \"...\"}\r\n" + 
            "\r\n" + 
            "Allowed tools:\r\n" + 
            "- READ_BOOKING: read the booking. targetId must be \"\".\r\n" + 
            "- LIST_ALTERNATIVES: list candidate spaces. Only after READ_BOOKING. targetId must be \"\".\r\n" + 
            "- PROPOSE_REASSIGNMENT: propose moving the booking to targetId (a space id like \"B12\").\r\n" + 
            "- FINISH: stop without proposing. targetId must be \"\".\r\n" + 
            "\r\n" + 
            "Rules:\r\n" + 
            "- bookingId must always be the booking you were given.\r\n" + 
            "- Call READ_BOOKING before LIST_ALTERNATIVES.\r\n" + 
            "- You may only PROPOSE. You cannot approve or execute; a human operator does that.\r\n" + 
            "- After each tool call you will receive an OBSERVATION with the result. Use it to choose the next step.\r\n"
        );
        ChatMessage userMessage = new ChatMessage("user", 
        "Booking B1 is in space A17, which is closed. Find a suitable replacement space and propose a reassignment.\r\n" );

        history.add(systemMessage);
        history.add(userMessage);

        for (int i = 0; i < limit; i++){
            try {
                String reply = provider.next(history);
                history.add(new ChatMessage("assistant", reply));
                Map<String, Object> request = parseRequest(reply);
                String tool = (String)request.get("tool");

                switch (tool){
                    case "READ_BOOKING":
                        history.add(new ChatMessage("user", 
                            MiniJson.stringify(Map.of("booking", bookingSnapshot(), "policyVersion", POLICY_VERSION))
                        ));
                        break;
                    case "LIST_ALTERNATIVES":
                        
                        break;
                    case "PROPOSE_REASSIGNMENT":
                        break;
                    case "FINISH":
                        break;
                    default:
                        break;
                }
                

            } 
            catch (Exception e) {
                history.add(new ChatMessage("user", "REJECTED: " + e.getMessage()));
            }
        }
        

        throw new UnsupportedOperationException("D2 model/tool coordination");
    }

    private Map<String, Object> parseRequest(String reply){     //helper method used in run()
        Map<String, Object> request = MiniJson.object(MiniJson.parse(reply));
        if (!request.keySet().equals(Set.of("tool", "bookingId", "targetId")) ||
            !request.values().stream().allMatch(v -> v instanceof String) ||
            !booking.getId().equals(request.get("bookingId"))){
                throw new IllegalArgumentException("Malformed request");
            }

        return request;
    }

    // TODO D3: evolve policy, preserve regressions, refactor and specify contracts.
}
