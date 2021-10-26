package me.racci.bloodnight.config.worldsettings.deathactions

import de.eldoria.eldoutilities.serialization.SerializationUtil
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.potion.PotionEffectType

@SerializableAs("bloodNightPotionEffectSettings")
class PotionEffectSettings : ConfigurationSerializable {

    var effectType: PotionEffectType; private set
    var duration = 10; private set

    constructor(effectType: PotionEffectType, duration: Int) {
        this.effectType = effectType
        this.duration = duration
    }

    constructor(objectMap: Map<String, Any>) {
        val map = SerializationUtil.mapOf(objectMap)
        effectType = map.getValue("effectType") { PotionEffectType.getByName(it) }!!
        duration = map.getValueOrDefault("duration", duration)
    }

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("effectType", effectType.name)
            .add("duration", duration)
            .build()
    }
}