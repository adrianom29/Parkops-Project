final class AttemptBudget {

    static final int MAX_LIMIT = 6;

    private final int limit;
    private int remaining;

    AttemptBudget(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Limit must be between 1 and " + MAX_LIMIT);
        }
        this.limit = limit;
        this.remaining = limit;
    }
    
    int remaining() {
        return remaining;
    }

    int used() {
        return limit - remaining;
    }

    void consume() {
        if (remaining == 0) {
            throw new IllegalStateException("Attempt budget exhausted");
        }
        remaining--;
    }
}
