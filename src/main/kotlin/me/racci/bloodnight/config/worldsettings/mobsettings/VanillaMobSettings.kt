package me.racci.bloodnight.config.worldsettings.mobsettings

import de.eldoria.eldoutilities.serialization.SerializationUtil
import de.eldoria.eldoutilities.utils.EnumUtil
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("bloodNightVanillaMobSettings")
class VanillaMobSettings : ConfigurationSerializable {
    /**
     * The modifier which will be multiplied with monster damage when a non special mob deals damage to players.
     */
    var damageMultiplier = 2.0

    /**
     * The value the damage will be divided by when a player damages a non special mob
     */
    var healthMultiplier = 2.0

    /**
     * The modifier which will be multiplied with the dropped item amount.
     */
    var dropMultiplier = 2.0
    var vanillaDropMode = VanillaDropMode.VANILLA
    var extraDrops = 1

    constructor()

    constructor(objectMap: Map<String, Any>) {
        val map             = SerializationUtil.mapOf(objectMap)
        damageMultiplier    = map.getValueOrDefault("damageMultiplier", damageMultiplier)
        healthMultiplier    = map.getValueOrDefault("healthMultiplier", healthMultiplier)
        dropMultiplier      = map.getValueOrDefault("dropMultiplier", dropMultiplier)
        vanillaDropMode     = EnumUtil.parse(
            map.getValueOrDefault("vanillaDropMode", vanillaDropMode.name),
            VanillaDropMode::class.java
        )
        extraDrops          = map.getValueOrDefault("extraDrops", extraDrops)
    }

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("damageMultiplier", damageMultiplier)
            .add("healthMultiplier", healthMultiplier)
            .add("dropMultiplier", dropMultiplier)
            .add("vanillaDropMode", vanillaDropMode.toString())
            .add("extraDrops", extraDrops)
            .build()
    }
}