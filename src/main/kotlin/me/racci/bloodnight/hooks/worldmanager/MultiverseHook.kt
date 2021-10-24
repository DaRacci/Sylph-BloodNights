package me.racci.bloodnight.hooks.worldmanager

import com.onarandombox.MultiverseCore.MultiverseCore
import me.racci.bloodnight.hooks.AbstractHookService
import org.bukkit.World

class MultiverseHook : AbstractHookService<MultiverseCore>("Multiverse-Core"), WorldManager {
    private var plugin: MultiverseCore? = null

    override val hook: MultiverseCore
        get() {
            if (plugin == null) {
                plugin = MultiverseCore.getPlugin(MultiverseCore::class.java)
            }
            return plugin as MultiverseCore
        }

    override fun setup() {}

    override fun shutdown() {}

    override fun getAlias(world: World): String {
        return try {
            hook.mvWorldManager.getMVWorld(world).alias
        } catch (e: ClassNotFoundException) {
            world.name
        }
    }
}