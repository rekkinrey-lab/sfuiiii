package xyz.practice.pingcrystal.tracking;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.practice.pingcrystal.PingCrystalPlugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps a short rolling history of every online player's location so we can
 * later ask "where was this player approximately N ms ago?".
 *
 * History length is driven by settings.history-window-ms in config.yml.
 */
public class PositionTracker {

    private final PingCrystalPlugin plugin;
    private final Map<UUID, Deque<TimestampedLocation>> history = new ConcurrentHashMap<>();

    private BukkitRunnable task;

    public PositionTracker(PingCrystalPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long windowMs = plugin.getSettings().getHistoryWindowMs();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Deque<TimestampedLocation> deque =
                            history.computeIfAbsent(player.getUniqueId(), id -> new ArrayDeque<>());

                    deque.addLast(new TimestampedLocation(now, player.getLocation()));

                    // Trim anything older than the configured window.
                    while (!deque.isEmpty() && now - deque.peekFirst().getTimestampMs() > windowMs) {
                        deque.pollFirst();
                    }
                }

                // Clean up players who have left.
                history.keySet().removeIf(id -> Bukkit.getPlayer(id) == null);
            }
        };
        // Every tick (20/sec) so we have fine-grained history to rewind through.
        task.runTaskTimer(plugin, 1L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Returns the location closest to (now - delayMs) that we have recorded
     * for this player, or null if we have no history for them yet.
     */
    public org.bukkit.Location getPastLocation(Player player, long delayMs) {
        Deque<TimestampedLocation> deque = history.get(player.getUniqueId());
        if (deque == null || deque.isEmpty()) {
            return null;
        }

        long targetTime = System.currentTimeMillis() - delayMs;

        TimestampedLocation best = null;
        for (TimestampedLocation snapshot : deque) {
            if (snapshot.getTimestampMs() <= targetTime) {
                best = snapshot; // keep walking forward, we want the closest one <= targetTime
            } else {
                break;
            }
        }

        // If everything in history is newer than targetTime (e.g. very fresh
        // history buffer), fall back to the oldest snapshot we have.
        if (best == null) {
            best = deque.peekFirst();
        }

        return best.getLocation();
    }

    public void clear() {
        history.clear();
    }
}
