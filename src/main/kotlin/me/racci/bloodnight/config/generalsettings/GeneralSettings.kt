package me.racci.bloodnight.config.generalsettings

import de.eldoria.eldoutilities.serialization.SerializationUtil
import me.racci.bloodnight.core.BloodNight
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("bloodNightGeneralSettings")
class GeneralSettings : ConfigurationSerializable {
    var language                = "en_US"
    var prefix                  = "§4[BN]"
    var broadcastLevel          = BroadcastLevel.SERVER
    var broadcastMethod         = BroadcastMethod.SUBTITLE
    var messageMethod           = BroadcastMethod.SUBTITLE
    var mobTick                 = 5
    var blindness               = true
    var joinWorldWarning        = true
    var updateReminder          = true
    var autoUpdater             = false
    var beeFix                  = false
    var spawnerDropSuppression  = true
    var ignoreSpawnerMobs       = false
    var blockedCommands         = ArrayList<String>()

    constructor(objectMap: Map<String, Any>) {
        val map                 = SerializationUtil.mapOf(objectMap)
        language                = map.getValueOrDefault("language", language)
        prefix                  = map.getValueOrDefault("prefix", prefix.replace("&", "§"))
        broadcastLevel          = map.getValueOrDefault("broadcastLevel", broadcastLevel, BroadcastLevel::class.java)
        broadcastMethod         = map.getValueOrDefault("broadcastMethod", broadcastMethod, BroadcastMethod::class.java)
        messageMethod           = map.getValueOrDefault("messageMethod", messageMethod, BroadcastMethod::class.java)
        mobTick                 = map.getValueOrDefault("mobTick", mobTick)
        joinWorldWarning        = map.getValueOrDefault("joinWorldWarning", joinWorldWarning)
        blindness               = map.getValueOrDefault("blindness", blindness)
        updateReminder          = map.getValueOrDefault("updateReminder", updateReminder)
        autoUpdater             = map.getValueOrDefault("autoUpdater", autoUpdater)
        beeFix                  = map.getValueOrDefault("beeFix", beeFix)
        spawnerDropSuppression  = map.getValueOrDefault("spawnerDropSuppression", spawnerDropSuppression)
        ignoreSpawnerMobs       = map.getValueOrDefault("ignoreSpawnerMobs", ignoreSpawnerMobs)
        blockedCommands         = map.getValueOrDefault("blockedCommands", blockedCommands)
        if (beeFix) BloodNight.logger().info("§4Bee Fix is enabled. This feature should be used with care.")
    }

    constructor()

    override fun serialize(): Map<String, Any> {
        return SerializationUtil.newBuilder()
            .add("language", language)
            .add("prefix", prefix)
            .add("broadcastLevel", broadcastLevel.name)
            .add("broadcastMethod", broadcastMethod.name)
            .add("messageMethod", messageMethod.name)
            .add("mobTick", mobTick)
            .add("joinWorldWarning", joinWorldWarning)
            .add("blindness", blindness)
            .add("updateReminder", updateReminder)
            .add("autoUpdater", autoUpdater)
            .add("beeFix", beeFix)
            .add("spawnerDropSuppression", spawnerDropSuppression)
            .add("ignoreSpawnerMobs", ignoreSpawnerMobs)
            .add("blockedCommands", blockedCommands)
            .build()
    }
}