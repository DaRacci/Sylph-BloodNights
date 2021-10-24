package me.racci.bloodnight.util

import me.racci.bloodnight.core.BloodNight
import org.bukkit.NamespacedKey
import org.bukkit.World

fun getBossBarNamespace(world: World): NamespacedKey {
    return BloodNight.namespacedKey("bossBar${world.name}")
}
