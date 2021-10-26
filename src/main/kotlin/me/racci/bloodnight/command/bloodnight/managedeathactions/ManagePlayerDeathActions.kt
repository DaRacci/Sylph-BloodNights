package me.racci.bloodnight.command.bloodnight.managedeathactions

import de.eldoria.eldoutilities.builder.ItemStackBuilder
import de.eldoria.eldoutilities.conversation.ConversationRequester
import de.eldoria.eldoutilities.core.EldoUtilities
import de.eldoria.eldoutilities.inventory.ActionConsumer
import de.eldoria.eldoutilities.inventory.ActionItem
import de.eldoria.eldoutilities.inventory.InventoryActions
import de.eldoria.eldoutilities.localization.Replacement
import de.eldoria.eldoutilities.messages.MessageChannel
import de.eldoria.eldoutilities.messages.MessageType
import de.eldoria.eldoutilities.simplecommands.EldoCommand
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil
import de.eldoria.eldoutilities.utils.ArgumentUtils
import de.eldoria.eldoutilities.utils.DataContainerUtil
import de.eldoria.eldoutilities.utils.Parser
import me.racci.bloodnight.command.util.CommandUtil
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.deathactions.PlayerDeathActions
import me.racci.bloodnight.config.worldsettings.deathactions.PotionEffectSettings
import me.racci.bloodnight.core.BloodNight
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import java.util.*

