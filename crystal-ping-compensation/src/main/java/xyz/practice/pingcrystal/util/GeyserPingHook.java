package xyz.practice.pingcrystal.util;

import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.UUID;

/**
 * Kept in its own class so referencing Geyser's API classes doesn't trigger
 * class loading unless Geyser-Spigot is actually installed. Only call into
 * this class after checking the Geyser-Spigot plugin is enabled.
 *
 * Geyser's API surface has changed between versions in the past - if this
 * fails to compile against the Geyser version you run, either bump the
 * dependency version in pom.xml or delete this class and the corresponding
 * call site in PingUtil (Bedrock players will just fall back to the normal
 * Bukkit ping, which still works fine).
 */
final class GeyserPingHook {

    private GeyserPingHook() {
    }

    /**
     * Returns the real RakNet ping for a connected Bedrock player, or -1 if
     * it can't be determined (player not found, API mismatch, etc.).
     */
    static int getNativePing(UUID javaUuid) {
        try {
            GeyserConnection connection = GeyserApi.api().connectionByUuid(javaUuid);
            if (connection == null) {
                return -1;
            }
            return connection.ping();
        } catch (Throwable t) {
            return -1;
        }
    }
}
