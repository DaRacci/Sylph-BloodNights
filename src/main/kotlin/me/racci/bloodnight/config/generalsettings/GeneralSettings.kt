package me.racci.bloodnight.config.generalsettings

import de.eldoria.bloodnight.core.BloodNight
import java.util.ArrayList

@Getter
@Setter
@SerializableAs("bloodNightGeneralSettings")
class GeneralSettings : ConfigurationSerializable {
    private var language = "en_US"
    private var prefix = "§4[BN]"
    private var broadcastLevel = BroadcastLevel.SERVER
    private var broadcastMethod = BroadcastMethod.SUBTITLE
    private var messageMethod = BroadcastMethod.SUBTITLE
    private var mobTick = 5
    private var blindness = true
    private var joinWorldWarning = true
    private var updateReminder = true
    private var autoUpdater = false
    private var beeFix = false
    private var spawnerDropSuppression = true
    private var ignoreSpawnerMobs = false
    private var blockedCommands: List<String> = ArrayList()

    constructor(objectMap: Map<String?, Any?>?) {
        val map: TypeResolvingMap = SerializationUtil.mapOf(objectMap)
        language = map.getValueOrDefault<String>("language", language)
        prefix = map.getValueOrDefault<String>("prefix", prefix.replace("&", "§"))
        broadcastLevel =
            map.getValueOrDefault<BroadcastLevel>("broadcastLevel", broadcastLevel, BroadcastLevel::class.java)
        broadcastMethod =
            map.getValueOrDefault<BroadcastMethod>("broadcastMethod", broadcastMethod, BroadcastMethod::class.java)
        messageMethod =
            map.getValueOrDefault<BroadcastMethod>("messageMethod", messageMethod, BroadcastMethod::class.java)
        mobTick = map.getValueOrDefault<Int>("mobTick", mobTick)
        joinWorldWarning = map.getValueOrDefault<Boolean>("joinWorldWarning", joinWorldWarning)
        blindness = map.getValueOrDefault<Boolean>("blindness", blindness)
        updateReminder = map.getValueOrDefault<Boolean>("updateReminder", updateReminder)
        autoUpdater = map.getValueOrDefault<Boolean>("autoUpdater", autoUpdater)
        beeFix = map.getValueOrDefault<Boolean>("beeFix", beeFix)
        spawnerDropSuppression = map.getValueOrDefault<Boolean>("spawnerDropSuppression", spawnerDropSuppression)
        ignoreSpawnerMobs = map.getValueOrDefault<Boolean>("ignoreSpawnerMobs", ignoreSpawnerMobs)
        blockedCommands = map.getValueOrDefault<List<String>>("blockedCommands", blockedCommands)
        if (beeFix) {
            BloodNight.logger().info("§4Bee Fix is enabled. This feature should be used with care.")
        }
    }

    constructor() {}

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