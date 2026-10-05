public final class Booking {
    private final String id; 
    private final String spaceId;
    private final int duration;
    private final int version;
    private final boolean accessiblityRequirement;

    public Booking(String id, String spaceId, int duration, int version, boolean accessiblityRequirement){
        DomainRules.requireDuration(duration);
        
        DomainRules.requireIdentifier(id);
        DomainRules.requireIdentifier(spaceId);
    }
}
