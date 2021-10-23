package me.racci.bloodnight.config.worldsettings.deathactions

import lombok.Getter

@Getter
@Setter
@SerializableAs("bloodNightMobDeathActions")
class MobDeathActions : DeathActions {
    constructor(objectMap: Map<String?, Any?>?) : super(objectMap) {}
    constructor() {}
}