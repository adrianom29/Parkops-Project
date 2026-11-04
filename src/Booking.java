public final class Booking {
    private final String id; 
    private String spaceId;
    private final int duration;
    private int version;
    private final boolean accessibilityRequirement;

    public Booking(String id, String spaceId, int duration, int version, boolean accessibilityRequirement){
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
        this.accessibilityRequirement = accessibilityRequirement;
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

    /**
     * Moves this booking to another space. The only way the space changes, and it
     * always bumps the version, so proposals made against the old version go stale.
     */
    public void reassignTo(String newSpaceId){
        DomainRules.requireIdentifier(newSpaceId);
        this.spaceId = newSpaceId;
        this.version++;
    }

    public boolean accessibilityRequirement(){
        return accessibilityRequirement;
    }

}
