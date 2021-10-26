package me.racci.bloodnight.command.bloodnight.managedeathactions

import de.eldoria.eldoutilities.simplecommands.EldoCommand
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil
import de.eldoria.eldoutilities.utils.ArgumentUtils
import me.racci.bloodnight.command.util.CommandUtil
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.deathactions.MobDeathActions
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class ManageMonsterDeathActions(
    plugin: Plugin,
    val configuration: Configuration,
    private val bukkitAudiences: BukkitAudiences
) : EldoCommand(plugin) {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val player: Player = sender as Player
        val world = ArgumentUtils.getOrDefault(
            args,
            0,
            { world: String? -> ArgumentUtils.getWorld(world) },
            player.world
        )
        if (world == null) {
            messageSender().sendLocalizedError(sender, "error.invalidWorld")
            return true
        }
        val mobDeathActions: MobDeathActions =
            configuration.getWorldSettings(world).deathActionSettings.mobDeathActions
        if (args.size < 2) {
            sendMobDeathActions(player, world, mobDeathActions)
            return true
        }
        if (argumentsInvalid(
                sender, args, 1,
                "<monster|player> <\$syntax.worldName$> [<\$syntax.field$> <\$syntax.value$>]"
            )
        ) return true
        val field = args[1]
        val value: String = ArgumentUtils.getOrDefault(args, 2, "none")
        if ("lightning".equals(field, ignoreCase = true)) {
            DeathActionUtil.buildLightningUI(
                mobDeathActions.lightningSettings,
                player,
                configuration,
                localizer()
            ) { sendMobDeathActions(player, world, mobDeathActions) }
            return true
        }
        if ("shockwave".equals(field, ignoreCase = true)) {
            DeathActionUtil.buildShockwaveUI(
                mobDeathActions.shockwaveSettings, player, configuration, localizer()
            ) { sendMobDeathActions(player, world, mobDeathActions) }
            return true
        }
        messageSender().sendError(sender, "error.invalidField")
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        if (args.size == 1) {
            return TabCompleteUtil.completeWorlds(args[0])
        }
        return if (args.size == 2) {
            TabCompleteUtil.complete(args[1], "lightning", "shockwave")
        } else emptyList()
    }

    private fun sendMobDeathActions(player: Player, world: World, mobDeathActions: MobDeathActions) {
        val cmd = "/bloodnight deathActions monster " + ArgumentUtils.escapeWorldName(world.name) + " "
        val build: TextComponent = Component.text()
            .append(CommandUtil.getHeader(localizer().getMessage("manageDeathActions.monster.title")))
            .append(Component.newline())
            .append(Component.text(localizer().getMessage("field.lightningSettings"), NamedTextColor.AQUA))
            .append(
                Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand(cmd + "lightning"))
            )
            .append(Component.newline())
            .append(Component.text(localizer().getMessage("field.shockwaveSettings"), NamedTextColor.AQUA))
            .append(
                Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand(cmd + "shockwave"))
            )
            .build()
        bukkitAudiences.player(player).sendMessage(build)
    }
}