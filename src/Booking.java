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

        if (version < 0){
            throw new IllegalArgumentException("Illegal version");
        }

        this.id = id; 
        this.spaceId = spaceId;
        this.duration = duration;
        this.version = version;
        this.accessiblityRequirement = accessiblityRequirement;
    }

    public String getId(){
        return id;
    }

    public String getSpaceId(){
        return spaceId;
    }

    public int getDuration(){
        return duration;
    }

    public int getVersion(){
        return version;
    }

    public boolean accessiblityRequirement(){
        return accessiblityRequirement;
    }

}
