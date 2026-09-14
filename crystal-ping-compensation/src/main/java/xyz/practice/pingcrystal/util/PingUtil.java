package xyz.practice.pingcrystal.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.practice.pingcrystal.PingCrystalPlugin;

public final class PingUtil {

    private PingUtil() {
    }

    /**
     * Returns the ping (ms) to use for compensation purposes for this player.
     * For Bedrock players (via Floodgate) with Geyser-Spigot installed, this
     * prefers the native RakNet ping since it's typically more accurate than
     * the Java keep-alive ping Bukkit reports for proxied Bedrock clients.
     */
    public static int getEffectivePing(PingCrystalPlugin plugin, Player player) {
        Settings settings = plugin.getSettings();

        if (settings.isUseFloodgateDetection() && isFloodgateEnabled()) {
            boolean isBedrock = FloodgateHook.isBedrockPlayer(player.getUniqueId());

            if (isBedrock && settings.isPreferGeyserNativePing() && isGeyserEnabled()) {
                int nativePing = GeyserPingHook.getNativePing(player.getUniqueId());
                if (nativePing >= 0) {
                    return nativePing;
                }
            }
        }

        return player.getPing();
    }

    private static boolean isFloodgateEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("floodgate");
    }

    private static boolean isGeyserEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("Geyser-Spigot");
    }
}
