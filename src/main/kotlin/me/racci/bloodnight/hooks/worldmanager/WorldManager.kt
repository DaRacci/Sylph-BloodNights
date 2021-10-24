package me.racci.bloodnight.hooks.worldmanager

import org.bukkit.World

interface WorldManager {

    fun getAlias(world: World): String

    companion object {
        val DEFAULT = World::getName
    }
}
