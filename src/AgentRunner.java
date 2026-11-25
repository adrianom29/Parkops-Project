import java.util.ArrayList;
import java.util.List;


//Application coordination for one bounded model conversation: calls the provider,
//validates each reply through ToolProtocol, dispatches it to ReassignmentTools and
//appends the observation to the history so the next call is informed by it.
//Stops at a pending proposal (AWAITING_APPROVAL), FINISH, the call limit or a provider error.

final class AgentRunner {

    private final ReassignmentTools tools;

    AgentRunner(ReassignmentTools tools) {
        this.tools = tools;
    }

    String run(ModelProvider provider, int limit) {
        if (provider == null) {
            return "REJECTED: provider must not be null";
        }
        if (limit < 1 || limit > 6) {
            return "REJECTED: Limit must be between 1 and 6";
        }

        Transcript transcript = new Transcript(
                ToolProtocol.openingMessages(tools.goalBookingId(), tools.currentSpaceId()));
        Investigation investigation = new Investigation();

        for (int call = 1; call <= limit; call++) {
            String reply;
            try {
                reply = provider.next(transcript.history());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "PROVIDER_ERROR: interrupted (calls = " + call + ", invalid = " + transcript.invalid() + ")";
            } catch (Exception e) {
                return "PROVIDER_ERROR: " + e.getMessage()
                        + " (calls = " + call + ", invalid = " + transcript.invalid() + ")";
            }

            if (reply == null) {
                transcript.reject("empty reply");
                continue;
            }
            transcript.add(ToolProtocol.assistant(reply));

            ToolRequest request;
            try {
                request = ToolProtocol.parse(reply, tools.goalBookingId());
            } catch (IllegalArgumentException e) {
                transcript.reject(e.getMessage());
                continue;
            }

            switch (request.tool()) {
                case FINISH -> {
                    return "NO_ACTION: calls = " + call + ", invalid = " + transcript.invalid();
                }
                case READ_BOOKING -> {
                    investigation.read(tools.bookingVersion(), tools.policyVersion());
                    transcript.observe(
                            ToolProtocol.bookingObservation(tools.readBooking(), tools.policyVersion()));
                }
                case LIST_ALTERNATIVES -> {
                    if (!investigation.hasRead()) {
                        transcript.reject("READ_BOOKING must be before LIST_ALTERNATIVES");
                    } else if (!restartIfStale(investigation, transcript)) {
                        investigation.listed(tools.listAlternatives());
                        transcript.observe(ToolProtocol.candidatesObservation(investigation.candidates()));
                    }
                }
                case PROPOSE_REASSIGNMENT -> {
                    if (!investigation.hasListed()) {
                        transcript.reject(
                                "READ_BOOKING then LIST_ALTERNATIVES must be before PROPOSE_REASSIGNMENT");
                    } else if (!restartIfStale(investigation, transcript)) {
                        if (!investigation.candidates().contains(request.targetId())) {
                            transcript.reject("targetId was not a listed candidate");
                        } else {
                            try {
                                ProposalView p = tools.propose(request.targetId());
                                return "AWAITING_APPROVAL: " + p + " calls = " + call
                                        + " invalid=" + transcript.invalid();
                            } catch (IllegalArgumentException ex) {
                                transcript.reject(ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return "STEP_LIMIT: no proposal (calls = " + limit + ", invalid = " + transcript.invalid() + ")";
    }

    //If the versions moved since READ_BOOKING, discards the investigation and tells the model to start over.
    //Not counted as invalid: the model did nothing wrong.
    private boolean restartIfStale(Investigation investigation, Transcript transcript) {
        if (!investigation.isStale(tools.bookingVersion(), tools.policyVersion())) {
            return false;
        }
        investigation.reset();
        transcript.observe(ToolProtocol.STALE_OBSERVATION);
        return true;
    }

    //The messages of one conversation and how many replies were rejected.
    //reject() counts and reports a rejection in one step, and is the only thing that changes the count.
    private static final class Transcript {
        private final List<ChatMessage> messages;
        private int invalid;

        Transcript(List<ChatMessage> opening) {
            this.messages = new ArrayList<>(opening);
        }

        void add(ChatMessage message) {
            messages.add(message);
        }

        void observe(String content) {
            add(ToolProtocol.observation(content));
        }

        void reject(String reason) {
            invalid++;
            add(ToolProtocol.rejected(reason));
        }

        //Read-only copy, so a provider cannot alter the conversation.
        List<ChatMessage> history() {
            return List.copyOf(messages);
        }

        int invalid() {
            return invalid;
        }
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
