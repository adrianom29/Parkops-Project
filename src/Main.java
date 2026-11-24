
import java.util.List;

public final class Main {

    public static void main(String[] args) throws Exception {
        String mode = args.length == 0 ? "fixture" : args[0];
        if (mode.equals("fixture")) {
            System.out.println("SYNTHETIC FIXTURE; no model invocation or booking mutation.");
            System.out.println(MiniJson.stringify(Fixture.booking()));
            System.out.println(MiniJson.stringify(Fixture.spaces()));
            return;
        }
        StudentApplication app = new StudentApplication();
        try {
            if (mode.equals("scripted")) {
                System.out.println("REPLAY D1 input: not a live model run; approval/execution are not invoked.");
                ReplayProvider replay = new ReplayProvider(List.of(
                        Fixture.proposalRequest("B12"),
                        Fixture.proposalRequest("D09"),
                        Fixture.proposalRequest("Z99")));
                for (int i = 0; i < 3; i++) {
                    var input = MiniJson.object(MiniJson.parse(replay.next(List.of(new ChatMessage("user", "Propose a replacement for B1")))));
                    String target = (String) input.get("targetId");
                    System.out.println("--- Proposed target: " + target);
                    try {
                        ProposalView p = app.propose(target);
                        System.out.println(p);
                    } catch (IllegalArgumentException ex) {
                        System.out.println("REJECTED: " + ex.getMessage());
                    }
                    System.out.println("Booking: " + MiniJson.stringify(app.bookingSnapshot()));
                }
            } 
            else if (mode.equals("agent-scripted")) {
                System.out.println("REPLAY D2 workflow; not a live model run.");
                System.out.println(app.run(Fixture.replay(), 6));
            } 
            else if (mode.equals("d3-demo")) {
                System.out.println("D3 DEMO: scripted calls to the application; not a live model run.");
                d3Demo();
            }
            else if (mode.equals("live")) {
                System.out.println("LIVE Gemini: record model replies and tool observations.");
                System.out.println(app.run(GeminiProvider.fromEnvironment(), 6));            } 
            else {
                System.err.println("Modes: fixture | scripted (D1) | agent-scripted (D2) | d3-demo (D3) | live (D2)");
                System.exit(2);
            }
        } 
        catch (UnsupportedOperationException ex) {
            System.err.println("STARTER_NOT_IMPLEMENTED: " + ex.getMessage());
            System.exit(1);
        }
    }

    //D3.1: baseline vs. policy version 2, stale proposal, fresh proposal, one commit, periodic tick.
    private static void d3Demo() {
        System.out.println("=== 1. Baseline (policy 1), B1 requires accessible ===");
        StudentApplication base = new StudentApplication();
        System.out.println("policy=" + base.policyVersion() + " eligible=" + base.eligibleTargets());

        System.out.println("=== 2. Policy 2 on a clean fixture ===");
        StudentApplication app = new StudentApplication();
        app.activatePolicyVersion2();
        System.out.println("policy=" + app.policyVersion() + " eligible=" + app.eligibleTargets());
        tryPropose(app, "B12");

        System.out.println("=== 3. Version-1 proposal approved, then policy 2 activated ===");
        app = new StudentApplication();
        System.out.println(app.propose("C03"));
        System.out.println(app.evaluate("P1", true));
        app.activatePolicyVersion2();
        System.out.println(app.execute("P1"));
        printState(app);

        System.out.println("=== 4. Fresh version-2 proposal to C03, approve, execute, repeat ===");
        System.out.println(app.propose("C03"));
        System.out.println(app.evaluate("P2", true));
        System.out.println(app.execute("P2"));
        System.out.println(app.execute("P2"));
        printState(app);

        System.out.println("=== 5. Booking that does not require accessibility, policy 2 ===");
        StudentApplication plain = new StudentApplication(false);
        plain.activatePolicyVersion2();
        System.out.println("eligible=" + plain.eligibleTargets());

        System.out.println("=== 6. Periodic ticks across the policy change ===");
        StudentApplication ticker = new StudentApplication();
        System.out.println("tick 0:   " + ticker.tick(0));
        ticker.activatePolicyVersion2();
        System.out.println("tick 30:  " + ticker.tick(30));
        System.out.println("tick 60:  " + ticker.tick(60));
        System.out.println("tick 120: " + ticker.tick(120));
    }

    private static void tryPropose(StudentApplication app, String target) {
        try {
            System.out.println(app.propose(target));
        } catch (IllegalArgumentException ex) {
            System.out.println("REJECTED " + target + ": " + ex.getMessage());
        }
        printState(app);
    }

    private static void printState(StudentApplication app) {
        System.out.println("Booking: " + MiniJson.stringify(app.bookingSnapshot())
                + " proposals=" + app.proposalCount() + " notifications=" + app.notificationCount());
    }
}
