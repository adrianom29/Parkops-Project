//The bounded number of model calls one conversation may make (D3.4 contracted operation).
//Holds only two ints, so its contracts need no assumptions about other objects.
//Not thread safe: each AgentRunner.run creates its own and does not share it.

final class AttemptBudget {

    static final /*@ spec_public @*/ int MAX_LIMIT = 6;

    private final /*@ spec_public @*/ int limit;
    private /*@ spec_public @*/ int remaining;

    //@ public invariant 1 <= limit && limit <= MAX_LIMIT;
    //@ public invariant 0 <= remaining && remaining <= limit;

    /*@ public normal_behavior
         requires 1 <= limit && limit <= MAX_LIMIT;
         assignable this.limit, this.remaining;
         ensures this.limit == limit && this.remaining == limit;
       also
       public exceptional_behavior
         requires limit < 1 || limit > MAX_LIMIT;
         assignable \nothing;
         signals_only IllegalArgumentException;
      @*/
    AttemptBudget(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Limit must be between 1 and " + MAX_LIMIT);
        }
        this.limit = limit;
        this.remaining = limit;
    }
    
    /*@ public normal_behavior
         assignable \nothing;
         ensures \result == remaining;
    @*/

    /*@ pure @*/ int remaining() {
        return remaining;
    }

    /*@public normal_behavior
        assignable \nothing;
        ensures \result == limit - remaining;
    @*/
   
    /*@ pure @*/ int used() {
        return limit - remaining;
    }

    //Uses up one attempt. With none left it fails and changes nothing.
    /*@ public normal_behavior
         requires remaining > 0;
         assignable remaining;
         ensures remaining == \old(remaining) - 1;
       also
       public exceptional_behavior
         requires remaining == 0;
         assignable \nothing;
         signals_only IllegalStateException;     
    @*/

    void consume() {
        if (remaining == 0) {
            throw new IllegalStateException("Attempt budget exhausted");
        }
        remaining--;
    }
}
