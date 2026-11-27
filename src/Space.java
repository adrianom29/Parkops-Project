public class Space {
    private final String id;
    private boolean open;
    private boolean occupied;
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

    public void open(){
        this.open = true;
    }
    public void close(){
        this.open = false;
    }

    //A committed reassignment occupies the target; only an available space can be taken.
    public void occupy(){
        if (!isAvailable()){
            throw new IllegalStateException("Space (" + id + ") is not available to occupy");
        }
        this.occupied = true;
    }
    //Frees the space a booking has moved out of. Does not reopen a closed space.
    public void release(){
        this.occupied = false;
    }

}
