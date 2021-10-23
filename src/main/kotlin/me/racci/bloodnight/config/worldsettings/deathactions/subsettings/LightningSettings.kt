package me.racci.bloodnight.config.worldsettings.deathactions.subsettings

import de.eldoria.bloodnight.util.InvMenuUtil
import org.bukkit.inventory.Inventory

@Setter
@Getter
@SerializableAs("bloodNightLightningSettings")
class LightningSettings : ConfigurationSerializable {
    /**
     * Activate Lighting.
     */
    protected var doLightning = true

    /**
     * Probability of a lighting should be spawned above the death location.
     */
    protected var lightning = 100

    /**
     * Activate thunder.
     */
    protected var doThunder = true

    /**
     * If no lighting is send a thunder sound can be played optionally.
     */
    protected var thunder = 100

    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        doLightning = map.getValueOrDefault<Boolean>("doLightning", doLightning)
        lightning = map.getValueOrDefault<Int>("lightning", lightning)
        doThunder = map.getValueOrDefault<Boolean>("doThunder", doThunder)
        thunder = map.getValueOrDefault<Int>("thunder", thunder)
    }

    constructor() {}

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("doLightning", doLightning)
            .add("lightning", lightning)
            .add("doThunder", doThunder)
            .add("thunder", thunder)
            .build()
    }

    fun getInventoryRepresentation(inventoryHolder: Player?): Inventory? {
        val inventory: Inventory = Bukkit.createInventory(inventoryHolder, 9)
        var type: Material
        val lightingState = ItemStack(InvMenuUtil.getBooleanMaterial(doLightning))
        return null
    }
}