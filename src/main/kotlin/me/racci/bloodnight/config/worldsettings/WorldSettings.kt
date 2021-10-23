package me.racci.bloodnight.config.worldsettings

import de.eldoria.eldoutilities.serialization.SerializationUtil
import de.eldoria.eldoutilities.serialization.TypeResolvingMap
import me.racci.bloodnight.config.worldsettings.deathactions.DeathActionSettings
import me.racci.bloodnight.config.worldsettings.mobsettings.MobSettings
import me.racci.bloodnight.config.worldsettings.sound.SoundSettings
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("bloodNightWorldSettings")
class WorldSettings : ConfigurationSerializable {
    var worldName: String
    var enabled = false
    var creeperBlockDamage = false
    var alwaysManageCreepers = true
    var bossBarSettings = BossBarSettings()
    var nightSelection = NightSelection()
    var nightSettings = NightSettings()
    var mobSettings: MobSettings = MobSettings()
    var soundSettings: SoundSettings = SoundSettings()
    var deathActionSettings: DeathActionSettings = DeathActionSettings()

    constructor(objectMap: Map<String, Any>) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        worldName = map.getValue<String>("world")!!
        enabled = map.getValueOrDefault("enabled", enabled)
        creeperBlockDamage = map.getValueOrDefault("creeperBlockDamage", creeperBlockDamage)
        alwaysManageCreepers = map.getValueOrDefault("alwaysManageCreepers", alwaysManageCreepers)
        bossBarSettings = map.getValueOrDefault("bossBar", bossBarSettings)
        nightSelection = map.getValueOrDefault("nightSelection", nightSelection)
        nightSettings = map.getValueOrDefault("nightSettings", nightSettings)
        mobSettings = map.getValueOrDefault("mobSettings", mobSettings)
        soundSettings = map.getValueOrDefault("soundSettings", soundSettings)
        deathActionSettings = map.getValueOrDefault("deathActionSettings", deathActionSettings)
    }

    constructor(world: String) {
        worldName = world
    }

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("world", worldName)
            .add("enabled", enabled)
            .add("creeperBlockDamage", creeperBlockDamage)
            .add("alwaysManageCreepers", alwaysManageCreepers)
            .add("bossBar", bossBarSettings)
            .add("nightSelection", nightSelection)
            .add("nightSettings", nightSettings)
            .add("mobSettings", mobSettings)
            .add("soundSettings", soundSettings)
            .add("deathActionSettings", deathActionSettings)
            .build()
    }
}