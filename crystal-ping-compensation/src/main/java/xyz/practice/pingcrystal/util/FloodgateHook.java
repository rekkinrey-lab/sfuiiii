package xyz.practice.pingcrystal.util;

import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

/**
 * Kept in its own class so that referencing Floodgate's API classes doesn't
 * trigger class loading (and a NoClassDefFoundError) unless Floodgate is
 * actually installed. Only call into this class after checking the Floodgate
 * plugin is enabled.
 */
final class FloodgateHook {

    private FloodgateHook() {
    }

    static boolean isBedrockPlayer(UUID uuid) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
        } catch (Throwable t) {
            return false;
        }
    }
}
