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
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSetting
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.MobValueModifier
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.core.mobfactory.MobFactory
import me.racci.bloodnight.core.mobfactory.SpecialMobRegistry
import me.racci.bloodnight.util.Permissions
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.World
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

class ManageMob(plugin: Plugin, val configuration: Configuration, private val inventoryListener: InventoryListener) :
    EldoCommand(plugin) {


    private val bukkitAudiences: BukkitAudiences = BukkitAudiences.create(BloodNight.instance)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (denyConsole(sender)) {
            return true
        }
        if (denyAccess(sender, Permissions.Admin.MANAGE_MOB)) {
            return true
        }
        val player: Player = sender as Player
        val world: World = ArgumentUtils.getOrDefault(
            args,
            1,
            { ArgumentUtils.getWorld(it) },
            player.world
        )
        val worldSettings: WorldSettings = configuration.getWorldSettings(world)
        if (!worldSettings.enabled) {
            messageSender().sendLocalizedError(
                player, "error.worldNotEnabled",
                Replacement.create("WORLD", world.name)
            )
            return true
        }

        // group world mob value [page]
        if (argumentsInvalid(sender, args, 1, "<\$syntax.mobGroup$>")) {
            return true
        }
        val mobGroupName = args[0]
        val mobGroup = worldSettings.mobSettings.mobTypes.getGroup(mobGroupName)
        if (mobGroup == null) {
            messageSender().sendLocalizedError(sender, "error.invalidMobGroup")
            return true
        }

        if (args.size < 3) {
            sendMobListPage(world, sender, mobGroup, 0)
            return true
        }

        // group world page %page%
        if (argumentsInvalid(
                sender, args, 4,
                "<\$syntax.mobGroup$> [<\$syntax.worldName$> <\$syntax.mob$> <\$syntax.field$> <\$syntax.value$>]"
            )
        ) {
            return true
        }
        val mobString = args[2]
        val field = args[3]
        if ("page".equals(mobString, ignoreCase = true)) {
            val optPage: OptionalInt = Parser.parseInt(field)
            if (optPage.isPresent) {
                sendMobListPage(world, sender, mobGroup, optPage.asInt)
            }
            return true
        }

        // group world mob value
        if (argumentsInvalid(
                sender, args, 4,
                "<\$syntax.mobGroup>$ [<\$syntax.worldName$> <\$syntax.mob$> <\$syntax.field$> <\$syntax.value$>]"
            )
        ) {
            return true
        }
        val value = args[4]
        val mobByName = worldSettings.mobSettings.getMobByName(mobString)
        if (mobByName == null) {
            messageSender().sendLocalizedError(sender, "error.invalidMob")
            return true
        }
        val mob: MobSetting = mobByName
        val optPage: OptionalInt = CommandUtil.findPage(mobGroup.value, 2) { m ->
            m.mobName.equals(mobByName.mobName, true)
        }
        if (ArrayUtil.arrayContains(arrayOf("state", "overrideDefault"), field)) {
            val aBoolean = Parser.parseBoolean(value)
            if (!aBoolean.isPresent) {
                messageSender().sendLocalizedError(sender, "error.invalidBoolean")
                return true
            }
            if ("state".equals(field, ignoreCase = true)) {
                mob.active = aBoolean.get()
            }
            if ("overrideDefault".equals(field, ignoreCase = true)) {
                mob.overrideDefaultDrops = aBoolean.get()
            }
            optPage.ifPresent { i: Int -> sendMobListPage(world, sender, mobGroup, i) }
            configuration.save()
            return true
        }
        if (ArrayUtil.arrayContains(arrayOf("displayName"), field)) {
            val s = java.lang.String.join(" ", *args.copyOfRange(4, args.size))
            if ("displayName".equals(field, ignoreCase = true)) {
                mob.displayName = s
            }
            optPage.ifPresent { i: Int -> sendMobListPage(world, sender, mobGroup, i) }
            configuration.save()
            return true
        }
        if (ArrayUtil.arrayContains(arrayOf("dropAmount"), field)) {
            val num: OptionalInt = Parser.parseInt(value)
            if (!num.isPresent) {
                messageSender().sendLocalizedError(sender, "error.invalidNumber")
                return true
            }
            if ("dropAmount".equals(field, ignoreCase = true)) {
                if (invalidRange(sender, num.asInt, 0, 100)) {
                    return true
                }
                mob.dropAmount = num.asInt
            }
            optPage.ifPresent { i: Int -> sendMobListPage(world, sender, mobGroup, i) }
            configuration.save()
            return true
        }
        if (ArrayUtil.arrayContains(arrayOf("health", "damage"), field)) {
            val num: OptionalDouble = Parser.parseDouble(value)
            if (!num.isPresent) {
                messageSender().sendLocalizedError(sender, "error.invalidNumber")
                return true
            }
            if ("health".equals(field, ignoreCase = true)) {
                if (invalidRange(sender, num.asDouble, 1.0, 500.0)) {
                    return true
                }
                mob.health = num.asDouble
            }
            if ("damage".equals(field, ignoreCase = true)) {
                if (invalidRange(sender, num.asDouble, 1.0, 500.0)) {
                    return true
                }
                mob.damage = num.asDouble
            }
            optPage.ifPresent { i: Int -> sendMobListPage(world, sender, mobGroup, i) }
            configuration.save()
            return true
        }
        if (ArrayUtil.arrayContains(arrayOf("healthModifier", "damageModifier"), field)) {
            val `val` = EnumUtil.parse(value, MobValueModifier::class.java)
            if (`val` == null) {
                messageSender().sendLocalizedError(sender, "error.invalidValue")
                return true
            }
            if ("healthModifier".equals(field, ignoreCase = true)) {
                mob.healthModifier = `val`
            }
            if ("damageModifier".equals(field, ignoreCase = true)) {
                mob.damageModifier = `val`
            }
            optPage.ifPresent { i: Int -> sendMobListPage(world, sender, mobGroup, i) }
            configuration.save()
            return true
        }
        if ("drops".equals(field, ignoreCase = true)) {
            if (!ArrayUtil.arrayContains(arrayOf("changeContent", "changeWeight", "clear"), value)) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidValue"))
            }
            if ("changeContent".equals(value, ignoreCase = true)) {
                val inv: Inventory = Bukkit.createInventory(player, 54, localizer().getMessage("drops.dropsTitle"))
                inv.setContents(mob.drops.map(Drop::weightedItem).toTypedArray())
                player.openInventory(inv)
                inventoryListener.registerModification(player, object : InventoryListener.InventoryActionHandler {
                    override fun onInventoryClose(event: InventoryCloseEvent) {
                        val collect: List<Drop> = event.inventory.contents
                            .filterNotNull()
                            .map(Drop::fromItemStack)
                        mob.drops = collect
                        optPage.ifPresent { sendMobListPage(world, sender, mobGroup, it) }
                    }

                    override fun onInventoryClick(event: InventoryClickEvent) {}
                })
            }
            if ("changeWeight".equals(value, ignoreCase = true)) {
                val stacks: List<ItemStack> =
                    mob.drops.map(Drop::weightedItem)
                val inv: Inventory = Bukkit.createInventory(player, 54, localizer().getMessage("drops.weightTitle"))
                inv.setContents(stacks.toTypedArray())
                player.openInventory(inv)
                inventoryListener.registerModification(player, object : InventoryListener.InventoryActionHandler {
                    override fun onInventoryClose(event: InventoryCloseEvent) {
                        val collect: List<Drop> = event.inventory.contents
                            .filterNotNull()
                            .map(Drop::fromItemStack)
                        mob.drops = collect
                        optPage.ifPresent { sendMobListPage(world, sender, mobGroup, it) }
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
            }
            if ("clear".equals(value, true)) {
                mob.drops = ArrayList()
                optPage.ifPresent { sendMobListPage(world, sender, mobGroup, it) }
                configuration.save()
            }
            return true
        }
        messageSender().sendError(sender, localizer().getMessage("error.invalidField"))
        return true
    }

    private fun sendMobListPage(
        world: World,
        sender: CommandSender,
        mobGroup: Map.Entry<String, Set<MobSetting>>,
        page: Int
    ) {
        val mobSettings: MobSettings = configuration.getWorldSettings(world).mobSettings
        val component: TextComponent = CommandUtil.getPage(
            ArrayList(mobGroup.value),
            page,
            2, 7,
            { entry ->
                val cmd =
                    "/bloodnight manageMob " + mobGroup.key + " " + ArgumentUtils.escapeWorldName(world.name) + " " + entry.mobName + " "
                val builder: TextComponent.Builder = Component.text() // Mob name
                    .append(Component.text(entry.mobName, NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.space()) // Mob state
                    .append(
                        CommandUtil.getBooleanField(
                            entry.active,
                            cmd + "state {bool}",
                            "",
                            localizer().getMessage("state.enabled"),
                            localizer().getMessage("state.disabled")
                        )
                    ) // Display name
                    .append(Component.newline()).append(Component.text("  "))
                    .append(Component.text(localizer().getMessage("field.displayName") + ": ", NamedTextColor.AQUA))
                    .append(Component.text(entry.displayName ?: "", NamedTextColor.GOLD))
                    .append(
                        Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                            .clickEvent(
                                ClickEvent.suggestCommand(
                                    cmd + "displayName " + entry.displayName?.replace("§", "&")
                                )
                            )
                    )
                    .append(Component.newline()).append(Component.text("  ")) // Drop amount
                    .append(Component.text(localizer().getMessage("field.dropAmount") + ": ", NamedTextColor.AQUA))
                    .append(
                        Component.text(
                            if (entry.dropAmount <= 0) localizer().getMessage("action.default") else entry.dropAmount
                                .toString() + "x", NamedTextColor.GOLD
                        )
                    )
                    .append(
                        Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.suggestCommand(cmd + "dropAmount "))
                    )
                    .append(Component.newline()).append(Component.text("  ")) // drops
                    .append(Component.text(localizer().getMessage("field.drops") + ": ", NamedTextColor.AQUA))
                    .append(
                        Component.text(
                            entry.drops.size.toString() + " " + localizer().getMessage("field.drops"),
                            NamedTextColor.GOLD
                        )
                    )
                    .append(
                        Component.text(" [" + localizer().getMessage("action.content") + "]", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.runCommand(cmd + "drops changeContent"))
                    )
                    .append(
                        Component.text(" [" + localizer().getMessage("action.weight") + "]", NamedTextColor.GOLD)
                            .clickEvent(ClickEvent.runCommand(cmd + "drops changeWeight"))
                    )
                    .append(
                        Component.text(" [" + localizer().getMessage("action.clear") + "]", NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand(cmd + "drops clear"))
                    ) // override drops
                    .append(Component.newline()).append(Component.text("  "))
                    .append(
                        CommandUtil.getBooleanField(
                            entry.overrideDefaultDrops,
                            cmd + "overrideDefault {bool} " + page,
                            localizer().getMessage("field.overrideDefaultDrops"),
                            localizer().getMessage("state.override"),
                            localizer().getMessage("state.combine")
                        )
                    )
                    .append(Component.newline()).append(Component.text("  ")) // health modifier
                    .append(Component.text("Health Modifier: ", NamedTextColor.AQUA))
                    .append(
                        CommandUtil.getToggleField(
                            entry.healthModifier === MobValueModifier.DEFAULT,
                            cmd + "healthModifier DEFAULT",
                            localizer().getMessage("action.default")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            entry.healthModifier === MobValueModifier.MULTIPLY,
                            cmd + "healthModifier MULTIPLY",
                            localizer().getMessage("action.multiply")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            entry.healthModifier === MobValueModifier.VALUE,
                            cmd + "healthModifier VALUE",
                            localizer().getMessage("action.value")
                        )
                    )
                    .append(Component.newline()).append(Component.text("  "))
                    .append(Component.text(localizer().getMessage("field.health") + ": ", NamedTextColor.AQUA))
                when (entry.healthModifier) {
                    MobValueModifier.DEFAULT -> builder.append(
                        Component.text(
                            localizer().getMessage("action.default") + " (" + mobSettings.healthModifier + "x)",
                            NamedTextColor.GOLD
                        )
                    )
                    MobValueModifier.MULTIPLY -> builder.append(
                        Component.text(
                            entry.health.toString() + "x",
                            NamedTextColor.GOLD
                        )
                    )
                    MobValueModifier.VALUE -> builder.append(
                        Component.text(
                            entry.health.toString() + " " + localizer().getMessage("field.health"),
                            NamedTextColor.GOLD
                        )
                    )
                }
                builder.append(
                    Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "health "))
                )
                // damage modifier
                builder.append(Component.newline()).append(Component.text("  "))
                    .append(Component.text("Damage Modifier: ", NamedTextColor.AQUA))
                    .append(
                        CommandUtil.getToggleField(
                            entry.damageModifier === MobValueModifier.DEFAULT,
                            cmd + "damageModifier DEFAULT",
                            localizer().getMessage("action.default")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            entry.damageModifier === MobValueModifier.MULTIPLY,
                            cmd + "damageModifier MULTIPLY",
                            localizer().getMessage("action.multiply")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            entry.damageModifier === MobValueModifier.VALUE,
                            cmd + "damageModifier VALUE",
                            localizer().getMessage("action.value")
                        )
                    )
                    .append(Component.newline()).append(Component.text("  "))
                    .append(Component.text(localizer().getMessage("field.damage") + ": ", NamedTextColor.AQUA))
                when (entry.damageModifier) {
                    MobValueModifier.DEFAULT -> builder.append(
                        Component.text(
                            localizer().getMessage("action.default") + " (" + mobSettings.healthModifier + "x)",
                            NamedTextColor.GOLD
                        )
                    )
                    MobValueModifier.MULTIPLY -> builder.append(
                        Component.text(
                            entry.damage.toString() + "x",
                            NamedTextColor.GOLD
                        )
                    )
                    MobValueModifier.VALUE -> builder.append(
                        Component.text(
                            entry.damage.toString() + " " + localizer().getMessage("field.damage"),
                            NamedTextColor.GOLD
                        )
                    )
                }
                builder.append(
                    Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "damage "))
                )
                builder.build()
            },
            localizer().getMessage(
                "manageMob.title",
                Replacement.create("TYPE", mobGroup.key),
                Replacement.create("WORLD", world.name)
            ),
            "/bloodNight manageMob " + mobGroup.key + " " + ArgumentUtils.escapeWorldName(world) + " page {page}"
        )
        bukkitAudiences.sender(sender).sendMessage(Identity.nil(), component)
    }

    //group world mob field value
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        // mobGroup
        if (args.size == 1) {
            return TabCompleteUtil.complete(
                args[0],
                SpecialMobRegistry.mobGroups.keys
            ) { obj: Class<*> -> obj.simpleName }
        }
        // world
        if (args.size == 2) {
            return TabCompleteUtil.complete(args[1], configuration.worldSettings.keys)
        }
        // mob
        if (args.size == 3) {
            val mobGroup = SpecialMobRegistry.getMobGroup(args[0])
            return if (mobGroup == null) {
                listOf(localizer().getMessage("error.invalidMobGroup"))
            } else TabCompleteUtil.complete(args[2], mobGroup.factories, MobFactory::mobName)
        }
        // field
        val scmd = args[3]
        if (args.size == 4) {
            return TabCompleteUtil.complete(
                scmd, "state", "overrideDefault", "displayName",
                "dropAmount", "health", "damage", "healthModifier", "damageModifier", "drops"
            )
        }
        val `val` = args[4]
        if (args.size == 5) {
            if (TabCompleteUtil.isCommand(scmd, "state", "overrideDefault")) {
                return TabCompleteUtil.completeBoolean(`val`)
            }
            if (TabCompleteUtil.isCommand(scmd, "dropAmount")) {
                return TabCompleteUtil.completeInt(`val`, 1, 100, localizer())
            }
            if (TabCompleteUtil.isCommand(scmd, "health", "damage")) {
                return TabCompleteUtil.completeInt(`val`, 1, 500, localizer())
            }
            if (TabCompleteUtil.isCommand(scmd, "healthModifier", "damageModifier")) {
                return TabCompleteUtil.complete(`val`, MobValueModifier::class.java)
            }
            if (TabCompleteUtil.isCommand(scmd, "drops")) {
                return TabCompleteUtil.complete(`val`, "changeContent", "changeWeight", "clear")
            }
        }
        return if (TabCompleteUtil.isCommand(scmd, "displayName")) {
            TabCompleteUtil.completeFreeInput(
                ArgumentUtils.getRangeAsString(args, 4),
                16, localizer().getMessage("field.displayName"), localizer()
            )
        } else emptyList()
    }

}