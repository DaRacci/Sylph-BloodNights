package me.racci.bloodnight.config.worldsettings.deathactions

import lombok.Getter

@Getter
@Setter
@SerializableAs("bloodNightDeathActionSettings")
class DeathActionSettings : ConfigurationSerializable {
    private var mobDeathActions = MobDeathActions()
    private var playerDeathActions = PlayerDeathActions()

    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        mobDeathActions = map.getValueOrDefault<MobDeathActions>("mobDeathActions", mobDeathActions)
        playerDeathActions = map.getValueOrDefault<PlayerDeathActions>("playerDeathActions", playerDeathActions)
    }

    constructor() {}

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("mobDeathActions", mobDeathActions)
            .add("playerDeathActions", playerDeathActions)
            .build()
    }
}