public final class DomainRules {
    private DomainRules() {

    }

    public static void requireDuration(int hours) {
        // TODO D1: reject outside 1..24 with IllegalArgumentException.
        if (hours < 1 || hours > 24) {
            throw new IllegalArgumentException("Inputted hours is out of bounds");
        }
        // throw new UnsupportedOperationException("D1 duration validation");
    }

    public static void requireIdentifier(String id) {
        // TODO D1: non-null [A-Z][A-Z0-9_-]{0,15}.
        if (id == null || 
            id.isBlank() ||
            id.length() == 0 || 
            id.length() > 16 || 
            id.charAt(0) < 'A' || 
            id.charAt(0) > 'Z') {

            throw new IllegalArgumentException("Invalid id input");
        }

        for (int i = 1; i < id.length(); i++) {
            char c = id.charAt(i);
            //all characters must be either a capital letter, digit, - or _
            if (!(c >= 'A' && c <= 'Z') &&          
                    !(c >= '0' && c <= '9') &&
                    c != '-' &&
                    c != '_') {
                throw new IllegalArgumentException("Invalid id input");
            }
        }

        // throw new UnsupportedOperationException("D1 identifier validation");
    }
}