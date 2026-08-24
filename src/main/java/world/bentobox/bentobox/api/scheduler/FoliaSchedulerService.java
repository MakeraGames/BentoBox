package world.bentobox.bentobox.api.scheduler;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

/**
 * {@link BentoBoxScheduler} implementation for Folia's regionized schedulers.
 * <p>
 * Notes on semantics:
 * <ul>
 * <li>Folia rejects delays and periods below one tick, so they are clamped to one.</li>
 * <li>The async scheduler is wall-clock based; tick values are converted at 50 ms per tick.</li>
 * <li>Entity scheduling returns {@link SchedulerTask#CANCELLED} if the entity has been removed,
 * matching the null return of Folia's {@code EntityScheduler}.</li>
 * </ul>
 *
 * @since 3.23.0
 */
public class FoliaSchedulerService implements BentoBoxScheduler {

    private static final long MS_PER_TICK = 50L;

    private final Plugin plugin;

    public FoliaSchedulerService(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFolia() {
        return true;
    }

    @Override
    @NonNull
    public SchedulerTask runGlobal(@NonNull Runnable task) {
        return wrap(Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run()));
    }

    @Override
    @NonNull
    public SchedulerTask runGlobalLater(@NonNull Runnable task, long delayTicks) {
        return wrap(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), clamp(delayTicks)));
    }

    @Override
    @NonNull
    public SchedulerTask runGlobalTimer(@NonNull Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), clamp(delayTicks),
                clamp(periodTicks)));
    }

    @Override
    @NonNull
    public SchedulerTask runAtLocation(@NonNull Location location, @NonNull Runnable task) {
        return wrap(Bukkit.getRegionScheduler().run(plugin, location, t -> task.run()));
    }

    @Override
    @NonNull
    public SchedulerTask runAtLocationLater(@NonNull Location location, @NonNull Runnable task, long delayTicks) {
        return wrap(
                Bukkit.getRegionScheduler().runDelayed(plugin, location, t -> task.run(), clamp(delayTicks)));
    }

    @Override
    @NonNull
    public SchedulerTask runAtLocationTimer(@NonNull Location location, @NonNull Runnable task, long delayTicks,
            long periodTicks) {
        return wrap(Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, t -> task.run(), clamp(delayTicks),
                clamp(periodTicks)));
    }

    @Override
    @NonNull
    public SchedulerTask runAtEntity(@NonNull Entity entity, @NonNull Runnable task) {
        return wrap(entity.getScheduler().run(plugin, t -> task.run(), null));
    }

    @Override
    @NonNull
    public SchedulerTask runAtEntityLater(@NonNull Entity entity, @NonNull Runnable task, long delayTicks) {
        return wrap(entity.getScheduler().runDelayed(plugin, t -> task.run(), null, clamp(delayTicks)));
    }

    @Override
    @NonNull
    public SchedulerTask runAtEntityTimer(@NonNull Entity entity, @NonNull Runnable task, long delayTicks,
            long periodTicks) {
        return wrap(entity.getScheduler().runAtFixedRate(plugin, t -> task.run(), null, clamp(delayTicks),
                clamp(periodTicks)));
    }

    @Override
    @NonNull
    public SchedulerTask runAsync(@NonNull Runnable task) {
        return wrap(Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run()));
    }

    @Override
    @NonNull
    public SchedulerTask runAsyncLater(@NonNull Runnable task, long delayTicks) {
        return wrap(Bukkit.getAsyncScheduler().runDelayed(plugin, t -> task.run(), clamp(delayTicks) * MS_PER_TICK,
                TimeUnit.MILLISECONDS));
    }

    @Override
    @NonNull
    public SchedulerTask runAsyncTimer(@NonNull Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, t -> task.run(),
                clamp(delayTicks) * MS_PER_TICK, clamp(periodTicks) * MS_PER_TICK, TimeUnit.MILLISECONDS));
    }

    @Override
    public void cancelAll() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
    }

    private static long clamp(long ticks) {
        return Math.max(1L, ticks);
    }

    @NonNull
    private static SchedulerTask wrap(@Nullable ScheduledTask task) {
        if (task == null) {
            // Folia's EntityScheduler returns null when the entity is already removed
            return SchedulerTask.CANCELLED;
        }
        return new SchedulerTask() {
            @Override
            public void cancel() {
                task.cancel();
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }
        };
    }
}
