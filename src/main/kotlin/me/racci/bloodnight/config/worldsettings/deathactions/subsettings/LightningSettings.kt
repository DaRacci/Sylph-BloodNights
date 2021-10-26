package me.racci.bloodnight.config.worldsettings.deathactions.subsettings

import de.eldoria.eldoutilities.serialization.SerializationUtil
import me.racci.bloodnight.util.InvMenuUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

@SerializableAs("bloodNightLightningSettings")
class LightningSettings : ConfigurationSerializable {
    /**
     * Activate Lighting.
     */
    var doLightning = true

    /**
     * Probability of a lighting should be spawned above the death location.
     */
    var lightning = 100

    /**
     * Activate thunder.
     */
    var doThunder = true

    /**
     * If no lighting is sent a thunder sound can be played optionally.
     */
    var thunder = 100

    constructor(objectMap: Map<String, Any>) {
        val map = SerializationUtil.mapOf(objectMap)
        doLightning = map.getValueOrDefault("doLightning", doLightning)
        lightning = map.getValueOrDefault("lightning", lightning)
        doThunder = map.getValueOrDefault("doThunder", doThunder)
        thunder = map.getValueOrDefault("thunder", thunder)
    }

    constructor()

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("doLightning", doLightning)
            .add("lightning", lightning)
            .add("doThunder", doThunder)
            .add("thunder", thunder)
            .build()
    }

    fun getInventoryRepresentation(inventoryHolder: Player): Inventory? {
        val inventory = Bukkit.createInventory(inventoryHolder, 9)
        var type: Material
        val lightingState = ItemStack(InvMenuUtil.getBooleanMaterial(doLightning))
        return null
    }
}