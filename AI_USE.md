# Student AI-use disclosure

Record your own work below. The course-provided starter has separate provenance.

| Date | Tool/model | Task | Files affected | What I changed or rejected | Verification |
|---|---|---|---|---|---|
| Oct 5 2026 | Claude Code VS code Extension Opus 5.5 | Made constructor where booking and four spaces are built from Fixture.java instead of map used by starter. Implemented bookingSnapshot() and propose() methods | StudentApplication.java | Changed exception error messages | Ran and passed all starter code junit test cases |
| Oct 7 2026 | Claude Code VS code Extension Opus 5.5 | Updated test cases so that they properly test cases listed in TEST_PLAN.md (specifically D1.D, D1.E and D1.F) | StudentApplication.java, StudentTests.java | instead of calling bookingSnapshot() multiple times, I made "after" objects for comparisons | Ran all starter code JUnit tests and tests in StudentTests.java and they all passed |
| Oct 26 2026 | Claude Code VS code Extension Opus 5.5 | Generated system message and user message for run() method | StudentApplication.java | Instead of having prompts as static final strings, i build each chat message with the prompts directly in run() | not needed |
| Oct 27 2026 | Claude Code VS code Extension Opus 5.5 | Explained behaviour for each tool for tool loop in run(). Provided sample code for READ_BOOKING, LIST_ALTERNATIVES, and PROPOSE_REASSIGNMENT| StudentApplication.java | Wrote the code myself. Chose specific design of code, wrote custom messages for ChatMessage list, and more | Ran and passed D2 junit test cases  |


If you used no generative AI for your own work, state that explicitly here.
Do not claim that the supplied starter was created without assistance.
