/**
 * Domain object: a reassignment proposal and its lifecycle.
 * PENDING -> APPROVED | REJECTED, APPROVED -> EXECUTED. No other transition exists,
 * and identity and captured versions never change after creation.
 */
public class Proposal {
    private final String id;
    private final String bookingId;
    private final String spaceId;
    private final int bookingVersion;
    private final int policyVersion;
    private ProposalStatus status;

    public Proposal(String id, String bookingId, String spaceId, int bookingVersion, int policyVersion){
        DomainRules.requireIdentifier(id);
        DomainRules.requireIdentifier(bookingId);
        DomainRules.requireIdentifier(spaceId);

        if (bookingVersion < 0){
            throw new IllegalArgumentException("Illegal booking version");
        }

        if (policyVersion < 1){
            throw new IllegalArgumentException("Illegal policy version");
        }

        this.id = id;
        this.bookingId = bookingId;
        this.spaceId = spaceId;
        this.bookingVersion = bookingVersion;
        this.policyVersion = policyVersion;
        this.status = ProposalStatus.PENDING;
    }

    public String getId(){
        return id;
    }

    public String getBookingId(){
        return bookingId;
    }

    public String getSpaceId(){
        return spaceId;
    }

    public int getBookingVersion(){
        return bookingVersion;
    }

    public int getPolicyVersion(){
        return policyVersion;
    }

    public ProposalStatus getStatus(){
        return status;
    }

    /** Still able to lead to a commit (pending or approved but not yet executed). */
    public boolean isActive(){
        return status == ProposalStatus.PENDING || status == ProposalStatus.APPROVED;
    }

    public void approve(){
        requirePending();
        status = ProposalStatus.APPROVED;
    }

    public void reject(){
        requirePending();
        status = ProposalStatus.REJECTED;
    }

    public void markExecuted(){
        if (status != ProposalStatus.APPROVED){
            throw new IllegalStateException("Proposal must be APPROVED to be executed");
        }
        status = ProposalStatus.EXECUTED;
    }

    private void requirePending(){
        if (status == ProposalStatus.APPROVED || status == ProposalStatus.REJECTED) {
            throw new IllegalStateException("Proposal has already been evaluated, cannot be evaluated twice");
        }
        if (status != ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal status must be PENDING in order to be evaluated");
        }
    }

    public ProposalView toView(){
        return new ProposalView(id, bookingId, spaceId, bookingVersion, policyVersion, status.name());
    }

}
