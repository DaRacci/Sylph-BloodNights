package me.racci.bloodnight.command.bloodnight

import de.eldoria.eldoutilities.container.Pair
import de.eldoria.eldoutilities.localization.ILocalizer
import de.eldoria.eldoutilities.simplecommands.EldoCommand
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil
import de.eldoria.eldoutilities.utils.ArgumentUtils
import de.eldoria.eldoutilities.utils.EMath
import de.eldoria.eldoutilities.utils.EnumUtil
import de.eldoria.eldoutilities.utils.Parser
import me.racci.bloodnight.command.InventoryListener
import me.racci.bloodnight.command.util.CommandUtil
import me.racci.bloodnight.config.Configuration
import me.racci.bloodnight.config.worldsettings.NightSelection
import me.racci.bloodnight.config.worldsettings.WorldSettings
import me.racci.bloodnight.core.BloodNight
import me.racci.bloodnight.util.Permissions
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
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
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

class ManageNightSelection(
    plugin: Plugin,
    val configuration: Configuration,
    private val inventoryListener: InventoryListener
) : EldoCommand(plugin) {

    private val bukkitAudiences: BukkitAudiences = BukkitAudiences.create(BloodNight.instance)

    // world field value
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (denyConsole(sender)) {
            return true
        }
        if (denyAccess(sender, Permissions.Admin.MANAGE_WORLDS)) {
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
        val value: String = ArgumentUtils.getOptionalParameter(args, 2, "none") { s: String? -> s }
        val optPage: OptionalInt = CommandUtil.findPage(
            configuration.worldSettings.values, 3
        ) { w -> w.worldName.equals(world.name, true) }
        if ("page".equals(field, ignoreCase = true)) {
            val optionalInt: OptionalInt = Parser.parseInt(value)
            if (optionalInt.isPresent) {
                sendWorldPage(world, sender, optionalInt.asInt)
            }
            return true
        }
        val sel: NightSelection = worldSettings.nightSelection
        if (TabCompleteUtil.isCommand(
                field, "interval", "intervalProbability", "probability", "phaseAmount",
                "period", "minCurveVal", "maxCurveVal"
            )
        ) {
            val optionalInt: OptionalInt = Parser.parseInt(value)
            if (!optionalInt.isPresent) {
                messageSender().sendError(player, localizer().getMessage("error.invalidNumber"))
                return true
            }
            if ("interval".equals(field, ignoreCase = true)) {
                sel.interval = (EMath.clamp(1, 100, optionalInt.asInt))
            }
            if ("intervalProbability".equals(field, ignoreCase = true)) {
                sel.intervalProbability = (EMath.clamp(0, 100, optionalInt.asInt))
            }
            if ("probability".equals(field, ignoreCase = true)) {
                sel.probability = (EMath.clamp(1, 100, optionalInt.asInt))
            }
            if ("phaseAmount".equals(field, ignoreCase = true)) {
                sel.setPhaseCount(EMath.clamp(1, 54, optionalInt.asInt))
            }
            if ("period".equals(field, ignoreCase = true)) {
                sel.period = (EMath.clamp(3, 100, optionalInt.asInt))
            }
            if ("minCurveVal".equals(field, ignoreCase = true)) {
                sel.minCurveVal = (EMath.clamp(0, 100, optionalInt.asInt))
            }
            if ("maxCurveVal".equals(field, ignoreCase = true)) {
                sel.maxCurveVal = (EMath.clamp(0, 100, optionalInt.asInt))
            }
            optPage.ifPresent { p: Int -> sendWorldPage(world, sender, p) }
            configuration.save()
            return true
        }
        if (TabCompleteUtil.isCommand(field, "moonPhase", "phase")) {
            val moonPhase = "moonPhase".equals(field, ignoreCase = true)
            val inv: Inventory = Bukkit.createInventory(
                player, if (moonPhase) 9 else 54,
                localizer().getMessage(if (moonPhase) "nightSelection.title.moonPhase" else "nightSelection.title.phase")
            )
            val stacks: List<ItemStack> =
                PhaseItem.getPhaseItems(if (moonPhase) sel.moonPhase else sel.phaseCustom, moonPhase)
            inv.setContents(stacks.toTypedArray())
            player.openInventory(inv)
            inventoryListener.registerModification(player, object : InventoryListener.InventoryActionHandler {
                override fun onInventoryClose(event: InventoryCloseEvent) {
                    event.inventory.contents
                        .filterNotNull()
                        .map(PhaseItem::fromItemStack)
                        .forEach(Consumer {
                            if (moonPhase) {
                                sel.setMoonPhase(it.first, it.second)
                            } else {
                                sel.setPhaseCustom(it.first, it.second)
                            }
                        })
                    optPage.ifPresent { i: Int -> sendWorldPage(world, sender, i) }
                }

                override fun onInventoryClick(event: InventoryClickEvent) {
                    if (event.inventory.type != InventoryType.CHEST) return
                    if (event.view.topInventory != event.clickedInventory) {
                        return
                    }
                    when (event.click) {
                        ClickType.LEFT -> PhaseItem.changeProbability(event.currentItem, 1, moonPhase)
                        ClickType.SHIFT_LEFT -> PhaseItem.changeProbability(event.currentItem, 10, moonPhase)
                        ClickType.RIGHT -> PhaseItem.changeProbability(event.currentItem, -1, moonPhase)
                        ClickType.SHIFT_RIGHT -> PhaseItem.changeProbability(event.currentItem, -10, moonPhase)
                        else -> {}
                    }
                    event.isCancelled = true
                }
            })
            return true
        }
        if (TabCompleteUtil.isCommand(field, "type")) {
            val parse =
                EnumUtil.parse(value, NightSelection.NightSelectionType::class.java)
            if (parse == null) {
                messageSender().sendLocalizedError(sender, "error.invalidValue")
                return true
            }
            sel.nightSelectionType = parse
            configuration.save()
            optPage.ifPresent { p: Int -> sendWorldPage(world, sender, p) }
            return true
        }
        return true
    }

    private fun sendWorldPage(world: World, sender: CommandSender, p: Int) {
        val page: TextComponent = CommandUtil.getPage(
            configuration.worldSettings.values, p, 3, 4, { s ->
                val ns: NightSelection = s.nightSelection
                val cmd = "/bloodnight nightSelection " + ArgumentUtils.escapeWorldName(s.worldName) + " "
                val builder: TextComponent.Builder = Component.text()
                    .append(Component.text(s.worldName, NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.newline())
                    .append(
                        Component.text(
                            localizer().getMessage("field.nightSelectionType") + ":",
                            NamedTextColor.AQUA
                        )
                    )
                    .append(Component.newline())
                    .append(
                        CommandUtil.getToggleField(
                            ns.nightSelectionType === NightSelection.NightSelectionType.RANDOM,
                            cmd + "type random",
                            localizer().getMessage("state.random")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            ns.nightSelectionType === NightSelection.NightSelectionType.MOON_PHASE,
                            cmd + "type moon_phase",
                            localizer().getMessage("state.moonPhase")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            ns.nightSelectionType === NightSelection.NightSelectionType.REAL_MOON_PHASE,
                            cmd + "type real_moon_phase",
                            localizer().getMessage("state.realMoonPhase")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            ns.nightSelectionType === NightSelection.NightSelectionType.INTERVAL,
                            cmd + "type interval",
                            localizer().getMessage("state.interval")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            ns.nightSelectionType === NightSelection.NightSelectionType.PHASE,
                            cmd + "type phase",
                            localizer().getMessage("state.phase")
                        )
                    )
                    .append(Component.space())
                    .append(
                        CommandUtil.getToggleField(
                            ns.nightSelectionType === NightSelection.NightSelectionType.CURVE,
                            cmd + "type curve",
                            localizer().getMessage("state.curve")
                        )
                    )
                    .append(Component.space())
                    .append(Component.newline())
                when (ns.nightSelectionType) {
                    NightSelection.NightSelectionType.RANDOM -> builder.append(
                        Component.text(
                            localizer().getMessage("field.probability") + ": ",
                            NamedTextColor.AQUA
                        )
                    )
                        .append(Component.text(ns.probability, NamedTextColor.GOLD))
                        .append(
                            Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.suggestCommand(cmd + "probability "))
                        )
                        .append(Component.newline())
                        .append(Component.newline())
                    NightSelection.NightSelectionType.REAL_MOON_PHASE, NightSelection.NightSelectionType.MOON_PHASE -> {
                        builder.append(
                            Component.text(
                                localizer().getMessage("field.moonPhase") + ": ",
                                NamedTextColor.AQUA
                            )
                        )
                            .append(
                                Component.text(
                                    " [" + localizer().getMessage("action.change") + "]",
                                    NamedTextColor.GREEN
                                )
                                    .clickEvent(ClickEvent.runCommand(cmd + "moonPhase none"))
                            )
                            .append(Component.newline())
                        ns.moonPhase.forEach { (key, value) ->
                            builder.append(
                                Component.text("| $key:$value |", NamedTextColor.GOLD)
                                    .hoverEvent(
                                        HoverEvent.showText(
                                            Component.text()
                                                .append(
                                                    Component.text(
                                                        localizer().getMessage("field.moonPhase") + " " + key,
                                                        NamedTextColor.GOLD
                                                    )
                                                )
                                                .append(Component.newline())
                                                .append(
                                                    Component.text(
                                                        localizer().getMessage(getMoonPhaseName(key)),
                                                        NamedTextColor.AQUA
                                                    )
                                                )
                                                .append(Component.newline())
                                                .append(Component.text(getMoonPhaseSign(key)))
                                                .append(Component.newline())
                                                .append(
                                                    Component.text(
                                                        localizer().getMessage("field.probability") + ": " + value,
                                                        NamedTextColor.GREEN
                                                    )
                                                )
                                                .build()
                                        )
                                    )
                            )
                        }
                        builder.append(Component.newline())
                    }
                    NightSelection.NightSelectionType.INTERVAL -> builder.append(
                        Component.text(
                            localizer().getMessage("field.interval") + ": ",
                            NamedTextColor.AQUA
                        )
                    )
                        .append(Component.text(ns.interval, NamedTextColor.GOLD))
                        .append(
                            Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.suggestCommand(cmd + "interval "))
                        )
                        .append(Component.newline())
                        .append(
                            Component.text(
                                localizer().getMessage("field.intervalProbability") + ": ",
                                NamedTextColor.AQUA
                            )
                        )
                        .append(Component.text(ns.intervalProbability, NamedTextColor.GOLD))
                        .append(
                            Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.suggestCommand(cmd + "intervalProbability "))
                        )
                        .append(Component.newline())
                    NightSelection.NightSelectionType.PHASE -> builder.append(
                        Component.text(
                            localizer().getMessage("field.amount") + ": ",
                            NamedTextColor.AQUA
                        )
                    )
                        .append(Component.text(ns.phaseCustom.size, NamedTextColor.GOLD))
                        .append(
                            Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.suggestCommand(cmd + "phaseAmount "))
                        )
                        .append(Component.newline())
                        .append(Component.text(localizer().getMessage("field.phase") + ": ", NamedTextColor.AQUA))
                        .append(
                            Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.runCommand(cmd + "phase none"))
                        )
                        .append(Component.newline())
                    NightSelection.NightSelectionType.CURVE -> builder.append(
                        Component.text(
                            localizer().getMessage("field.length") + ": ",
                            NamedTextColor.AQUA
                        )
                    )
                        .append(Component.text(ns.period, NamedTextColor.GOLD))
                        .append(
                            Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.suggestCommand(cmd + "period "))
                        )
                        .append(Component.newline())
                        .append(Component.text(localizer().getMessage("field.minProb") + ": ", NamedTextColor.AQUA))
                        .append(Component.text(ns.minCurveVal, NamedTextColor.GOLD))
                        .append(
                            Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.suggestCommand(cmd + "minCurveVal "))
                        )
                        .append(Component.newline())
                        .append(Component.text(localizer().getMessage("field.maxProb") + ": ", NamedTextColor.AQUA))
                        .append(Component.text(ns.maxCurveVal, NamedTextColor.GOLD))
                        .append(
                            Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.suggestCommand(cmd + "maxCurveVal "))
                        )
                }
                builder.build()
            }, localizer().getMessage("nightSelection.title.menu"),
            "/bloodNight nightSelection " + ArgumentUtils.escapeWorldName(world) + " page {page}"
        )
        bukkitAudiences.sender(sender).sendMessage(page)
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
            return TabCompleteUtil.complete(
                args[1],
                "interval",
                "intervalProbability",
                "probability",
                "moonPhase",
                "type"
            )
        }
        val field = args[1]
        val value = args[2]
        if (TabCompleteUtil.isCommand(field, "interval")) {
            return TabCompleteUtil.completeInt(value, 1, 100, localizer())
        }
        if (TabCompleteUtil.isCommand(field, "intervalProbability", "probability", "minCurveVal", "maxCurveVal")) {
            return TabCompleteUtil.completeInt(value, 0, 100, localizer())
        }
        if (TabCompleteUtil.isCommand(field, "phaseAmount")) {
            return TabCompleteUtil.completeInt(value, 0, 54, localizer())
        }
        if (TabCompleteUtil.isCommand(field, "period")) {
            return TabCompleteUtil.completeInt(value, 3, 100, localizer())
        }
        if (TabCompleteUtil.isCommand(field, "moonPhase", "phase")) {
            return emptyList()
        }
        return if (TabCompleteUtil.isCommand(field, "type")) {
            TabCompleteUtil.complete(value, NightSelection.NightSelectionType::class.java)
        } else emptyList()
    }

    private object PhaseItem {

        private val PHASE: NamespacedKey = BloodNight.namespacedKey("phase")
        private val PROBABILITY: NamespacedKey = BloodNight.namespacedKey("probability")

        fun fromItemStack(itemStack: ItemStack): Pair<Int, Int> {
            return Pair.of(
                getPhase(
                    itemStack
                ), getProbability(itemStack)
            )
        }

        private fun toPhaseItem(entry: Map.Entry<Int, Int>, moon: Boolean): ItemStack {
            val phase = entry.key + 1
            val localizer: ILocalizer = ILocalizer.getPluginLocalizer(BloodNight::class.java)
            val stack = ItemStack(Material.FIREWORK_STAR, phase)
            val itemMeta: ItemMeta = stack.itemMeta
            itemMeta.setDisplayName(
                "§2" + localizer.getMessage("phaseItem.phase") + ": " + phase
                        + if (moon) " (" + localizer.getMessage(getMoonPhaseName(entry.key)) + ")" else ""
            )
            itemMeta.lore = getLore(entry.key, entry.value, moon)
            val container: PersistentDataContainer = itemMeta.persistentDataContainer
            container.set(PROBABILITY, PersistentDataType.INTEGER, entry.value)
            container.set(PHASE, PersistentDataType.INTEGER, entry.key)
            stack.itemMeta = itemMeta
            return stack
        }

        private fun getPhase(itemStack: ItemStack): Int {
            return itemStack.itemMeta.persistentDataContainer.get(PHASE, PersistentDataType.INTEGER)!!
        }

        private fun getProbability(itemStack: ItemStack): Int {
            return itemStack.itemMeta.persistentDataContainer
                .get(PROBABILITY, PersistentDataType.INTEGER)!!
        }

        private fun setProbability(item: ItemStack, weight: Int) {
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.itemMeta else Bukkit.getItemFactory().getItemMeta(item.type)
            val dataContainer: PersistentDataContainer = itemMeta.persistentDataContainer
            dataContainer.set(PROBABILITY, PersistentDataType.INTEGER, weight)
            item.itemMeta = itemMeta
        }

        private fun setProbabilityIfAbsent(item: ItemStack, weight: Int) {
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.itemMeta else Bukkit.getItemFactory().getItemMeta(item.type)
            val dataContainer: PersistentDataContainer = itemMeta.persistentDataContainer
            if (!dataContainer.has(PROBABILITY, PersistentDataType.INTEGER)) {
                setProbability(item, weight)
            }
        }

        fun changeProbability(item: ItemStack?, change: Int, moon: Boolean) {
            if (item == null) return
            val currProb = getProbability(item)
            val phase = getPhase(item)
            val newProb = min(max(currProb + change, 0), 100)
            setProbability(item, newProb)
            val itemMeta: ItemMeta = item.itemMeta!!
            itemMeta.lore = getLore(phase, newProb, moon)
            item.itemMeta = itemMeta
        }

        fun getPhaseItems(selection: Map<Int, Int>, moon: Boolean) =
            selection.entries.map { toPhaseItem(it, moon) }

        private fun getLore(phase: Int, probability: Int, moon: Boolean): List<String> {
            val result: MutableList<String> = ArrayList()
            if (moon) {
                result.add(getMoonPhaseSign(phase))
            }
            result.add(
                "§6" + ILocalizer.getPluginLocalizer(BloodNight::class.java)
                    .getMessage("field.probability") + ": " + probability
            )
            return result
        }
    }

    companion object {
        private fun getMoonPhaseName(phase: Int): String {
            return "state.phase$phase"
        }

        private fun getMoonPhaseSign(phase: Int): String {
            return when (phase) {
                0 -> "§f████"
                1 -> "§f███§8█"
                2 -> "§f██§8██"
                3 -> "§f█§8███"
                4 -> "§8████"
                5 -> "§8███§f█"
                6 -> "§8██§f██"
                7 -> "§8█§f███"
                else -> throw IllegalStateException("Unexpected value: $phase")
            }
        }
    }

}