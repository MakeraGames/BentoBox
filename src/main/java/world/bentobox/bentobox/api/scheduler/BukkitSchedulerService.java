package world.bentobox.bentobox.api.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link BentoBoxScheduler} implementation for Paper/Spigot, where everything synchronous runs
 * on the single main thread. All sync contexts delegate to {@link org.bukkit.scheduler.BukkitScheduler}.
 *
 * @since 3.23.0
 */
public class BukkitSchedulerService implements BentoBoxScheduler {

    private final Plugin plugin;

    public BukkitSchedulerService(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFolia() {
        return false;
    }

    @Override
    @NonNull
    public SchedulerTask runGlobal(@NonNull Runnable task) {
        return wrap(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    @NonNull
    public SchedulerTask runGlobalLater(@NonNull Runnable task, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    @Override
    @NonNull
    public SchedulerTask runGlobalTimer(@NonNull Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
    }

    @Override
    @NonNull
    public SchedulerTask runAtLocation(@NonNull Location location, @NonNull Runnable task) {
        return runGlobal(task);
    }

    @Override
    @NonNull
    public SchedulerTask runAtLocationLater(@NonNull Location location, @NonNull Runnable task, long delayTicks) {
        return runGlobalLater(task, delayTicks);
    }

    @Override
    @NonNull
    public SchedulerTask runAtLocationTimer(@NonNull Location location, @NonNull Runnable task, long delayTicks,
            long periodTicks) {
        return runGlobalTimer(task, delayTicks, periodTicks);
    }

    @Override
    @NonNull
    public SchedulerTask runAtEntity(@NonNull Entity entity, @NonNull Runnable task) {
        return runGlobal(task);
    }

    @Override
    @NonNull
    public SchedulerTask runAtEntityLater(@NonNull Entity entity, @NonNull Runnable task, long delayTicks) {
        return runGlobalLater(task, delayTicks);
    }

    @Override
    @NonNull
    public SchedulerTask runAtEntityTimer(@NonNull Entity entity, @NonNull Runnable task, long delayTicks,
            long periodTicks) {
        return runGlobalTimer(task, delayTicks, periodTicks);
    }

    @Override
    @NonNull
    public SchedulerTask runAsync(@NonNull Runnable task) {
        return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    @NonNull
    public SchedulerTask runAsyncLater(@NonNull Runnable task, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks));
    }

    @Override
    @NonNull
    public SchedulerTask runAsyncTimer(@NonNull Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public void cancelAll() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    /**
     * Wraps a BukkitTask into a SchedulerTask. Tolerates a null delegate (e.g. a mocked
     * scheduler in tests) by returning an inert handle.
     */
    @NonNull
    private static SchedulerTask wrap(@Nullable BukkitTask task) {
        if (task == null) {
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
