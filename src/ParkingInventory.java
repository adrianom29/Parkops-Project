import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

//Data storage for the booking and the spaces. Holds state only; rules live in ReassignmentPolicy.
final class ParkingInventory {

    private final Booking booking;
    private final Map<String, Space> spaces;

    private ParkingInventory(Booking booking, Map<String, Space> spaces) {
        this.booking = booking;
        this.spaces = spaces;
    }

    static ParkingInventory fromFixture() {
        Map<String, Object> raw = Fixture.booking();
        Booking booking = new Booking(
                (String) raw.get("id"),
                (String) raw.get("spaceId"),
                (Integer) raw.get("durationHours"),
                (Integer) raw.get("version"),
                (Boolean) raw.get("requiresAccessible"));

        Map<String, Space> spaces = new LinkedHashMap<>();   // keeps fixture order for listings
        for (Map<String, Object> s : Fixture.spaces()) {
            Space space = new Space(
                    (String) s.get("id"),
                    (Boolean) s.get("open"),
                    (Boolean) s.get("occupied"),
                    (Boolean) s.get("accessible"));
            spaces.put(space.getId(), space);
        }
        return new ParkingInventory(booking, spaces);
    }

    Booking booking() {
        return booking;
    }

    //The space with this id, or null if unknown.
    Space space(String id) {
        return spaces.get(id);
    }

    Collection<Space> spaces() {
        return Collections.unmodifiableCollection(spaces.values());
    }
}
