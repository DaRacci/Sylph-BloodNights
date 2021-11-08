package me.racci.bloodnight.core.mobfactory

import me.racci.bloodnight.specialmobs.SpecialMob
import me.racci.bloodnight.specialmobs.mobs.creeper.EnderCreeper
import me.racci.bloodnight.specialmobs.mobs.creeper.GhostCreeper
import me.racci.bloodnight.specialmobs.mobs.creeper.NervousPoweredCreeper
import me.racci.bloodnight.specialmobs.mobs.creeper.SpeedCreeper
import me.racci.bloodnight.specialmobs.mobs.creeper.ToxicCreeper
import me.racci.bloodnight.specialmobs.mobs.creeper.UnstableCreeper
import me.racci.bloodnight.specialmobs.mobs.enderman.FearfulEnderman
import me.racci.bloodnight.specialmobs.mobs.enderman.ToxicEnderman
import me.racci.bloodnight.specialmobs.mobs.phantom.FearfulPhantom
import me.racci.bloodnight.specialmobs.mobs.phantom.FirePhantom
import me.racci.bloodnight.specialmobs.mobs.phantom.PhantomSoul
import me.racci.bloodnight.specialmobs.mobs.skeleton.InvisibleSkeleton
import me.racci.bloodnight.specialmobs.mobs.skeleton.MagicSkeleton
import me.racci.bloodnight.specialmobs.mobs.slime.ToxicSlime
import me.racci.bloodnight.specialmobs.mobs.spider.BlazeRider
import me.racci.bloodnight.specialmobs.mobs.spider.SpeedSkeletonRider
import me.racci.bloodnight.specialmobs.mobs.spider.WitherSkeletonRider
import me.racci.bloodnight.specialmobs.mobs.witch.FireWizard
import me.racci.bloodnight.specialmobs.mobs.witch.ThunderWizard
import me.racci.bloodnight.specialmobs.mobs.witch.WitherWizard
import me.racci.bloodnight.specialmobs.mobs.zombie.ArmoredZombie
import me.racci.bloodnight.specialmobs.mobs.zombie.InvisibleZombie
import me.racci.bloodnight.specialmobs.mobs.zombie.SpeedZombie
import org.bukkit.entity.Creeper
import org.bukkit.entity.Enderman
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.EntityType.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Phantom
import org.bukkit.entity.Skeleton
import org.bukkit.entity.Slime
import org.bukkit.entity.Spider
import org.bukkit.entity.Witch
import org.bukkit.entity.Zombie
import java.util.Collections
import java.util.function.Function

object SpecialMobRegistry {

    private val MOB_GROUPS = HashMap<Class<out Entity>, MobGroup>()
    private val ENTITY_MAPPINGS = HashMap<Class<out Entity>, Class<out Entity>>()

    private fun <T : SpecialMob<*>> registerMob(
        entityType: EntityType,
        clazz: Class<T>,
        factory: Function<LivingEntity, SpecialMob<*>>
    ) {
        MOB_GROUPS.computeIfAbsent(entityType.entityClass!!) { MobGroup(entityType) }
            .registerFactory(clazz, factory)
    }

    private fun getMobBaseClass(entity: Entity): Class<out Entity>? {
        if (entity.type == UNKNOWN) return null
        return ENTITY_MAPPINGS.computeIfAbsent(
            entity.type.entityClass!!,
            Function<Class<out Entity>, Class<out Entity>> computeIfAbsent@{ k: Class<out Entity> ->
                for (clazz in MOB_GROUPS.keys) {
                    if (clazz.isAssignableFrom(k)) {
                        return@computeIfAbsent clazz
                    }
                }
                null
            }
        )
    }

    fun getMobGroup(entity: Entity): MobGroup? =
        mobGroups[getMobBaseClass(entity)]

    fun getMobGroup(name: String): MobGroup? =
        MOB_GROUPS.entries.firstOrNull { it.key.simpleName.equals(name, true) }?.value

    val registeredMobs: Set<MobFactory>
        get() {
            val wrappers: MutableSet<MobFactory> = HashSet()
            for (value in MOB_GROUPS.values) {
                wrappers.addAll(value.factories)
            }
            return wrappers
        }

    /**
     * Get a read only map of registered mob groups
     *
     * @return registered mob groups
     */
    val mobGroups: Map<Class<out Entity>, MobGroup>
        get() = Collections.unmodifiableMap(MOB_GROUPS)

