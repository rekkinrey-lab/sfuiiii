package xyz.practice.pingcrystal.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.practice.pingcrystal.PingCrystalPlugin;
import xyz.practice.pingcrystal.util.Settings;

public class PingCrystalCommand implements CommandExecutor {

    private final PingCrystalPlugin plugin;

    public PingCrystalCommand(PingCrystalPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /pingcrystal <reload|status|toggle>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.getSettings().load();
                sender.sendMessage(ChatColor.GREEN + "[CrystalPingCompensation] Config reloaded.");
            }
            case "status" -> {
                Settings s = plugin.getSettings();
                sender.sendMessage(ChatColor.AQUA + "=== CrystalPingCompensation ===");
                sender.sendMessage("Enabled: " + s.isEnabled());
                sender.sendMessage("Min ping threshold: " + s.getMinPingThresholdMs() + "ms");
                sender.sendMessage("Max compensation: " + s.getMaxCompensationMs() + "ms");
                sender.sendMessage("Max damage multiplier: " + s.getMaxDamageMultiplier());
                sender.sendMessage("Floodgate detection: " + s.isUseFloodgateDetection());
                sender.sendMessage("Prefer native Geyser ping: " + s.isPreferGeyserNativePing());
            }
            case "toggle" -> {
                boolean newState = !plugin.getSettings().isEnabled();
                plugin.getConfig().set("settings.enabled", newState);
                plugin.saveConfig();
                plugin.getSettings().load();
                sender.sendMessage(ChatColor.GREEN + "[CrystalPingCompensation] Enabled set to " + newState);
            }
            default -> sender.sendMessage(ChatColor.YELLOW + "Usage: /pingcrystal <reload|status|toggle>");
        }
        return true;
    }
}
