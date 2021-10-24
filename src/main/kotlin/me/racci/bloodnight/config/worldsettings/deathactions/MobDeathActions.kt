package me.racci.bloodnight.config.worldsettings.deathactions

import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("bloodNightMobDeathActions")
class MobDeathActions : DeathActions {

    constructor(objectMap: Map<String, Any>) : super(objectMap)
    constructor()
}