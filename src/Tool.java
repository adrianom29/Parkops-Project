//Model protocol: the only four tools a model reply may request.
enum Tool {
    READ_BOOKING, LIST_ALTERNATIVES, PROPOSE_REASSIGNMENT, FINISH;

    static Tool fromName(String name) {
        for (Tool tool : values()) {
            if (tool.name().equals(name)) {
                return tool;
            }
        }
        throw new IllegalArgumentException("unknown tool " + name);
    }
}
