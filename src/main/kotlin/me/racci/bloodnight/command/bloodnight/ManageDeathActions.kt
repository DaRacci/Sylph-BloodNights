package me.racci.bloodnight.command.bloodnight

import de.eldoria.eldoutilities.conversation.ConversationRequester
import de.eldoria.eldoutilities.simplecommands.EldoCommand
import me.racci.bloodnight.command.bloodnight.managedeathactions.ManageMonsterDeathActions
import me.racci.bloodnight.command.bloodnight.managedeathactions.ManagePlayerDeathActions
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.util.Permissions
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class ManageDeathActions(plugin: Plugin, configuration: Configuration) : EldoCommand(plugin) {

    private val configuration: Configuration
    private val conversationRequester: ConversationRequester

    // <monster|player> <world> <field> <value>
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (denyConsole(sender)) {
            return true
        }
        if (denyAccess(sender, Permissions.Admin.MANAGE_DEATH_ACTION)) {
            return true
        }
        return if (argumentsInvalid(
                sender,
                args,
                1,
                "<monster|player> <\$syntax.worldName$> [<\$syntax.field$> <\$syntax.value$>]"
            )
        ) {
            true
        } else super.onCommand(sender, command, label, args)
    }

    init {
        this.configuration = configuration
        conversationRequester = ConversationRequester.start(plugin)
        val bukkitAudiences = BukkitAudiences.create(getPlugin())
        registerCommand("monster", ManageMonsterDeathActions(plugin, configuration, bukkitAudiences))
        registerCommand(
            "player",
            ManagePlayerDeathActions(plugin, configuration, conversationRequester, bukkitAudiences)
        )
    }
}