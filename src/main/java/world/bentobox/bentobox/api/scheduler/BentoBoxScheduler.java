package world.bentobox.bentobox.api.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Platform-neutral task scheduler.
 * <p>
 * On Paper/Spigot every sync context runs on the single main thread, so the
 * {@code runGlobal*}, {@code runAtLocation*} and {@code runAtEntity*} families are equivalent.
 * On Folia each family maps to a different scheduler ({@code GlobalRegionScheduler},
 * {@code RegionScheduler}, {@code EntityScheduler}) and picking the right one matters:
 * <ul>
 * <li>{@code runGlobal*} — bookkeeping not tied to a world position: world registration,
 * command bookkeeping, cache maintenance, firing plugin-level events.</li>
 * <li>{@code runAtLocation*} — anything that reads or writes blocks, chunks or entities
 * <em>around a position</em>: pasting blueprints, clearing mobs in an area, scanning chunks.</li>
 * <li>{@code runAtEntity*} — anything acting on a specific entity: teleporting a player,
 * closing an inventory, sending a dialog, applying effects.</li>
 * <li>{@code runAsync*} — off-thread work with no Bukkit API access: database I/O, web requests.</li>
 * </ul>
 * All delays and periods are in server ticks, matching {@link org.bukkit.scheduler.BukkitScheduler}
 * conventions. On Folia, delays below one tick are clamped to one tick.
 * <p>
 * Obtain the instance via {@code BentoBox.getInstance().getScheduler()}. Addons should use this
 * instead of {@code Bukkit.getScheduler()} to stay Folia-compatible.
 *
 * @since 3.23.0
 */
public interface BentoBoxScheduler {

    /**
     * @return true if this server runs Folia (regionized multithreading)
     */
    boolean isFolia();

    // --- Global context ---

    /**
     * Runs a task on the next tick in the global context.
     * @param task task to run
     * @return task handle
     */
    @NonNull
    SchedulerTask runGlobal(@NonNull Runnable task);

    /**
     * Runs a task in the global context after a delay.
     * @param task task to run
     * @param delayTicks delay in ticks
     * @return task handle
     */
    @NonNull
    SchedulerTask runGlobalLater(@NonNull Runnable task, long delayTicks);

    /**
     * Runs a repeating task in the global context.
     * @param task task to run
     * @param delayTicks initial delay in ticks
     * @param periodTicks period in ticks
     * @return task handle
     */
    @NonNull
    SchedulerTask runGlobalTimer(@NonNull Runnable task, long delayTicks, long periodTicks);

    // --- Region (location) context ---

    /**
     * Runs a task on the next tick on the region that owns the given location.
     * @param location location whose region the task must run on
     * @param task task to run
     * @return task handle
     */
    @NonNull
    SchedulerTask runAtLocation(@NonNull Location location, @NonNull Runnable task);

    /**
     * Runs a task on the region that owns the given location after a delay.
     * @param location location whose region the task must run on
     * @param task task to run
     * @param delayTicks delay in ticks
     * @return task handle
     */
    @NonNull
    SchedulerTask runAtLocationLater(@NonNull Location location, @NonNull Runnable task, long delayTicks);

    /**
     * Runs a repeating task on the region that owns the given location.
     * @param location location whose region the task must run on
     * @param task task to run
     * @param delayTicks initial delay in ticks
     * @param periodTicks period in ticks
     * @return task handle
     */
    @NonNull
    SchedulerTask runAtLocationTimer(@NonNull Location location, @NonNull Runnable task, long delayTicks,
            long periodTicks);

    // --- Entity context ---

    /**
     * Runs a task on the next tick on the thread that owns the given entity.
     * @param entity entity the task acts upon
     * @param task task to run
     * @return task handle; {@link SchedulerTask#CANCELLED} if the entity was already removed
     */
    @NonNull
    SchedulerTask runAtEntity(@NonNull Entity entity, @NonNull Runnable task);

    /**
     * Runs a task on the thread that owns the given entity after a delay.
     * @param entity entity the task acts upon
     * @param task task to run
     * @param delayTicks delay in ticks
     * @return task handle; {@link SchedulerTask#CANCELLED} if the entity was already removed
     */
    @NonNull
    SchedulerTask runAtEntityLater(@NonNull Entity entity, @NonNull Runnable task, long delayTicks);

    /**
     * Runs a repeating task on the thread that owns the given entity.
     * @param entity entity the task acts upon
     * @param task task to run
     * @param delayTicks initial delay in ticks
     * @param periodTicks period in ticks
     * @return task handle; {@link SchedulerTask#CANCELLED} if the entity was already removed
     */
    @NonNull
    SchedulerTask runAtEntityTimer(@NonNull Entity entity, @NonNull Runnable task, long delayTicks, long periodTicks);

    // --- Async context ---

    /**
     * Runs a task asynchronously (off any server thread).
     * @param task task to run
     * @return task handle
     */
    @NonNull
    SchedulerTask runAsync(@NonNull Runnable task);

    /**
     * Runs a task asynchronously after a delay.
     * @param task task to run
     * @param delayTicks delay in ticks (converted to wall-clock time on Folia)
     * @return task handle
     */
    @NonNull
    SchedulerTask runAsyncLater(@NonNull Runnable task, long delayTicks);

    /**
     * Runs a repeating task asynchronously.
     * @param task task to run
     * @param delayTicks initial delay in ticks (converted to wall-clock time on Folia)
     * @param periodTicks period in ticks (converted to wall-clock time on Folia)
     * @return task handle
     */
    @NonNull
    SchedulerTask runAsyncTimer(@NonNull Runnable task, long delayTicks, long periodTicks);

    /**
     * Cancels all tasks scheduled by BentoBox where the platform supports bulk cancellation.
     * On Folia this cancels global and async tasks; per-region and per-entity tasks cannot be
     * bulk-cancelled and are cancelled individually via their {@link SchedulerTask} handles.
     */
    void cancelAll();
}
