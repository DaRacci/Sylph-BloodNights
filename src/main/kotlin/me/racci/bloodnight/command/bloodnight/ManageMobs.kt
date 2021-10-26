package me.racci.bloodnight.command.bloodnight

import de.eldoria.eldoutilities.localization.Replacement
import de.eldoria.eldoutilities.simplecommands.EldoCommand
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil
import de.eldoria.eldoutilities.utils.ArgumentUtils
import de.eldoria.eldoutilities.utils.ArrayUtil
import de.eldoria.eldoutilities.utils.EnumUtil
import de.eldoria.eldoutilities.utils.Parser
import me.racci.bloodnight.command.InventoryListener
import me.racci.bloodnight.command.util.CommandUtil
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.Drop
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.VanillaDropMode
import me.racci.bloodnight.config.worldsettings.mobsettings.VanillaMobSettings
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.util.Permissions
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

class ManageMobs(plugin: Plugin, configuration: Configuration, inventoryListener: InventoryListener) :
    EldoCommand(plugin) {

    private val bukkitAudiences = BukkitAudiences.create(BloodNight.instance)
    private val configuration: Configuration
    private val inventoryListener: InventoryListener

    // world field value
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (denyConsole(sender)) {
            return true
        }
        if (denyAccess(sender, Permissions.Admin.MANAGE_MOBS)) {
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
        val mobSettings: MobSettings = worldSettings.mobSettings
        if (args.size < 2) {
            sendInfo(sender, worldSettings)
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
        val field = args[1]
        val value = args[2]
        if (ArrayUtil.arrayContains(arrayOf("spawnPercentage", "dropAmount", "vanillaDropAmount"), field)) {
            val optionalInt: OptionalInt = Parser.parseInt(value)
            if (!optionalInt.isPresent) {
                messageSender().sendError(player, localizer().getMessage("error.invalidNumber"))
                return true
            }
            if (invalidRange(sender, optionalInt.asInt, 1, 100)) {
                return true
            }
            if ("spawnPercentage".equals(field, ignoreCase = true)) {
                mobSettings.spawnPercentage = (optionalInt.asInt)
            }
            if ("dropAmount".equals(field, ignoreCase = true)) {
                mobSettings.dropAmount = (optionalInt.asInt)
            }
            if ("vanillaDropAmount".equals(field, ignoreCase = true)) {
                mobSettings.vanillaMobSettings.extraDrops = (optionalInt.asInt)
            }
            configuration.save()
            sendInfo(sender, worldSettings)
            return true
        }
        if (ArrayUtil.arrayContains(
                arrayOf(
                    "monsterDamage", "vanillaMonsterDamage", "vanillaMonsterHealth",
                    "monsterHealth", "experience", "vanillaDropsMulti"
                ), field
            )
        ) {
            val optionalDouble: OptionalDouble = Parser.parseDouble(value)
            if (!optionalDouble.isPresent) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidNumber"))
                return true
            }
            if (invalidRange(sender, optionalDouble.asDouble, 1.0, 200.0)) {
                return true
            }
            if ("monsterDamage".equals(field, ignoreCase = true)) {
                mobSettings.damageMultiplier = (optionalDouble.asDouble)
            }
            if ("monsterHealth".equals(field, ignoreCase = true)) {
                mobSettings.healthModifier = (optionalDouble.asDouble)
            }
            if ("vanillaMonsterDamage".equals(field, ignoreCase = true)) {
                mobSettings.vanillaMobSettings.damageMultiplier = (optionalDouble.asDouble)
            }
            if ("vanillaMonsterHealth".equals(field, ignoreCase = true)) {
                mobSettings.vanillaMobSettings.healthMultiplier = (optionalDouble.asDouble)
            }
            if ("experience".equals(field, ignoreCase = true)) {
                mobSettings.experienceMultiplier = (optionalDouble.asDouble)
            }
            if ("vanillaDropsMulti".equals(field, ignoreCase = true)) {
                mobSettings.vanillaMobSettings.dropMultiplier = (optionalDouble.asDouble)
            }
            configuration.save()
            sendInfo(sender, worldSettings)
            return true
        }
        if (ArrayUtil.arrayContains(arrayOf("forcePhantoms", "displayName", "naturalDrops"), field)) {
            val optionalBoolean = Parser.parseBoolean(value)
            if (!optionalBoolean.isPresent) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidBoolean"))
                return true
            }
            if ("forcePhantoms".equals(field, ignoreCase = true)) {
                mobSettings.forcePhantoms = (optionalBoolean.get())
            }
            if ("displayName".equals(field, ignoreCase = true)) {
                mobSettings.displayMobNames = (optionalBoolean.get())
            }
            if ("naturalDrops".equals(field, ignoreCase = true)) {
                mobSettings.naturalDrops = (optionalBoolean.get())
            }
            sendInfo(sender, worldSettings)
            configuration.save()
            return true
        }
        if ("defaultDrops".equals(field, ignoreCase = true)) {
            if ("changeContent".equals(value, ignoreCase = true)) {
                val inv: Inventory = Bukkit.createInventory(player, 54, "Drops")
                val stacks: List<ItemStack> =
                    mobSettings.defaultDrops.map(Drop::weightedItem)
                inv.setContents(stacks.toTypedArray())
                player.openInventory(inv)
                inventoryListener.registerModification(player, object : InventoryListener.InventoryActionHandler {
                    override fun onInventoryClose(event: InventoryCloseEvent) {
                        val collect = event.inventory.contents
                            .filterNotNull()
                            .map(Drop::fromItemStack)
                        mobSettings.defaultDrops = (collect)
                        sendInfo(sender, worldSettings)
                    }

                    override fun onInventoryClick(event: InventoryClickEvent) {}
                })
                return true
            }
            if ("changeWeight".equals(value, ignoreCase = true)) {
                val stacks: List<ItemStack> =
                    mobSettings.defaultDrops.map(Drop::weightedItem)
                val inv: Inventory = Bukkit.createInventory(player, 54, "Weight")
                inv.setContents(stacks.toTypedArray())
                player.openInventory(inv)
                inventoryListener.registerModification(player, object : InventoryListener.InventoryActionHandler {
                    override fun onInventoryClose(event: InventoryCloseEvent) {
                        val collect = event.inventory.contents
                            .filterNotNull()
                            .map(Drop::fromItemStack)
                        mobSettings.defaultDrops = (collect)
                        sendInfo(sender, worldSettings)
                    }

                    override fun onInventoryClick(event: InventoryClickEvent) {
                        if (event.inventory.type != InventoryType.CHEST) return
                        if (event.view.topInventory != event.clickedInventory) {
                            return
                        }
                        when (event.click) {
                            ClickType.LEFT -> Drop.changeWeight(event.currentItem, 1)
                            ClickType.SHIFT_LEFT -> Drop.changeWeight(event.currentItem, 10)
                            ClickType.RIGHT -> Drop.changeWeight(event.currentItem, -1)
                            ClickType.SHIFT_RIGHT -> Drop.changeWeight(event.currentItem, -10)
                            else -> {}
                        }
                        event.isCancelled = true
                    }
                })
                return true
            }
            if ("clear".equals(value, ignoreCase = true)) {
                mobSettings.defaultDrops = (ArrayList())
                configuration.save()
                sendInfo(sender, worldSettings)
                return true
            }
            messageSender().sendError(sender, localizer().getMessage("error.invalidValue"))
            return true
        }
        if ("vanillaDropMode".equals(field, ignoreCase = true)) {
            val parse = EnumUtil.parse(value, VanillaDropMode::class.java)
            if (parse == null) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidValue"))
                return true
            }
            mobSettings.vanillaMobSettings.vanillaDropMode = (parse)
            sendInfo(sender, worldSettings)
            configuration.save()
            return true
        }
        messageSender().sendError(sender, localizer().getMessage("error.invalidField"))
        return true
    }

    private fun sendInfo(sender: CommandSender, worldSettings: WorldSettings) {
        val mSet: MobSettings = worldSettings.mobSettings
        val vms: VanillaMobSettings = worldSettings.mobSettings.vanillaMobSettings
        val cmd = "/bloodnight manageMobs " + ArgumentUtils.escapeWorldName(worldSettings.worldName) + " "
        val message: TextComponent.Builder = Component.text()
            .append(
                CommandUtil.getHeader(
                    localizer().getMessage(
                        "manageMobs.title",
                        Replacement.create("WORLD", worldSettings.worldName).addFormatting('6')
                    )
                )
            )
            .append(Component.newline()) // spawn percentage
            .append(Component.text(localizer().getMessage("field.spawnPercentage") + ": ", NamedTextColor.AQUA))
            .append(Component.text(mSet.spawnPercentage.toString() + "% ", NamedTextColor.GOLD))
            .append(
                Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "spawnPercentage "))
            ) // Display mobNames
            .append(Component.newline())
            .append(
                CommandUtil.getBooleanField(
                    mSet.displayMobNames,
                    cmd + "displayName {bool}",
                    localizer().getMessage("field.showMobNames"),
                    localizer().getMessage("state.enabled"),
                    localizer().getMessage("state.disabled")
                )
            )
            .append(Component.newline()) // Monster damage
            .append(Component.text(localizer().getMessage("field.monsterDamage") + ": ", NamedTextColor.AQUA))
            .append(Component.text(mSet.damageMultiplier.toString() + "x ", NamedTextColor.GOLD))
            .append(
                Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "monsterDamage "))
            )
            .append(Component.newline()) // Player damage
            .append(Component.text(localizer().getMessage("field.monsterHealth") + ": ", NamedTextColor.AQUA))
            .append(Component.text(mSet.healthModifier.toString() + "x ", NamedTextColor.GOLD))
            .append(
                Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "monsterHealth "))
            )
            .append(Component.newline()) // experience multiply
            .append(Component.text(localizer().getMessage("field.experienceMultiplier") + ": ", NamedTextColor.AQUA))
            .append(Component.text(mSet.experienceMultiplier.toString() + "x ", NamedTextColor.GOLD))
            .append(
                Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "experience "))
            )
            .append(Component.newline()) // force phantoms
            .append(
                CommandUtil.getBooleanField(
                    mSet.forcePhantoms,
                    cmd + "forcePhantoms {bool}",
                    localizer().getMessage("field.forcePhantoms"),
                    localizer().getMessage("state.enabled"),
                    localizer().getMessage("state.disabled")
                )
            )
            .append(Component.newline()) // natural drops
            .append(
                CommandUtil.getBooleanField(
                    mSet.naturalDrops,
                    cmd + "naturalDrops {bool}",
                    localizer().getMessage("field.naturalDrops"),
                    localizer().getMessage("state.allow"),
                    localizer().getMessage("state.deny")
                )
            )
            .append(Component.newline()) // default drops
            .append(Component.text(localizer().getMessage("field.defaultDrops") + ": ", NamedTextColor.AQUA))
            .append(
                Component.text(
                    mSet.defaultDrops.size.toString() + " " + localizer().getMessage("field.drops"),
                    NamedTextColor.GOLD
                )
            )
            .append(
                Component.text(" [" + localizer().getMessage("action.content") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops changeContent"))
            )
            .append(
                Component.text(" [" + localizer().getMessage("action.weight") + "]", NamedTextColor.GOLD)
                    .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops changeWeight"))
            )
            .append(
                Component.text(" [" + localizer().getMessage("action.clear") + "]", NamedTextColor.RED)
                    .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops clear"))
            )
            .append(Component.newline()) // default drop amount
            .append(Component.text(localizer().getMessage("field.dropAmount") + ": ", NamedTextColor.AQUA))
            .append(Component.text(mSet.dropAmount.toString() + "x ", NamedTextColor.GOLD))
            .append(
                Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "dropAmount "))
            )
            .append(Component.newline()) // Vanilla Mobs submenu
            .append(Component.text(localizer().getMessage("field.vanillaMobs") + ": ", NamedTextColor.AQUA))
            .append(Component.newline().append(Component.text("  "))) // Monster damage
            .append(Component.text(localizer().getMessage("field.monsterDamage") + ": ", NamedTextColor.AQUA))
            .append(Component.text(vms.damageMultiplier.toString() + "x ", NamedTextColor.GOLD))
            .append(
                Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaMonsterDamage "))
            )
            .append(Component.newline().append(Component.text("  "))) // Player damage
            .append(Component.text(localizer().getMessage("field.monsterHealth") + ": ", NamedTextColor.AQUA))
            .append(Component.text(vms.healthMultiplier.toString() + "x ", NamedTextColor.GOLD))
            .append(
                Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaMonsterHealth "))
            )
            .append(Component.newline().append(Component.text("  "))) // drops
            .append(Component.text(localizer().getMessage("field.dropsMultiplier") + ": ", NamedTextColor.AQUA))
            .append(Component.text(vms.dropMultiplier.toString() + "x ", NamedTextColor.GOLD))
            .append(
                Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaDropsMulti "))
            )
            .append(Component.newline().append(Component.text("  "))) // Drop Mode
            .append(Component.text(localizer().getMessage("field.dropMode") + ": ", NamedTextColor.AQUA))
            .append(
                CommandUtil.getToggleField(
                    vms.vanillaDropMode === VanillaDropMode.VANILLA,
                    cmd + "vanillaDropMode VANILLA",
                    localizer().getMessage("state.vanilla")
                )
            )
            .append(Component.space())
            .append(
                CommandUtil.getToggleField(
                    vms.vanillaDropMode === VanillaDropMode.COMBINE,
                    cmd + "vanillaDropMode COMBINE",
                    localizer().getMessage("state.combine")
                )
            )
            .append(Component.space())
            .append(
                CommandUtil.getToggleField(
                    vms.vanillaDropMode === VanillaDropMode.CUSTOM,
                    cmd + "vanillaDropMode CUSTOM",
                    localizer().getMessage("state.custom")
                )
            )
        if (vms.vanillaDropMode !== VanillaDropMode.VANILLA) {
            message.append(Component.newline().append(Component.text("  ")))
                .append(Component.text(localizer().getMessage("field.customDropAmount") + ": ", NamedTextColor.AQUA))
                .append(Component.text(vms.extraDrops.toString() + "x ", NamedTextColor.GOLD))
                .append(
                    Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaDropAmount "))
                )
        }
        bukkitAudiences.sender(sender).sendMessage(Identity.nil(), message)
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
                args[1], "spawnPercentage", "dropAmount", "monsterDamage",
                "vanillaMonsterDamage", "vanillaMonsterHealth", "experience", "drops",
                "forcePhantoms", "displayName"
            )
        }
        val field = args[1]
        val value = args[2]
        if (TabCompleteUtil.isCommand(
                field,
                "spawnPercentage",
                "dropAmount",
                "vanillaDropAmount",
                "vanillaDropAmount"
            )
        ) {
            return TabCompleteUtil.completeInt(value, 1, 100, localizer())
        }
        if (TabCompleteUtil.isCommand(
                field, "monsterDamage", "vanillaMonsterDamage", "vanillaMonsterHealth", "monsterHealth",
                "experience", "drops"
            )
        ) {
            return TabCompleteUtil.completeDouble(value, 1.0, 200.0, localizer())
        }
        if (TabCompleteUtil.isCommand(field, "forcePhantoms", "displayName", "naturalDrops")) {
            return TabCompleteUtil.completeBoolean(value)
        }
        if (TabCompleteUtil.isCommand(field, "defaultDrops")) {
            return TabCompleteUtil.complete(value, "changeContent", "changeWeight", "clear")
        }
        return if (TabCompleteUtil.isCommand(field, "vanillaDropMode")) {
            TabCompleteUtil.complete(value, VanillaDropMode::class.java)
        } else emptyList()
    }

    init {
        this.configuration = configuration
        this.inventoryListener = inventoryListener
    }
}