package xyz.practice.pingcrystal;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.practice.pingcrystal.commands.PingCrystalCommand;
import xyz.practice.pingcrystal.listeners.CrystalDamageListener;
import xyz.practice.pingcrystal.listeners.CrystalPlaceListener;
import xyz.practice.pingcrystal.tracking.PositionTracker;
import xyz.practice.pingcrystal.util.Settings;

public class PingCrystalPlugin extends JavaPlugin {

    private Settings settings;
    private PositionTracker positionTracker;
    private NamespacedKey placerKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.placerKey = new NamespacedKey(this, "crystal_placer");
        this.settings = new Settings(this);
        this.positionTracker = new PositionTracker(this);
        this.positionTracker.start();

        Bukkit.getPluginManager().registerEvents(new CrystalPlaceListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CrystalDamageListener(this), this);

        var command = getCommand("pingcrystal");
        if (command != null) {
            command.setExecutor(new PingCrystalCommand(this));
        }

        boolean floodgate = Bukkit.getPluginManager().isPluginEnabled("floodgate");
        boolean geyser = Bukkit.getPluginManager().isPluginEnabled("Geyser-Spigot");
        getLogger().info("Floodgate detected: " + floodgate + " | Geyser-Spigot detected: " + geyser);
        getLogger().info("CrystalPingCompensation enabled.");
    }

    @Override
    public void onDisable() {
        if (positionTracker != null) {
            positionTracker.stop();
            positionTracker.clear();
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public PositionTracker getPositionTracker() {
        return positionTracker;
    }

    public NamespacedKey getPlacerKey() {
        return placerKey;
    }
}
