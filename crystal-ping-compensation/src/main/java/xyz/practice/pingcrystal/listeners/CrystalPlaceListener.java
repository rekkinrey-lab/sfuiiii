package xyz.practice.pingcrystal.listeners;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.persistence.PersistentDataType;
import xyz.practice.pingcrystal.PingCrystalPlugin;

/**
 * Tags every placed end crystal with the placer's UUID so that, when it later
 * explodes, we know whose ping to use for compensation.
 */
public class CrystalPlaceListener implements Listener {

    private final PingCrystalPlugin plugin;

    public CrystalPlaceListener(PingCrystalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal crystal)) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        crystal.getPersistentDataContainer().set(
                plugin.getPlacerKey(),
                PersistentDataType.STRING,
                player.getUniqueId().toString()
        );
    }
}
