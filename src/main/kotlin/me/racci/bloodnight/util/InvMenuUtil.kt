package me.racci.bloodnight.util

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object InvMenuUtil {
    private val INV_MENU_UTIL = "inv_menu_util"
    private val BOOLEAN_KEY: NamespacedKey = NamespacedKey(INV_MENU_UTIL, "boolean")

    /**
     * Get a item stack with the matching material based on the state.
     *
     *
     * This method will apply a nbt tag which marks this item as a boolean item.
     *
     * @param state current boolean state
     * @return item stack with correct material and boolean nbt tag.
     */
    fun getBooleanMaterial(state: Boolean): ItemStack {
        val mat: Material = if (state) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE
        val itemStack = ItemStack(mat)
        if (itemStack.hasItemMeta()) {
            val itemMeta: ItemMeta = itemStack.itemMeta
            val container: PersistentDataContainer = itemMeta.persistentDataContainer
            container.set(BOOLEAN_KEY, PersistentDataType.BYTE, (if (state) 1 else 0).toByte())
            itemStack.itemMeta = itemMeta
        }
        return itemStack
    }

    fun toggleBoolean(itemStack: ItemStack) {
        if (itemStack.hasItemMeta()) {
            val itemMeta = itemStack.itemMeta
            val container = itemMeta.persistentDataContainer
            if (container.has(BOOLEAN_KEY, PersistentDataType.BYTE)) {
                val aByte = container.get(BOOLEAN_KEY, PersistentDataType.BYTE)
                container.set(BOOLEAN_KEY, PersistentDataType.BYTE, (if (aByte == 1.toByte()) 0 else 1).toByte())
            }
            itemStack.itemMeta = itemMeta
        }
    }

    fun getBoolean(itemStack: ItemStack): Boolean {
        if (itemStack.hasItemMeta()) {
            val itemMeta = itemStack.itemMeta
            val container = itemMeta.persistentDataContainer
            if (container.has(BOOLEAN_KEY, PersistentDataType.BYTE)) {
                return container.get(BOOLEAN_KEY, PersistentDataType.BYTE) == 1.toByte()
            }
        }
        return false
    }

    fun getBooleanMat(state: Boolean): Material {
        return if (state) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE
    }
}