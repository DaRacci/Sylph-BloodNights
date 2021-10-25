package me.racci.bloodnight.util

import me.racci.bloodnight.core.BloodNight
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Entity

fun getBossBarNamespace(world: World) =
    BloodNight.namespacedKey("bossBar${world.name}")


fun uuidBossBarNamespace(entity: Entity) =
    BloodNight.namespacedKey("bossBar${entity.uniqueId}")