    fun getMobFactoryByName(arg: String) =
        registeredMobs.firstOrNull { it.mobName.equals(arg, true) }

    init {
        registerMob(
            CREEPER,
            EnderCreeper::class.java
        ) { EnderCreeper(it as Creeper) }
        registerMob(
            CREEPER,
            GhostCreeper::class.java
        ) { GhostCreeper(it as Creeper) }
        registerMob(
            CREEPER,
            NervousPoweredCreeper::class.java,
        ) { NervousPoweredCreeper(it as Creeper) }
        registerMob(
            CREEPER,
            SpeedCreeper::class.java,
        ) { SpeedCreeper(it as Creeper) }
        registerMob(
            CREEPER,
            ToxicCreeper::class.java,
        ) { ToxicCreeper(it as Creeper) }
        registerMob(
            CREEPER,
            UnstableCreeper::class.java,
        ) { UnstableCreeper(it as Creeper) }

        // Enderman
        registerMob(
            ENDERMAN,
            FearfulEnderman::class.java,
        ) { FearfulEnderman(it as Enderman) }
        registerMob(
            ENDERMAN,
            ToxicEnderman::class.java,
        ) { ToxicEnderman(it as Enderman) }

        // Phantom
        registerMob(
            PHANTOM,
            FearfulPhantom::class.java,
        ) { FearfulPhantom(it as Phantom) }
        registerMob(
            PHANTOM,
            FirePhantom::class.java,
        ) { FirePhantom(it as Phantom) }
        registerMob(
            PHANTOM,
            PhantomSoul::class.java,
        ) { PhantomSoul(it as Phantom) }

        // Rider
        registerMob(
            SPIDER,
            BlazeRider::class.java,
        ) { BlazeRider(it as Spider) }
        registerMob(
            SPIDER,
            SpeedSkeletonRider::class.java,
        ) { SpeedSkeletonRider(it as Spider) }
        registerMob(
            SPIDER,
            WitherSkeletonRider::class.java,
        ) { WitherSkeletonRider(it as Spider) }

        // Skeleton
        registerMob(
            SKELETON,
            InvisibleSkeleton::class.java,
        ) { InvisibleSkeleton(it as Skeleton) }
        registerMob(
            SKELETON,
            MagicSkeleton::class.java,
        ) { MagicSkeleton(it as Skeleton) }

        // Slime
        registerMob(
            SLIME,
            ToxicSlime::class.java,
        ) { ToxicSlime(it as Slime) }

        // Witch
        registerMob(
            WITCH,
            FireWizard::class.java,
        ) { FireWizard(it as Witch) }
        registerMob(
            WITCH,
            ThunderWizard::class.java,
        ) { ThunderWizard(it as Witch) }
        registerMob(
            WITCH,
            WitherWizard::class.java,
        ) { WitherWizard(it as Witch) }

        // Zombie
        registerMob(
            ZOMBIE,
            ArmoredZombie::class.java,
        ) { ArmoredZombie(it as Zombie) }
        registerMob(
            ZOMBIE,
            InvisibleZombie::class.java,
        ) { InvisibleZombie(it as Zombie) }
        registerMob(
            ZOMBIE,
            SpeedZombie::class.java,
        ) { SpeedZombie(it as Zombie) }


        /*
         * HollowsEve2021 Mobs
         */
        /*registerMob(
            ZOMBIE,
            HollowAdventurer::class.java
        ) { HollowAdventurer(it as Zombie) }
        registerMob(
            SKELETON,
            HollowArcher::class.java
        ) { HollowArcher(it as Skeleton) }
        registerMob(
            PHANTOM,
            HollowHaunter::class.java
        ) { HollowHaunter(it as Phantom) }
        registerMob(
            STRAY,
            HollowRider::class.java
        ) { HollowRider(it as Stray) }
        registerMob(
            HUSK,
            HollowGoliath::class.java
        ) { HollowGoliath(it as Husk) }
        registerMob(
            DROWNED,
            HollowNecromancer::class.java
        ) { HollowNecromancer(it as Drowned) }
        registerMob(
            SKELETON,
            HollowThrall::class.java
        ) { HollowThrall(it as Skeleton) }
        registerMob(
            WITHER_SKELETON,
            HollowHarbinger::class.java
        ) { HollowHarbinger(it as WitherSkeleton) }*/
    }
}