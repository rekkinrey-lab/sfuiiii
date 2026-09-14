package xyz.practice.pingcrystal.tracking;

import org.bukkit.Location;

/**
 * A single snapshot of a player's location at a given wall-clock time (ms).
 */
public final class TimestampedLocation {

    private final long timestampMs;
    private final Location location;

    public TimestampedLocation(long timestampMs, Location location) {
        this.timestampMs = timestampMs;
        this.location = location;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public Location getLocation() {
        return location;
    }
}
