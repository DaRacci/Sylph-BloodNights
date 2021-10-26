package me.racci.bloodnight.config.worldsettings.mobsettings

import de.eldoria.eldoutilities.localization.ILocalizer
import de.eldoria.eldoutilities.serialization.SerializationUtil
import de.eldoria.eldoutilities.serialization.TypeResolvingMap
import me.racci.bloodnight.core.BloodNight
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.regex.Pattern

@Suppress("UNUSED", "DEPRECATION")
@SerializableAs("bloodNightDrop")
class Drop : ConfigurationSerializable {
    val item: ItemStack
        get() = field.clone()
    val weight: Int

    constructor(objectMap: Map<String, Any>) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        item = map.getValue("item")
        weight = map.getValue("weight")
    }

    constructor(item: ItemStack, weight: Int) {
        this.item = item
        this.weight = weight
    }

    override fun serialize(): MutableMap<String, Any> =
        SerializationUtil.newBuilder()
            .add("item", item)
            .add("weight", weight)
            .build()

    val weightedItem: ItemStack
        get() {
            val newItem: ItemStack = item.clone()
            setWeight(newItem, weight)
            return newItem
        }

    val itemWithLoreWeight: ItemStack
        get() {
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.itemMeta else Bukkit.getItemFactory().getItemMeta(item.type)
            val lore: MutableList<String> = if (itemMeta.hasLore()) itemMeta.lore!! else ArrayList()
            lore.add("§6Weight: $weight")
            itemMeta.lore = lore
            val newItem: ItemStack = item.clone()
            newItem.itemMeta = itemMeta
            return newItem
        }

    companion object {

        private val WEIGHT_KEY = BloodNight.namespacedKey("dropWeight")

        fun fromItemStack(itemStack: ItemStack) =
            Drop(
                removeWeight(itemStack),
                getWeightFromItemStack(itemStack)
            )

        fun changeWeight(item: ItemStack?, change: Int) {
            if (item == null) return
            val currWeight = getWeightFromItemStack(item)
            val newWeight = (currWeight + change).coerceAtLeast(1).coerceAtMost(100)
            setWeight(item, newWeight)
            val weight = regexWeight
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.itemMeta else Bukkit.getItemFactory().getItemMeta(item.type)
            val lore: MutableList<String> = if (itemMeta.hasLore()) itemMeta.lore!! else ArrayList()
            if (lore.isEmpty()) {
                lore.add(getWeightString(newWeight))
            } else {
                val matcher = weight.matcher(lore[lore.size - 1])
                if (matcher.find()) {
                    lore[lore.size - 1] = getWeightString(newWeight)
                } else {
                    lore.add(getWeightString(newWeight))
                }
            }
            itemMeta.lore = lore
            item.itemMeta = itemMeta
        }

        private fun removeWeight(item: ItemStack): ItemStack {
            val weight = regexWeight
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.itemMeta else Bukkit.getItemFactory().getItemMeta(item.type)
            val lore = if (itemMeta.hasLore()) itemMeta.lore!! else ArrayList()
            if (lore.isEmpty()) {
                return item
            } else {
                val matcher = weight.matcher(lore[lore.size - 1])
                if (matcher.find()) {
                    lore.removeAt(lore.size - 1)
                } else {
                    return item
                }
            }
            val newItem: ItemStack = item.clone()
            itemMeta.lore = lore
            val container: PersistentDataContainer = itemMeta.persistentDataContainer
            if (container.has(WEIGHT_KEY, PersistentDataType.INTEGER)) {
                container.remove(WEIGHT_KEY)
            }
            newItem.itemMeta = itemMeta
            return newItem
        }

        private fun getWeightFromItemStack(item: ItemStack): Int {
            setWeightIfNotSet(item, 1)
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.itemMeta else Bukkit.getItemFactory().getItemMeta(item.type)
            val dataContainer: PersistentDataContainer = itemMeta.persistentDataContainer
            return dataContainer[WEIGHT_KEY, PersistentDataType.INTEGER]!!
        }

        private val regexWeight: Pattern
            get() = Pattern.compile(
                "§6" + ILocalizer.getPluginLocalizer(BloodNight::class.java)
                    .getMessage("drops.weight") + ":\\s([0-9]+?)"
            )

        private fun setWeight(item: ItemStack, weight: Int) {
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.itemMeta else Bukkit.getItemFactory().getItemMeta(item.type)
            val dataContainer: PersistentDataContainer = itemMeta.persistentDataContainer
            dataContainer[WEIGHT_KEY, PersistentDataType.INTEGER] = weight
            item.itemMeta = itemMeta
        }

        private fun setWeightIfNotSet(item: ItemStack, weight: Int) {
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.itemMeta else Bukkit.getItemFactory().getItemMeta(item.type)
            val dataContainer: PersistentDataContainer = itemMeta.persistentDataContainer
            if (!dataContainer.has(WEIGHT_KEY, PersistentDataType.INTEGER)) {
                setWeight(item, weight)
            }
        }

        private fun getWeightString(weight: Int) =
            "§6" + ILocalizer.getPluginLocalizer(BloodNight::class.java)
                .getMessage("drops.weight") + ": " + weight
    }
}