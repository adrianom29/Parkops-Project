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

    // D1: non-null [A-Z][A-Z0-9_-]{0,15}.
    // D3.4 contract. Assumes id, if not null, is an ordinary (immutable) java.lang.String.
    // Returns normally exactly when id matches; changes no existing state either way.
    /* public normal_behavior
         requires id != null && 1 <= id.length() && id.length() <= 16;
         requires 'A' <= id.charAt(0) && id.charAt(0) <= 'Z';
         requires (\forall int k; 1 <= k && k < id.length();
                     ('A' <= id.charAt(k) && id.charAt(k) <= 'Z')
                  || ('0' <= id.charAt(k) && id.charAt(k) <= '9')
                  || id.charAt(k) == '-' || id.charAt(k) == '_');
         assignable \nothing;
       also
       public exceptional_behavior
         requires id == null || id.length() < 1 || id.length() > 16
               || id.charAt(0) < 'A' || id.charAt(0) > 'Z'
               || (\exists int k; 1 <= k && k < id.length();
                      !(('A' <= id.charAt(k) && id.charAt(k) <= 'Z')
                     || ('0' <= id.charAt(k) && id.charAt(k) <= '9')
                     || id.charAt(k) == '-' || id.charAt(k) == '_'));
         assignable \nothing;
         signals_only IllegalArgumentException;
    */

    public static void requireIdentifier(/*@ nullable @*/ String id) {
        if (id == null || 
            id.isBlank() ||
            id.length() == 0 || 
            id.length() > 16 || 
            id.charAt(0) < 'A' || 
            id.charAt(0) > 'Z') {

            throw new IllegalArgumentException("Invalid id input");
        }

        //Every character before i has been checked and is allowed.
        /* maintaining 1 <= i && i <= id.length();
           maintaining (\forall int k; 1 <= k && k < i;
                           ('A' <= id.charAt(k) && id.charAt(k) <= 'Z')
                        || ('0' <= id.charAt(k) && id.charAt(k) <= '9')
                        || id.charAt(k) == '-' || id.charAt(k) == '_');
           loop_assigns \nothing;
           decreasing id.length() - i;
        */
       
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