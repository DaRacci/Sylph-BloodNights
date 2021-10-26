package me.racci.bloodnight.command.bloodnight

import de.eldoria.eldoutilities.localization.Replacement
import de.eldoria.eldoutilities.simplecommands.EldoCommand
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil
import de.eldoria.eldoutilities.utils.ArgumentUtils
import de.eldoria.eldoutilities.utils.ArrayUtil
import de.eldoria.eldoutilities.utils.EnumUtil
import de.eldoria.eldoutilities.utils.Parser
import me.racci.bloodnight.command.util.CommandUtil
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.NightSettings
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.util.Permissions
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class ManageNight(plugin: Plugin, val configuration: Configuration) : EldoCommand(plugin) {

    private val bukkitAudiences: BukkitAudiences = BukkitAudiences.create(BloodNight.instance)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (denyConsole(sender)) {
            return true
        }
        if (denyAccess(sender, Permissions.Admin.MANAGE_NIGHT)) {
            return true
        }
        val player: Player = getPlayerFromSender(sender)
        val world = ArgumentUtils.getOrDefault(
            args,
            0,
            { world: String? -> ArgumentUtils.getWorld(world) },
            player.world
        )
        if (world == null) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidWorld"))
            return true
        }
        val worldSettings: WorldSettings = configuration.getWorldSettings(world)
        if (args.size < 2) {
            sendNightSettings(sender, worldSettings)
            return true
        }
        if (argumentsInvalid(
                sender, args, 3,
                "[" + localizer().getMessage("syntax.worldName") + "] [<"
                        + localizer().getMessage("syntax.field") + "> <"
                        + localizer().getMessage("syntax.value") + ">]"
            )
        ) {
            return true
        }
        val cmd = args[1]
        val value = args[2]
        val optionalDouble: OptionalDouble = Parser.parseDouble(value)
        val optionalInt: OptionalInt = Parser.parseInt(value)
        val optionalBoolean = Parser.parseBoolean(value)
        val nightSettings: NightSettings = worldSettings.nightSettings
        if (ArrayUtil.arrayContains(arrayOf("enable", "skippable"), cmd)) {
            if (!optionalBoolean.isPresent) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidBoolean"))
                return true
            }
            if ("enable".equals(cmd, ignoreCase = true)) {
                worldSettings.enabled = (optionalBoolean.get())
            }
            if ("skippable".equals(cmd, ignoreCase = true)) {
                nightSettings.skippable = (optionalBoolean.get())
            }
            configuration.save()
            sendNightSettings(sender, worldSettings)
            return true
        }
        if (ArrayUtil.arrayContains(arrayOf("nightBegin", "nightEnd", "nightDuration", "maxNightDuration"), cmd)) {
            if (!optionalInt.isPresent) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidNumber"))
                return true
            }
            if ("nightBegin".equals(cmd, ignoreCase = true)) {
                if (invalidRange(sender, optionalInt.asInt, 0, 24000)) {
                    return true
                }
                nightSettings.nightBegin = (optionalInt.asInt)
            }
            if ("nightEnd".equals(cmd, ignoreCase = true)) {
                if (invalidRange(sender, optionalInt.asInt, 0, 24000)) {
                    return true
                }
                nightSettings.nightEnd = (optionalInt.asInt)
            }
            if ("nightDuration".equals(cmd, ignoreCase = true)) {
                if (invalidRange(sender, optionalInt.asInt, 0, 86400)) {
                    return true
                }
                nightSettings.nightDuration = (optionalInt.asInt)
            }
            if ("maxNightDuration".equals(cmd, ignoreCase = true)) {
                if (invalidRange(sender, optionalInt.asInt, nightSettings.nightDuration, 86400)) {
                    return true
                }
                nightSettings.maxNightDuration = (optionalInt.asInt)
            }
            configuration.save()
            sendNightSettings(sender, worldSettings)
            return true
        }
        if ("durationMode".equals(cmd, ignoreCase = true)) {
            val parse = EnumUtil.parse(value, NightSettings.NightDuration::class.java)
            if (parse == null) {
                messageSender().sendLocalizedError(sender, "error.invalidValue")
                return true
            }
            nightSettings.nightDurationMode = (parse)
            configuration.save()
            sendNightSettings(sender, worldSettings)
            return true
        }
        messageSender().sendError(player, localizer().getMessage("error.invalidField"))
        return true
    }

    private fun sendNightSettings(sender: CommandSender, worldSettings: WorldSettings) {
        val nightSettings: NightSettings = worldSettings.nightSettings
        val cmd = "/bloodnight manageNight " + ArgumentUtils.escapeWorldName(worldSettings.worldName) + " "
        val durationMode: NightSettings.NightDuration = nightSettings.nightDurationMode
        val builder: TextComponent.Builder = Component.text()
            .append(Component.newline())
            .append(Component.newline())
            .append(Component.newline())
            .append(Component.newline())
            .append(
                CommandUtil.getHeader(
                    localizer().getMessage(
                        "manageNight.title",
                        Replacement.create("WORLD", worldSettings.worldName).addFormatting('6')
                    )
                )
            )
            .append(Component.newline()) // World state
            .append(
                CommandUtil.getBooleanField(
                    worldSettings.enabled,
                    cmd + "enable {bool}",
                    localizer().getMessage("field.active"),
                    localizer().getMessage("state.enabled"),
                    localizer().getMessage("state.disabled")
                )
            )
            .append(Component.newline()) // skippable
            .append(
                CommandUtil.getBooleanField(
                    nightSettings.skippable,
                    cmd + "skippable {bool}",
                    localizer().getMessage("field.sleep"),
                    localizer().getMessage("state.allow"),
                    localizer().getMessage("state.deny")
                )
            )
            .append(Component.newline()) // night begin
            .append(Component.text(localizer().getMessage("field.nightBegin") + ": ", NamedTextColor.AQUA))
            .append(Component.text(nightSettings.nightBegin.toString() + " ", NamedTextColor.GOLD))
            .append(
                Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "nightBegin "))
            )
            .append(Component.newline()) // night end
            .append(Component.text(localizer().getMessage("field.nightEnd") + ": ", NamedTextColor.AQUA))
            .append(Component.text(nightSettings.nightEnd.toString() + " ", NamedTextColor.GOLD))
            .append(
                Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "nightEnd "))
            )
            .append(Component.newline()) // override night duration
            .append(
                CommandUtil.getToggleField(
                    durationMode === NightSettings.NightDuration.NORMAL,
                    cmd + "durationMode NORMAL",
                    localizer().getMessage("state.normal")
                )
            )
            .append(Component.space())
            .append(
                CommandUtil.getToggleField(
                    durationMode === NightSettings.NightDuration.EXTENDED,
                    cmd + "durationMode EXTENDED",
                    localizer().getMessage("state.extended")
                )
            )
            .append(Component.space())
            .append(
                CommandUtil.getToggleField(
                    durationMode === NightSettings.NightDuration.RANGE,
                    cmd + "durationMode RANGE",
                    localizer().getMessage("state.range")
                )
            )
            .append(Component.newline())
        when (durationMode) {
            NightSettings.NightDuration.NORMAL -> builder.append(Component.text(">", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(Component.text(">", NamedTextColor.GOLD))
            NightSettings.NightDuration.EXTENDED ->                 //night duration
                builder.append(
                    Component.text(
                        localizer().getMessage("field.nightDuration") + ": ",
                        NamedTextColor.AQUA
                    )
                )
                    .append(
                        Component.text(
                            nightSettings.nightDuration.toString() + " " + localizer().getMessage("value.seconds"),
                            NamedTextColor.GOLD
                        )
                    )
                    .append(
                        Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.suggestCommand(cmd + "nightDuration "))
                    )
                    .append(Component.newline())
                    .append(Component.text(">", NamedTextColor.GOLD))
            NightSettings.NightDuration.RANGE -> builder.append(
                Component.text(
                    localizer().getMessage("field.minDuration") + ": ",
                    NamedTextColor.AQUA
                )
            )
                .append(
                    Component.text(
                        nightSettings.nightDuration.toString() + " " + localizer().getMessage("value.seconds"),
                        NamedTextColor.GOLD
                    )
                )
                .append(
                    Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "nightDuration "))
                )
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.maxDuration") + ": ", NamedTextColor.AQUA))
                .append(
                    Component.text(
                        nightSettings.maxNightDuration.toString() + " " + localizer().getMessage("value.seconds"),
                        NamedTextColor.GOLD
                    )
                )
                .append(
                    Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "maxNightDuration "))
                )
        }
        bukkitAudiences.sender(sender).sendMessage(Identity.nil(), builder.build())
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        if (args.size == 1) {
            return TabCompleteUtil.completeWorlds(args[0])
        }
        if (args.size == 2) {
            return TabCompleteUtil.complete(
                args[1], "nightBegin", "nightEnd", "nightDuration",
                "enable", "skippable", "overrideDuration"
            )
        }
        val field = args[1]
        val value = args[2]
        if (TabCompleteUtil.isCommand(field, "nightBegin", "nightEnd", "nightDuration", "maxNightDuration")) {
            return if (TabCompleteUtil.isCommand(field, "nightBegin", "nightEnd")) TabCompleteUtil.completeInt(
                value,
                1,
                24000,
                localizer()
            ) else TabCompleteUtil.completeInt(value, 1, 86400, localizer())
        }
        if (TabCompleteUtil.isCommand(field, "enable", "skippable")) {
            return TabCompleteUtil.completeBoolean(value)
        }
        return if (TabCompleteUtil.isCommand(field, "durationMode")) {
            TabCompleteUtil.complete(value, NightSettings.NightDuration::class.java)
        } else emptyList()
    }

}