package me.racci.bloodnight.command.bloodnight

import de.eldoria.eldoutilities.simplecommands.EldoCommand
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil
import de.eldoria.eldoutilities.utils.ArgumentUtils
import de.eldoria.eldoutilities.utils.EnumUtil
import de.eldoria.eldoutilities.utils.Parser
import me.racci.bloodnight.command.util.CommandUtil
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.BossBarSettings
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.util.Permissions
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.World
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class ManageWorlds(plugin: Plugin, val configuration: Configuration) : EldoCommand(plugin) {

    private val bukkitAudiences: BukkitAudiences = BukkitAudiences.create(BloodNight.instance)

    // world field value page
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (denyConsole(sender)) {
            return true
        }
        if (denyAccess(sender, Permissions.Admin.MANAGE_WORLDS)) {
            return true
        }
        val player: Player = getPlayerFromSender(sender)
        val world1: World = player.world
        val world = ArgumentUtils.getOrDefault(
            args,
            0,
            { ArgumentUtils.getWorld(it) },
            world1
        )
        if (world == null) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidWorld"))
            return true
        }
        val worldSettings: WorldSettings = configuration.getWorldSettings(world)
        if (args.size < 2) {
            sendWorldPage(world, sender, 0)
            return true
        }

        // world field value
        if (argumentsInvalid(
                sender, args, 3,
                "[" + localizer().getMessage("syntax.worldName") + "] [<"
                        + localizer().getMessage("syntax.field") + "> <"
                        + localizer().getMessage("syntax.value") + ">]"
            )
        ) {
            return true
        }
        val field = args[1]
        val value = args[2]
        var optPage: OptionalInt = CommandUtil.findPage(
            configuration.worldSettings.values, 2
        ) { w -> w.worldName.equals(world.name, true) }
        if ("page".equals(field, ignoreCase = true)) {
            optPage = Parser.parseInt(value)
            if (optPage.isPresent) {
                sendWorldPage(world, sender, optPage.asInt)
            }
            return true
        }
        if ("bossBar".equals(field, ignoreCase = true)) {
            if (!TabCompleteUtil.isCommand(value, "state", "title", "colour", "toggleEffect")) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidField"))
            }
            if (argumentsInvalid(
                    sender, args, 4,
                    "[" + localizer().getMessage("syntax.worldName") + "] [" +
                            "bossBar <"
                            + localizer().getMessage("syntax.field") + "> <"
                            + localizer().getMessage("syntax.value") + ">]"
                )
            ) {
                return true
            }
            val bossBarValue = args[3]
            val bbs: BossBarSettings = worldSettings.bossBarSettings
            if ("state".equals(value, ignoreCase = true)) {
                val aBoolean = Parser.parseBoolean(bossBarValue)
                if (!aBoolean.isPresent) {
                    messageSender().sendError(sender, localizer().getMessage("error.invalidBoolean"))
                    return true
                }
                bbs.enabled = aBoolean.get()
            }
            if ("title".equals(value, ignoreCase = true)) {
                val title = java.lang.String.join(" ", *args.copyOfRange(3, args.size))
                bbs.title = title
            }
            if ("colour".equals(value, ignoreCase = true)) {
                val parse = EnumUtil.parse(bossBarValue, BarColor::class.java)
                if (parse == null) {
                    messageSender().sendError(sender, localizer().getMessage("error.invalidValue"))
                    return true
                }
                bbs.colour = parse
            }
            if ("toggleEffect".equals(value, ignoreCase = true)) {
                val parse = EnumUtil.parse(bossBarValue, BarFlag::class.java)
                if (parse == null) {
                    messageSender().sendError(sender, localizer().getMessage("error.invalidValue"))
                    return true
                }
                bbs.toggleEffect(parse)
            }
            sendWorldPage(world, sender, optPage.asInt)
            configuration.save()
            return true
        }
        if (TabCompleteUtil.isCommand(field, "state", "creeperBlockDamage", "manageCreeperAlways")) {
            val aBoolean = Parser.parseBoolean(value)
            if (!aBoolean.isPresent) {
                messageSender().sendError(sender, "invalid boolean")
                return true
            }
            if ("state".equals(field, ignoreCase = true)) {
                worldSettings.enabled = aBoolean.get()
            }
            if ("creeperBlockDamage".equals(field, ignoreCase = true)) {
                worldSettings.creeperBlockDamage = aBoolean.get()
            }
            if ("manageCreeperAlways".equals(field, ignoreCase = true)) {
                worldSettings.alwaysManageCreepers = aBoolean.get()
            }
            sendWorldPage(world, sender, optPage.asInt)
            configuration.save()
            return true
        }
        messageSender().sendError(sender, localizer().getMessage("error.invalidField"))
        return true
    }

    private fun sendWorldPage(world: World, sender: CommandSender, page: Int) {
        val component: TextComponent = CommandUtil.getPage(
            ArrayList(configuration.worldSettings.values),
            page,
            2, 7,
            { entry ->
                val cmd = "/bloodnight manageWorlds " + ArgumentUtils.escapeWorldName(entry.worldName) + " "
                val bbs: BossBarSettings = entry.bossBarSettings
                Component.text() // World State
                    .append(Component.text(entry.worldName, NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text("  "))
                    .append(
                        CommandUtil.getBooleanField(
                            entry.enabled,
                            cmd + "state {bool} ",
                            "",
                            localizer().getMessage("state.enabled"),
                            localizer().getMessage("state.disabled")
                        )
                    )
                    .append(Component.newline())
                    .append(
                        CommandUtil.getBooleanField(
                            entry.creeperBlockDamage,
                            cmd + "creeperBlockDamage {bool} ",
                            localizer().getMessage("field.creeperBlockDamage"),
                            localizer().getMessage("state.enabled"),
                            localizer().getMessage("state.disabled")
                        )
                    )
                    .append(Component.newline())
                    .append(
                        CommandUtil.getBooleanField(
                            entry.alwaysManageCreepers,
                            cmd + "manageCreeperAlways {bool} ",
                            localizer().getMessage("field.alwaysManageCreepers"),
                            localizer().getMessage("state.enabled"),
                            localizer().getMessage("state.disabled")
                        )
                    )
                    .append(Component.newline()).append(Component.text("  ")) // boss bar state
                    .append(Component.text(localizer().getMessage("field.bossBarSettings") + ": ", NamedTextColor.AQUA))
                    .append(
                        CommandUtil.getBooleanField(
                            bbs.enabled,
                            cmd + "bossBar state {bool} ",
                            "",
                            localizer().getMessage("state.enabled"),
                            localizer().getMessage("state.disabled")
                        )
                    )
                    .append(Component.newline()).append(Component.text("  ")) // title
                    .append(Component.text(localizer().getMessage("field.title") + ": ", NamedTextColor.AQUA))
                    .append(Component.text(bbs.title, NamedTextColor.GOLD))
                    .append(
                        Component.text(" [" + localizer().getMessage("action.change") + "] ", NamedTextColor.GREEN)
                            .clickEvent(
                                ClickEvent.suggestCommand(
                                    cmd + "bossBar title " + bbs.title.replace("§", "&")
                                )
                            )
                    )
                    .append(Component.newline()).append(Component.text("  ")) // Color
                    .append(Component.text(localizer().getMessage("field.color") + ": ", NamedTextColor.AQUA))
                    .append(Component.text(bbs.colour.toString(), toKyoriColor(bbs.colour)))
                    .append(
                        Component.text(" [" + localizer().getMessage("action.change") + "] ", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.suggestCommand(cmd + "bossBar colour "))
                    )
                    .append(Component.newline()).append(Component.text("  ")) // Effects
                    .append(Component.text(localizer().getMessage("field.effects") + ": ", NamedTextColor.AQUA))
                    .append(
                        CommandUtil.getToggleField(
                            bbs.isEffectEnabled(BarFlag.CREATE_FOG),
                            cmd + "bossBar toggleEffect CREATE_FOG",
                            localizer().getMessage("state.fog")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            bbs.isEffectEnabled(BarFlag.DARKEN_SKY),
                            cmd + "bossBar toggleEffect DARKEN_SKY",
                            localizer().getMessage("state.darkenSky")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            bbs.isEffectEnabled(BarFlag.PLAY_BOSS_MUSIC),
                            cmd + "bossBar toggleEffect PLAY_BOSS_MUSIC",
                            localizer().getMessage("state.music")
                        )
                    )
                    .build()
            },
            localizer().getMessage("manageWorlds.title"),
            "/bloodNight manageWorlds " + ArgumentUtils.escapeWorldName(world) + " page {page}"
        )
        bukkitAudiences.sender(sender).sendMessage(Identity.nil(), component)
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
        if (args.size == 2) {
            return TabCompleteUtil.complete(args[1], "bossBar", "state", "creeperBlockDamage", "manageCreeperAlways")
        }
        val field = args[1]
        if ("bossBar".equals(field, ignoreCase = true)) {
            if (args.size == 3) {
                return TabCompleteUtil.complete(args[2], "state", "title", "colour", "toggleEffect")
            }
            val bossField = args[2]
            val bossValue = args[3]
            if ("state".equals(bossField, ignoreCase = true)) {
                return TabCompleteUtil.completeBoolean(bossValue)
            }
            if ("title".equals(bossField, ignoreCase = true)) {
                return TabCompleteUtil.completeFreeInput(
                    ArgumentUtils.getRangeAsString(args, 3),
                    16,
                    localizer().getMessage("field.title"),
                    localizer()
                )
            }
            if ("colour".equals(bossField, ignoreCase = true)) {
                return TabCompleteUtil.complete(bossValue, BarColor::class.java)
            }
            return if ("toggleEffect".equals(bossField, ignoreCase = true)) {
                TabCompleteUtil.complete(bossValue, BarFlag::class.java)
            } else emptyList()
        }
        return if (TabCompleteUtil.isCommand(field, "state", "creeperBlockDamage", "manageCreeperAlways")) {
            TabCompleteUtil.completeBoolean(args[2])
        } else emptyList()
    }

    private fun toKyoriColor(color: BarColor): TextColor {
        return when (color) {
            BarColor.PINK -> TextColor.color(248, 24, 148)
            BarColor.BLUE -> NamedTextColor.BLUE
            BarColor.RED -> NamedTextColor.RED
            BarColor.GREEN -> NamedTextColor.GREEN
            BarColor.YELLOW -> NamedTextColor.YELLOW
            BarColor.PURPLE -> NamedTextColor.LIGHT_PURPLE
            BarColor.WHITE -> NamedTextColor.WHITE
            else -> throw IllegalStateException("Unexpected value: $color")
        }
    }

}