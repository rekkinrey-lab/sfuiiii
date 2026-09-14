package xyz.practice.pingcrystal.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import xyz.practice.pingcrystal.PingCrystalPlugin;
import xyz.practice.pingcrystal.util.PingUtil;
import xyz.practice.pingcrystal.util.Settings;

import java.util.UUID;

/**
 * When an end crystal explodes and damages someone, this rewinds the
 * victim's position back by the attacker's (capped) ping and, if that
 * rewound position was meaningfully closer to the crystal than where the
 * victim ended up on the server, scales the damage/knockback up to match
 * what the attacker's client actually saw.
 *
 * This never reduces damage below what vanilla would have dealt - it only
 * ever compensates in the attacker's favor, and only above the configured
 * minimum ping threshold, so normal-ping players are completely unaffected.
 */
public class CrystalDamageListener implements Listener {

    private final PingCrystalPlugin plugin;

    public CrystalDamageListener(PingCrystalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCrystalDamage(EntityDamageByEntityEvent event) {
        Settings settings = plugin.getSettings();
        if (!settings.isEnabled()) {
            return;
        }

        if (!(event.getDamager() instanceof EnderCrystal crystal)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (victim.hasPermission("pingcrystal.exempt")) {
            return;
        }

        UUID placerId = getPlacerId(crystal);
        if (placerId == null || placerId.equals(victim.getUniqueId())) {
            return;
        }

        Player attacker = Bukkit.getPlayer(placerId);
        if (attacker == null) {
            return;
        }

        int ping = PingUtil.getEffectivePing(plugin, attacker);
        if (ping < settings.getMinPingThresholdMs()) {
            return;
        }

        long delayMs = Math.min(
                Math.round(ping * settings.getPingCompensationFactor()),
                settings.getMaxCompensationMs()
        );
        if (delayMs <= 0) {
            return;
        }

        Location past = plugin.getPositionTracker().getPastLocation(victim, delayMs);
        if (past == null || past.getWorld() == null) {
            return;
        }

        Location crystalLoc = crystal.getLocation();
        if (past.getWorld() != crystalLoc.getWorld()) {
            return;
        }

        double currentDist = victim.getLocation().distance(crystalLoc);
        double pastDist = past.distance(crystalLoc);

        // Victim was farther away (or same) back then - nothing to compensate.
        if (pastDist >= currentDist) {
            return;
        }

        double safePastDist = Math.max(pastDist, 0.5);
        double multiplier = Math.min(currentDist / safePastDist, settings.getMaxDamageMultiplier());

        if (multiplier > 1.01) {
            double newDamage = event.getDamage() * multiplier;
            event.setDamage(newDamage);

            if (settings.isDebug()) {
                plugin.getLogger().info(String.format(
                        "[PingCompensation] %s -> %s | ping=%dms delay=%dms dist %.2f->%.2f multiplier=%.2f",
                        attacker.getName(), victim.getName(), ping, delayMs, currentDist, pastDist, multiplier
                ));
            }

            if (settings.isCompensateKnockback()) {
                Vector direction = past.toVector().subtract(crystalLoc.toVector());
                if (direction.lengthSquared() > 0.0001) {
                    Vector normalized = direction.normalize();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Vector currentVelocity = victim.getVelocity();
                        double speed = currentVelocity.length();
                        if (speed > 0.0001) {
                            victim.setVelocity(normalized.multiply(speed));
                        }
                    });
                }
            }
        }
    }

    private UUID getPlacerId(EnderCrystal crystal) {
        String raw = crystal.getPersistentDataContainer().get(
                plugin.getPlacerKey(), PersistentDataType.STRING
        );
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
