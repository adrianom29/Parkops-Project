import java.util.List;
import java.util.Map;
import java.util.Set;


//Model protocol details: prompt wording, reply validation and observation formatting.
//Knows nothing about storage, approval or execution. The prompt is guidance for the
//model only; every rule it states is also enforced in Java.

final class ToolProtocol {

    private static final Set<String> FIELDS = Set.of("tool", "bookingId", "targetId");

    private static final String SYSTEM_PROMPT =
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
            + "- After each tool call you will receive an OBSERVATION with the result. Use it to choose the next step.\r\n";

    static final String STALE_OBSERVATION =
            "Stale version, investigation restarting, READ_BOOKING must be called again";

    private ToolProtocol() {
    }

    static List<ChatMessage> openingMessages(String bookingId, String spaceId) {
        return List.of(
                new ChatMessage("system", SYSTEM_PROMPT),
                new ChatMessage("user", "Booking " + bookingId + " is in space " + spaceId
                        + ", which is closed. Find a suitable replacement space and propose a reassignment.\r\n"));
    }

    
    //Validates untrusted reply text: one JSON object, exactly the three string fields,
    //a known tool, the goal booking, and an empty targetId for every tool except PROPOSE.
    //throws IllegalArgumentException with the reason, if any check fails

    static ToolRequest parse(String reply, String goalBookingId) {
        Map<String, Object> request = MiniJson.object(MiniJson.parse(reply));
        if (!request.keySet().equals(FIELDS)
                || !request.values().stream().allMatch(v -> v instanceof String)
                || !goalBookingId.equals(request.get("bookingId"))) {
            throw new IllegalArgumentException("Malformed request");
        }

        Tool tool = Tool.fromName((String) request.get("tool"));
        String targetId = (String) request.get("targetId");
        if (tool != Tool.PROPOSE_REASSIGNMENT && !targetId.isEmpty()) {
            throw new IllegalArgumentException("targetId must be empty for " + tool);
        }
        return new ToolRequest(tool, goalBookingId, targetId);
    }

    static ChatMessage assistant(String reply) {
        return new ChatMessage("assistant", reply);
    }

    static ChatMessage observation(String content) {
        return new ChatMessage("user", content);
    }

    static ChatMessage rejected(String reason) {
        return new ChatMessage("user", "REJECTED: " + reason);
    }

    static String bookingObservation(Map<String, Object> booking, int policyVersion) {
        return MiniJson.stringify(Map.of("booking", booking, "policyVersion", policyVersion));
    }

    static String candidatesObservation(List<String> candidates) {
        return MiniJson.stringify(Map.of("candidates", candidates));
    }
}
