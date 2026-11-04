import java.util.ArrayList;
import java.util.List;


//Application coordination for one bounded model conversation: calls the provider,
//validates each reply through ToolProtocol, dispatches it to ReassignmentTools and
//appends the observation to the history so the next call is informed by it.
//Stops at a pending proposal (AWAITING_APPROVAL), FINISH, the call limit or a provider error.

final class AgentRunner {

    static final int MAX_CALLS = 6;

    private final ReassignmentTools tools;

    AgentRunner(ReassignmentTools tools) {
        this.tools = tools;
    }

    String run(ModelProvider provider, int limit) {
        if (provider == null) {
            return "REJECTED: provider must not be null";
        }
        if (limit < 1 || limit > MAX_CALLS) {
            return "REJECTED: Limit must be between 1 and 6";
        }

        List<ChatMessage> history = new ArrayList<>(
                ToolProtocol.openingMessages(tools.goalBookingId(), tools.currentSpaceId()));
        Investigation investigation = new Investigation();
        int invalid = 0;

        for (int call = 1; call <= limit; call++) {
            String reply;
            try {
                reply = provider.next(List.copyOf(history));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "PROVIDER_ERROR: interrupted (calls = " + call + ", invalid = " + invalid + ")";
            } catch (Exception e) {
                return "PROVIDER_ERROR: " + e.getMessage() + " (calls = " + call + ", invalid = " + invalid + ")";
            }

            if (reply == null) {
                invalid++;
                history.add(ToolProtocol.rejected("empty reply"));
                continue;
            }
            history.add(ToolProtocol.assistant(reply));

            ToolRequest request;
            try {
                request = ToolProtocol.parse(reply, tools.goalBookingId());
            } catch (IllegalArgumentException e) {
                invalid++;
                history.add(ToolProtocol.rejected(e.getMessage()));
                continue;
            }

            switch (request.tool()) {
                case FINISH -> {
                    return "NO_ACTION: calls = " + call + ", invalid = " + invalid;
                }
                case READ_BOOKING -> {
                    investigation.read(tools.bookingVersion(), tools.policyVersion());
                    history.add(ToolProtocol.observation(
                            ToolProtocol.bookingObservation(tools.readBooking(), tools.policyVersion())));
                }
                case LIST_ALTERNATIVES -> {
                    if (!investigation.hasRead()) {
                        invalid++;
                        history.add(ToolProtocol.rejected("READ_BOOKING must be before LIST_ALTERNATIVES"));
                    } else if (investigation.isStale(tools.bookingVersion(), tools.policyVersion())) {
                        investigation.reset();
                        history.add(ToolProtocol.observation(ToolProtocol.STALE_OBSERVATION));
                    } else {
                        investigation.listed(tools.listAlternatives());
                        history.add(ToolProtocol.observation(
                                ToolProtocol.candidatesObservation(investigation.candidates())));
                    }
                }
                case PROPOSE_REASSIGNMENT -> {
                    if (!investigation.hasListed()) {
                        invalid++;
                        history.add(ToolProtocol.rejected(
                                "READ_BOOKING then LIST_ALTERNATIVES must be before PROPOSE_REASSIGNMENT"));
                    } else if (investigation.isStale(tools.bookingVersion(), tools.policyVersion())) {
                        investigation.reset();
                        history.add(ToolProtocol.observation(ToolProtocol.STALE_OBSERVATION));
                    } else if (!investigation.candidates().contains(request.targetId())) {
                        invalid++;
                        history.add(ToolProtocol.rejected("targetId was not a listed candidate"));
                    } else {
                        try {
                            ProposalView p = tools.propose(request.targetId());
                            return "AWAITING_APPROVAL: " + p + " calls = " + call + " invalid=" + invalid;
                        } catch (IllegalArgumentException ex) {
                            invalid++;
                            history.add(ToolProtocol.rejected(ex.getMessage()));
                        }
                    }
                }
            }
        }
        return "STEP_LIMIT: no proposal (calls = " + limit + ", invalid = " + invalid + ")";
    }

    //What the model has established so far in this conversation, and against which versions. 
    private static final class Investigation {
        private int bookingVersion = -1;
        private int policyVersion = -1;
        private List<String> candidates;

        void read(int bookingVersion, int policyVersion) {
            this.bookingVersion = bookingVersion;
            this.policyVersion = policyVersion;
            this.candidates = null;
        }

        void listed(List<String> candidates) {
            this.candidates = List.copyOf(candidates);
        }

        boolean hasRead() {
            return bookingVersion != -1;
        }

        boolean hasListed() {
            return candidates != null;
        }

        boolean isStale(int currentBookingVersion, int currentPolicyVersion) {
            return currentBookingVersion != bookingVersion || currentPolicyVersion != policyVersion;
        }

        List<String> candidates() {
            return candidates;
        }

        void reset() {
            bookingVersion = -1;
            policyVersion = -1;
            candidates = null;
        }
    }
}
