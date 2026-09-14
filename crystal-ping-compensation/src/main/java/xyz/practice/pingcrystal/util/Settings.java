package xyz.practice.pingcrystal.util;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.practice.pingcrystal.PingCrystalPlugin;

public class Settings {

    private final PingCrystalPlugin plugin;

    private boolean enabled;
    private int minPingThresholdMs;
    private int maxCompensationMs;
    private long historyWindowMs;
    private double pingCompensationFactor;
    private double maxDamageMultiplier;
    private boolean compensateKnockback;

    private boolean useFloodgateDetection;
    private boolean preferGeyserNativePing;

    private boolean debug;

    public Settings(PingCrystalPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        enabled = cfg.getBoolean("settings.enabled", true);
        minPingThresholdMs = cfg.getInt("settings.min-ping-threshold-ms", 80);
        maxCompensationMs = cfg.getInt("settings.max-compensation-ms", 250);
        historyWindowMs = cfg.getLong("settings.history-window-ms", 500);
        pingCompensationFactor = cfg.getDouble("settings.ping-compensation-factor", 1.0);
        maxDamageMultiplier = cfg.getDouble("settings.max-damage-multiplier", 1.5);
        compensateKnockback = cfg.getBoolean("settings.compensate-knockback", true);

        useFloodgateDetection = cfg.getBoolean("bedrock.use-floodgate-detection", true);
        preferGeyserNativePing = cfg.getBoolean("bedrock.prefer-geyser-native-ping", true);

        debug = cfg.getBoolean("debug", false);

        if (historyWindowMs < maxCompensationMs) {
            plugin.getLogger().warning("history-window-ms is smaller than max-compensation-ms; " +
                    "clamping history window up so compensation can't exceed recorded history.");
            historyWindowMs = maxCompensationMs;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMinPingThresholdMs() {
        return minPingThresholdMs;
    }

    public int getMaxCompensationMs() {
        return maxCompensationMs;
    }

    public long getHistoryWindowMs() {
        return historyWindowMs;
    }

    public double getPingCompensationFactor() {
        return pingCompensationFactor;
    }

    public double getMaxDamageMultiplier() {
        return maxDamageMultiplier;
    }

    public boolean isCompensateKnockback() {
        return compensateKnockback;
    }

    public boolean isUseFloodgateDetection() {
        return useFloodgateDetection;
    }

    public boolean isPreferGeyserNativePing() {
        return preferGeyserNativePing;
    }

    public boolean isDebug() {
        return debug;
    }
}
