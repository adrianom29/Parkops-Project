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

    public void setSpaceId(String value){
        this.spaceId = value;
    }

    public int getDuration(){
        return duration;
    }

    public int getVersion(){
        return version;
    }

    public void setVersion(int value){
        this.version = value;
    }

    public boolean accessibilityRequirement(){
        return accessibilityRequirement;
    }

}
