package me.racci.bloodnight.config.worldsettings.deathactions

import de.eldoria.eldoutilities.serialization.SerializationUtil
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("bloodNightDeathActionSettings")
class DeathActionSettings : ConfigurationSerializable {

    var mobDeathActions = MobDeathActions()
    var playerDeathActions = PlayerDeathActions()

    constructor(objectMap: Map<String, Any>) {
        val map             = SerializationUtil.mapOf(objectMap)
        mobDeathActions     = map.getValueOrDefault("mobDeathActions", mobDeathActions)
        playerDeathActions  = map.getValueOrDefault("playerDeathActions", playerDeathActions)
    }

    constructor()

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("mobDeathActions", mobDeathActions)
            .add("playerDeathActions", playerDeathActions)
            .build()
    }
}