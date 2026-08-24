package world.bentobox.bentobox.api.commands.admin.conversations;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

/**
 * Renames a home
 * @author tastybento
 *
 */
public class NamePrompt extends StringPrompt {

    @NonNull
    private final Island island;
    @NonNull
    private final User user;
    private final String oldName;
    private final BentoBox plugin;

    public NamePrompt(BentoBox plugin, @NonNull Island island, @NonNull User user, String oldName) {
        this.plugin = plugin;
        this.island = island;
        this.user = user;
        this.oldName = oldName;
    }

    @Override
    @NonNull
    public String getPromptText(@NonNull ConversationContext context) {
        return user.getTranslation("commands.island.renamehome.enter-new-name");
    }

    @Override
    public Prompt acceptInput(@NonNull ConversationContext context, String input) {
        if (island.renameHome(oldName, input)) {
            run(context, () -> user.sendMessage("general.success"));
        } else {
            run(context, () -> user.sendMessage("commands.island.renamehome.already-exists"));
        }
        return Prompt.END_OF_CONVERSATION;
    }

    /**
     * Runs the task on the conversing player's scheduler if available, otherwise globally.
     */
    private void run(ConversationContext context, Runnable task) {
        if (context.getForWhom() instanceof Player player) {
            plugin.getScheduler().runAtEntity(player, task);
        } else {
            plugin.getScheduler().runGlobal(task);
        }
    }

}