class ManagePlayerDeathActions(
    plugin: Plugin,
    val configuration: Configuration,
    private val conversationRequester: ConversationRequester,
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
        val playerDeathActions: PlayerDeathActions =
            configuration.getWorldSettings(world).deathActionSettings.playerDeathActions
        if (args.size < 2) {
            sendPlayerDeathActions(player, world, playerDeathActions)
            return true
        }
        if (argumentsInvalid(
                sender,
                args,
                2,
                "<monster|player> <\$syntax.worldName$> [<\$syntax.field$> <\$syntax.value$>]"
            )
        ) {
            return true
        }
        val field = args[1]
        val value: String = ArgumentUtils.getOrDefault(args, 2, "none")
        if ("effects".equals(field, ignoreCase = true)) {
            val inventory: Inventory = Bukkit.createInventory(
                player, 54,
                localizer().getMessage("manageDeathActions.inventory.respawnEffects.title")
            )
            val actions: InventoryActions = EldoUtilities.getInventoryActions().wrap(
                player, inventory
            ) {
                configuration.save()
                sendPlayerDeathActions(player, world, playerDeathActions)
            }
            val respawnEffects: MutableMap<PotionEffectType, PotionEffectSettings> =
                playerDeathActions.respawnEffects

            // this is always such a mess qwq
            val values: Array<PotionEffectType> = PotionEffectType.values()
            Arrays.sort(
                values, Comparator.comparing { obj: PotionEffectType -> obj.name }
            )
            for ((pos, potionType) in values.withIndex()) {
                val settings: PotionEffectSettings? = respawnEffects[potionType]
                val valueKey = NamespacedKey(BloodNight.instance, "value")
                val typeKey = NamespacedKey(BloodNight.instance, "type")
                actions.addAction(
                    ActionItem(
                        ItemStackBuilder
                            .of(Material.POTION)
                            .withDisplayName(potionType.name)
                            .withMetaValue(
                                PotionMeta::class.java
                            ) { m: PotionMeta -> m.color = potionType.color }
                            .withLore(java.lang.String.valueOf(settings?.duration ?: 0))
                            .withNBTData { c: PersistentDataContainer ->
                                c.set(typeKey, PersistentDataType.STRING, potionType.name)
                                c.set(
                                    valueKey,
                                    PersistentDataType.INTEGER,
                                    settings?.duration ?: 0
                                )
                            }
                            .build(),
                        pos,
                        ActionConsumer.getIntRange(valueKey, 0, 600)) label@
                    { it ->
                        val integer: Optional<Int> =
                            DataContainerUtil.get(it, valueKey, PersistentDataType.INTEGER)
                        if (!integer.isPresent) return@label
                        val optionalName: Optional<String> =
                            DataContainerUtil.get(it, typeKey, PersistentDataType.STRING)
                        optionalName.ifPresent {
                            val type: PotionEffectType = PotionEffectType.getByName(it)!!
                            if (integer.get() == 0) {
                                respawnEffects.remove(type)
                                return@ifPresent
                            }
                            respawnEffects.compute(
                                type
                            ) { _, _ ->
                                PotionEffectSettings(
                                    type,
                                    integer.get()
                                )
                            }
                        }
                    }
                )
            }
            player.openInventory(inventory)
            return true
        }
        if ("commands".equals(field, ignoreCase = true)) {
            val deathCommands: MutableList<String> = playerDeathActions.deathCommands
            val inventory: Inventory = Bukkit.createInventory(player, 54, "Manage Death Commands")
            val actions: InventoryActions = EldoUtilities.getInventoryActions().wrap(
                player,
                inventory
            ) { e: InventoryCloseEvent? -> configuration.save() }
            val pos = 0
            for (deathCommand in deathCommands) {
                actions.addAction(
                    ActionItem(ItemStackBuilder
                        .of(Material.PAPER)
                        .withDisplayName(deathCommand)
                        .withLore(
                            "§2" + localizer().localize("phrase.leftClickChange"),
                            "§c" + localizer().localize("phrase.rightClickRemove")
                        )
                        .build(),
                        pos, label@{ e: InventoryClickEvent ->
                            when (e.click) {
                                ClickType.LEFT, ClickType.SHIFT_LEFT -> {
                                    conversationRequester.requestInput(
                                        player,
                                        "phrase.commandPlayer",
                                        { true }, 0, { i: String ->
                                            deathCommands[pos] = i
                                            player.closeInventory()
                                            sendPlayerDeathActions(player, world, playerDeathActions)
                                        })
                                    player.closeInventory()
                                    return@label
                                }
                                ClickType.RIGHT, ClickType.SHIFT_RIGHT -> {
                                    deathCommands.removeAt(pos)
                                    player.closeInventory()
                                    sendPlayerDeathActions(player, world, playerDeathActions)
                                }
                                else -> {}
                            }
                        },
                        {}
                    )
                )
            }
            player.openInventory(inventory)
            return true
        }
        if ("addCommand".equals(field, ignoreCase = true)) {
            if ("none".equals(value, ignoreCase = true)) {
                messageSender().send(
                    MessageChannel.CHAT,
                    MessageType.ERROR,
                    sender,
                    localizer().getMessage("error.noCommand")
                )
                return true
            }
            val cmd = java.lang.String.join(" ", *args.copyOfRange(2, args.size))
            playerDeathActions.deathCommands
                .add(java.lang.String.join(" ", *args.copyOfRange(2, args.size)))
            configuration.save()
            sendPlayerDeathActions(player, world, playerDeathActions)
            return true
        }
        if (TabCompleteUtil.isCommand(field, "loseExp", "loseInv")) {
            val optionalInt: OptionalInt = Parser.parseInt(value)
            if (!optionalInt.isPresent) {
                messageSender().sendError(player, localizer().getMessage("error.invalidNumber"))
                return true
            }
            if (invalidRange(sender, optionalInt.asInt, 0, 100)) {
                return true
            }
            if ("loseExp".equals(field, ignoreCase = true)) {
                playerDeathActions.loseExpProbability = optionalInt.asInt
            }
            if ("loseInv".equals(field, ignoreCase = true)) {
                playerDeathActions.loseInvProbability = optionalInt.asInt
            }
            configuration.save()
            sendPlayerDeathActions(player, world, playerDeathActions)
            return true
        }
        if ("lightning".equals(field, ignoreCase = true)) {
            DeathActionUtil.buildLightningUI(
                playerDeathActions.lightningSettings, player, configuration, localizer()
            ) { sendPlayerDeathActions(player, world, playerDeathActions) }
            return true
        }
        if ("shockwave".equals(field, ignoreCase = true)) {
            DeathActionUtil.buildShockwaveUI(
                playerDeathActions.shockwaveSettings, player, configuration, localizer()
            ) { sendPlayerDeathActions(player, world, playerDeathActions) }
            return true
        }
        sendPlayerDeathActions(player, world, playerDeathActions)
        return true
    }

    private fun sendPlayerDeathActions(player: Player, world: World, playerDeathActions: PlayerDeathActions) {
        val cmd = "/bloodnight deathActions player " + ArgumentUtils.escapeWorldName(world.name) + " "
        val build: TextComponent = Component.text()
            .append(CommandUtil.getHeader(localizer().getMessage("manageDeathActions.player.title")))
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
            .append(Component.newline())
            .append(
                Component.text(
                    localizer().getMessage(
                        "field.deathCommands",
                        Replacement.create("COUNT", playerDeathActions.deathCommands.size)
                    ), NamedTextColor.AQUA
                )
            )
            .append(
                Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand(cmd + "commands"))
            )
            .append(
                Component.text(" [" + localizer().getMessage("action.add") + "]", NamedTextColor.DARK_GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "addCommand"))
            )
            .append(Component.newline())
            .append(Component.text(localizer().getMessage("field.respawnEffect"), NamedTextColor.AQUA))
            .append(
                Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand(cmd + "effects"))
            )
            .append(Component.newline())
            .append(Component.text(localizer().getMessage("field.loseInventory") + ": ", NamedTextColor.AQUA))
            .append(Component.text(playerDeathActions.loseInvProbability.toString() + "%", NamedTextColor.GOLD))
            .append(
                Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "loseInv "))
            )
            .append(Component.newline())
            .append(Component.text(localizer().getMessage("field.loseExperience") + ": ", NamedTextColor.AQUA))
            .append(Component.text(playerDeathActions.loseExpProbability.toString() + "%", NamedTextColor.GOLD))
            .append(
                Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "loseExp "))
            )
            .build()
        bukkitAudiences.player(player).sendMessage(build)
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
                args[1],
                "effects",
                "commands",
                "addCommand",
                "loseExp",
                "loseInv",
                "lightning",
                "shockwave"
            )
        }
        if (args.size >= 3) {
            if (TabCompleteUtil.isCommand(args[1], "addCommand")) {
                return TabCompleteUtil.completeFreeInput(
                    java.lang.String.join(
                        " ",
                        *args.copyOfRange(2, args.size)
                    ), 140, localizer().getMessage("syntax.commandPlayer"), localizer()
                )
            }
            return if (TabCompleteUtil.isCommand(args[1], "loseExp", "loseInv")) {
                TabCompleteUtil.completeInt(args[2], 0, 100, localizer())
            } else emptyList()
        }
        return emptyList()
    }
}