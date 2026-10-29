public class Proposal {
    private final String id;
    private final String bookingId;
    private final String spaceId;
    private int bookingVersion;
    private int policyVersion;
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
    
    public void setBookingVersion(int newVersion){
        this.bookingVersion = newVersion;
    }

    public int getPolicyVersion(){
        return policyVersion;
    }

    public ProposalStatus getStatus(){
        return status;
    }

    public void setStatus(ProposalStatus status){
        this.status = status;
    }

    public ProposalView toView(){
        return new ProposalView(id, bookingId, spaceId, bookingVersion, policyVersion, status.name());

    }

}
