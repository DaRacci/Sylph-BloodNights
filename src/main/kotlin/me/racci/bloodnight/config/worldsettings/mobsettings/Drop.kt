package me.racci.bloodnight.config.worldsettings.mobsettings

import de.eldoria.bloodnight.core.BloodNight
import java.util.ArrayList
import java.util.regex.Pattern

@Getter
@SerializableAs("bloodNightDrop")
class Drop : ConfigurationSerializable {
    private val item: ItemStack
    private val weight: Int

    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        item = map.getValue<ItemStack>("item")
        weight = map.getValue<Int>("weight")
    }

    constructor(item: ItemStack, weight: Int) {
        this.item = item
        this.weight = weight
    }

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("item", item)
            .add("weight", weight)
            .build()
    }

    val weightedItem: ItemStack
        get() {
            val newItem: ItemStack = item.clone()
            setWeight(newItem, weight)
            return newItem
        }

    fun getItem(): ItemStack {
        return item.clone()
    }

    val itemWithLoreWeight: ItemStack
        get() {
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.getItemMeta() else Bukkit.getItemFactory().getItemMeta(item.getType())
            val lore: MutableList<String> = if (itemMeta.hasLore()) itemMeta.getLore() else ArrayList()
            lore.add("§6Weight: " + getWeight())
            itemMeta.setLore(lore)
            val newItem: ItemStack = item.clone()
            newItem.setItemMeta(itemMeta)
            return newItem
        }

    companion object {
        private val WEIGHT_KEY: NamespacedKey = BloodNight.getNamespacedKey("dropWeight")
        fun fromItemStack(itemStack: ItemStack?): Drop? {
            return if (itemStack == null) null else Drop(
                removeWeight(itemStack),
                getWeightFromItemStack(itemStack)
            )
        }

        fun changeWeight(item: ItemStack, change: Int) {
            val currWeight = getWeightFromItemStack(item)
            val newWeight = Math.min(Math.max(currWeight + change, 1), 100)
            setWeight(item, newWeight)
            val weight = regexWeight
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.getItemMeta() else Bukkit.getItemFactory().getItemMeta(item.getType())
            val lore: MutableList<String> = if (itemMeta.hasLore()) itemMeta.getLore() else ArrayList()
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
            itemMeta.setLore(lore)
            item.setItemMeta(itemMeta)
        }

        fun removeWeight(item: ItemStack): ItemStack {
            val weight = regexWeight
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.getItemMeta() else Bukkit.getItemFactory().getItemMeta(item.getType())
            val lore: List<String> = if (itemMeta.hasLore()) itemMeta.getLore() else ArrayList()
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
            itemMeta.setLore(lore)
            val container: PersistentDataContainer = itemMeta.getPersistentDataContainer()
            if (container.has<Int, Int>(WEIGHT_KEY, PersistentDataType.INTEGER)) {
                container.remove(WEIGHT_KEY)
            }
            newItem.setItemMeta(itemMeta)
            return newItem
        }

        fun getWeightFromItemStack(item: ItemStack): Int {
            setWeightIfNotSet(item, 1)
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.getItemMeta() else Bukkit.getItemFactory().getItemMeta(item.getType())
            val dataContainer: PersistentDataContainer = itemMeta.getPersistentDataContainer()
            return dataContainer.get<Int, Int>(WEIGHT_KEY, PersistentDataType.INTEGER)
        }

        private val regexWeight: Pattern
            private get() = Pattern.compile(
                "§6" + ILocalizer.getPluginLocalizer(BloodNight::class.java)
                    .getMessage("drops.weight") + ":\\s([0-9]+?)"
            )

        private fun setWeight(item: ItemStack, weight: Int) {
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.getItemMeta() else Bukkit.getItemFactory().getItemMeta(item.getType())
            val dataContainer: PersistentDataContainer = itemMeta.getPersistentDataContainer()
            dataContainer.set<Int, Int>(WEIGHT_KEY, PersistentDataType.INTEGER, weight)
            item.setItemMeta(itemMeta)
        }

        private fun setWeightIfNotSet(item: ItemStack, weight: Int) {
            val itemMeta: ItemMeta =
                if (item.hasItemMeta()) item.getItemMeta() else Bukkit.getItemFactory().getItemMeta(item.getType())
            val dataContainer: PersistentDataContainer = itemMeta.getPersistentDataContainer()
            if (!dataContainer.has<Int, Int>(WEIGHT_KEY, PersistentDataType.INTEGER)) {
                setWeight(item, weight)
            }
        }

        private fun getWeightString(weight: Int): String {
            return "§6" + ILocalizer.getPluginLocalizer(BloodNight::class.java)
                .getMessage("drops.weight") + ": " + weight
        }
    }
}