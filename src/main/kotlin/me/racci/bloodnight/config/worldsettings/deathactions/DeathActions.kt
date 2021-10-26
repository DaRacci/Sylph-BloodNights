package me.racci.bloodnight.config.worldsettings.deathactions

import de.eldoria.eldoutilities.serialization.SerializationUtil
import me.racci.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings
import me.racci.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("bloodNightDeathActions")
open class DeathActions : ConfigurationSerializable {

    var lightningSettings = LightningSettings()
    var shockwaveSettings = ShockwaveSettings()

    constructor(objectMap: Map<String, Any>) {
        val map = SerializationUtil.mapOf(objectMap)
        lightningSettings = map.getValueOrDefault("lightningSettings", lightningSettings)
        shockwaveSettings = map.getValueOrDefault("shockwaveSettings", shockwaveSettings)
    }

    constructor()

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("lightningSettings", lightningSettings)
            .add("shockwaveSettings", shockwaveSettings)
            .build()
    }
}