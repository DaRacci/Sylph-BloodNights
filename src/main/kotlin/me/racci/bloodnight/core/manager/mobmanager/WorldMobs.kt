package me.racci.bloodnight.core.manager.mobmanager

import me.racci.bloodnight.specialmobs.SpecialMob
import org.bukkit.entity.Entity
import java.util.*
import java.util.function.Consumer

class WorldMobs {
    private val mobs = HashMap<UUID, SpecialMob<*>>()
    private val tickQueue = ArrayDeque<SpecialMob<*>>()
    private var entityTick = 0.0

    fun invokeIfPresent(entity: Entity, invoke: Consumer<SpecialMob<*>>) {
        invokeIfPresent(entity.uniqueId, invoke)
    }

    fun invokeIfPresent(uuid: UUID, invoke: Consumer<SpecialMob<*>>) {
        val specialMob = mobs[uuid]
        if (specialMob != null) {
            invoke.accept(specialMob)
        }
    }

    fun invokeAll(invoke: Consumer<SpecialMob<*>>) {
        mobs.values.forEach(invoke)
    }

    fun tick(tickDelay: Int) {
        if (tickQueue.isEmpty()) return
        entityTick += tickQueue.size / tickDelay.toDouble()
        while (entityTick > 0) {
            if (tickQueue.isEmpty()) return
            val poll = tickQueue.poll()
            if (!poll.baseEntity.isValid) {
                remove(poll.baseEntity.uniqueId)
                poll.remove()
            } else {
                poll.tick()
                tickQueue.add(poll)
            }
            entityTick--
        }
    }

    val isEmpty: Boolean
        get() = mobs.isEmpty()

    fun put(key: UUID, value: SpecialMob<*>) {
        mobs[key] = value
        tickQueue.add(value)
    }

    /**
     * Attempts to remove an entity from world mobs and the world.
     *
     * @param key uid of entity
     * @return special mob if present.
     */
    fun remove(key: UUID): SpecialMob<*>? {
        if (!mobs.containsKey(key)) return null
        val removed = mobs.remove(key)
        tickQueue.remove(removed)
        removed?.remove()
        return removed
    }

    fun clear() {
        mobs.clear()
        tickQueue.clear()
    }
}