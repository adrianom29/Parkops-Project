public class Space {
    private final String id;
    private final boolean open;
    private final boolean occupied; 
    private final boolean accessible;

    public Space(String id, boolean open, boolean occupied, boolean accessible){
        DomainRules.requireIdentifier(id);

        this.id = id;
        this.open = open;
        this.occupied = occupied;
        this.accessible = accessible;
    }

    public String getId(){
        return id;
    }
    public boolean isOpen(){
        return open;
    }
    public boolean isOccupied(){
        return occupied;
    }
    public boolean isAccessible(){
        return accessible;
    }
    public boolean isAvailable(){
        return open && !occupied;
    }
}
