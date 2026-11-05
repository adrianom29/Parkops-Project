import java.util.List;
import java.util.Map;

//The application operations a model conversation may reach: read, list and propose.
//There is deliberately no approve or execute here, so the agent loop cannot commit
//anything; that boundary is enforced by the type, not by the prompt.

interface ReassignmentTools {

    String goalBookingId();

    String currentSpaceId();

    int bookingVersion();

    int policyVersion();

    Map<String, Object> readBooking();

    List<String> listAlternatives();

    // Creates one pending proposal, or throws IllegalArgumentException if the target is ineligible.
    ProposalView propose(String targetId);
}
