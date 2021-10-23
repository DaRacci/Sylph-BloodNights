package me.racci.bloodnight.config.worldsettings.deathactions

import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings

@Getter
@Setter
@SerializableAs("bloodNightDeathActions")
open class DeathActions : ConfigurationSerializable {
    protected var lightningSettings: LightningSettings = LightningSettings()
    protected var shockwaveSettings: ShockwaveSettings = ShockwaveSettings()

    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        lightningSettings = map.getValueOrDefault("lightningSettings", lightningSettings)
        shockwaveSettings = map.getValueOrDefault("shockwaveSettings", shockwaveSettings)
    }

    constructor() {}

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("lightningSettings", lightningSettings)
            .add("shockwaveSettings", shockwaveSettings)
            .build()
    }
}