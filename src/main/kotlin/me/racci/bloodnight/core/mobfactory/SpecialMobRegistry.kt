package me.racci.bloodnight.core.mobfactory

import me.racci.bloodnight.core.mobfactory.SpecialMobRegistry.registerMob
import me.racci.bloodnight.specialmobs.SpecialMob
import me.racci.bloodnight.specialmobs.mobs.creeper.*
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
import org.bukkit.entity.*
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Function
import kotlin.collections.HashMap
import kotlin.collections.HashSet

object SpecialMobRegistry {

    private val MOB_GROUPS      = HashMap<Class<out Entity>, MobGroup>()
    private val ENTITY_MAPPINGS = HashMap<Class<out Entity>, Class<out Entity>>()
    private val RAND            = ThreadLocalRandom.current()

    fun < T : SpecialMob<*> > registerMob(entityType: EntityType, clazz: Class<T>, factory: Function<LivingEntity, SpecialMob<*>>) {
        MOB_GROUPS.computeIfAbsent(entityType.entityClass!!) {MobGroup(entityType)}
            .registerFactory(clazz, factory)
    }

    fun getMobBaseClass(entity: Entity): Class<out Entity>? {
        if (entity.type == EntityType.UNKNOWN) return null
        return ENTITY_MAPPINGS.computeIfAbsent(entity.type.entityClass ?: return null) { c ->
            MOB_GROUPS.keys.firstOrNull{c.isAssignableFrom(it)}!!
        }

//        val mob = ENTITY_MAPPINGS.computeIfAbsent(entity.type.entityClass ?: return null) {
//            for (clazz in MOB_GROUPS.keys) {
//                if (clazz.isAssignableFrom(it)) {
//                    return@computeIfAbsent clazz
//                }
//            }
//            null
//        }
//        mob
    }

    fun getMobGroup(entity: Entity) =
        getMobBaseClass(entity).mapNotNull(MOB_GROUPS::get)

    fun getMobGroup(name: String) =
        MOB_GROUPS.entries.first{it.key.simpleName.equals(name, true)}

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
        registeredMobs.firstOrNull{it.mobName.equals(arg, true)}

    init {
        /*
        Initialize default mobs.
         */
        registerMob(
            EntityType.CREEPER,
            EnderCreeper::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> EnderCreeper(me.racci.bloodnight.core.mobfactory.e as Creeper?) })
        registerMob(
            EntityType.CREEPER,
            GhostCreeper::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> GhostCreeper(me.racci.bloodnight.core.mobfactory.e as Creeper?) })
        registerMob(
            EntityType.CREEPER,
            NervousPoweredCreeper::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> NervousPoweredCreeper(me.racci.bloodnight.core.mobfactory.e as Creeper?) })
        registerMob(
            EntityType.CREEPER,
            SpeedCreeper::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> SpeedCreeper(me.racci.bloodnight.core.mobfactory.e as Creeper?) })
        registerMob(
            EntityType.CREEPER,
            ToxicCreeper::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> ToxicCreeper(me.racci.bloodnight.core.mobfactory.e as Creeper?) })
        registerMob(
            EntityType.CREEPER,
            UnstableCreeper::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> UnstableCreeper(me.racci.bloodnight.core.mobfactory.e as Creeper?) })

        // Enderman
        registerMob(
            EntityType.ENDERMAN,
            FearfulEnderman::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> FearfulEnderman(me.racci.bloodnight.core.mobfactory.e as Enderman?) })
        registerMob(
            EntityType.ENDERMAN,
            ToxicEnderman::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> ToxicEnderman(me.racci.bloodnight.core.mobfactory.e as Enderman?) })

        // Phantom
        registerMob(
            EntityType.PHANTOM,
            FearfulPhantom::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> FearfulPhantom(me.racci.bloodnight.core.mobfactory.e as Phantom?) })
        registerMob(
            EntityType.PHANTOM,
            FirePhantom::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> FirePhantom(me.racci.bloodnight.core.mobfactory.e as Phantom?) })
        registerMob(
            EntityType.PHANTOM,
            PhantomSoul::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> PhantomSoul(me.racci.bloodnight.core.mobfactory.e as Phantom?) })

        // Rider
        registerMob(
            EntityType.SPIDER,
            BlazeRider::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> BlazeRider(me.racci.bloodnight.core.mobfactory.e as Spider?) })
        registerMob(
            EntityType.SPIDER,
            SpeedSkeletonRider::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> SpeedSkeletonRider(me.racci.bloodnight.core.mobfactory.e as Spider?) })
        registerMob(
            EntityType.SPIDER,
            WitherSkeletonRider::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> WitherSkeletonRider(me.racci.bloodnight.core.mobfactory.e as Spider?) })

        // Skeleton
        registerMob(
            EntityType.SKELETON,
            InvisibleSkeleton::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> InvisibleSkeleton(me.racci.bloodnight.core.mobfactory.e as Skeleton?) })
        registerMob(
            EntityType.SKELETON,
            MagicSkeleton::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> MagicSkeleton(me.racci.bloodnight.core.mobfactory.e as Skeleton?) })

        // Slime
        registerMob(
            EntityType.SLIME,
            ToxicSlime::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> ToxicSlime(me.racci.bloodnight.core.mobfactory.e as Slime?) })

        // Witch
        registerMob(
            EntityType.WITCH,
            FireWizard::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> FireWizard(me.racci.bloodnight.core.mobfactory.e as Witch?) })
        registerMob(
            EntityType.WITCH,
            ThunderWizard::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> ThunderWizard(me.racci.bloodnight.core.mobfactory.e as Witch?) })
        registerMob(
            EntityType.WITCH,
            WitherWizard::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> WitherWizard(me.racci.bloodnight.core.mobfactory.e as Witch?) })

        // Zombie
        registerMob(
            EntityType.ZOMBIE,
            ArmoredZombie::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> ArmoredZombie(me.racci.bloodnight.core.mobfactory.e as Zombie?) })
        registerMob(
            EntityType.ZOMBIE,
            InvisibleZombie::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> InvisibleZombie(me.racci.bloodnight.core.mobfactory.e as Zombie?) })
        registerMob(
            EntityType.ZOMBIE,
            SpeedZombie::class.java,
            Function<LivingEntity?, SpecialMob<*>?> { e: LivingEntity? -> SpeedZombie(me.racci.bloodnight.core.mobfactory.e as Zombie?) })
    }
}