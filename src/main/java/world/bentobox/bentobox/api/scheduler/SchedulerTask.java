package world.bentobox.bentobox.api.scheduler;

/**
 * A handle to a task scheduled through {@link BentoBoxScheduler}.
 * <p>
 * This is the platform-neutral replacement for {@link org.bukkit.scheduler.BukkitTask}:
 * on Paper/Spigot it wraps a {@code BukkitTask}, on Folia it wraps a
 * {@code io.papermc.paper.threadedregions.scheduler.ScheduledTask}.
 *
 * @since 3.23.0
 */
public interface SchedulerTask {

    /**
     * Cancels this task. Cancelling a task that has already run or been cancelled is a no-op.
     */
    void cancel();

    /**
     * @return true if this task has been cancelled
     */
    boolean isCancelled();

    /**
     * A task handle that is already cancelled. Returned when a task could not be scheduled,
     * e.g. scheduling on an entity that has been removed on Folia.
     */
    SchedulerTask CANCELLED = new SchedulerTask() {
        @Override
        public void cancel() {
            // Nothing to cancel
        }

        @Override
        public boolean isCancelled() {
            return true;
        }
    };
}
