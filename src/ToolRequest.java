
//A model reply that passed protocol validation. Being well formed is not permission:
//the runner still checks tool ordering, and the domain policy still checks eligibility.

record ToolRequest(Tool tool, String bookingId, String targetId) {
}
