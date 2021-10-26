package me.racci.bloodnight.specialmobs.mobs.events

import com.willfp.ecoenchants.enchantments.EcoEnchants
import me.racci.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings
import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.*
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractSkeleton
import me.racci.bloodnight.specialmobs.mobs.events.HollowsEve2021.attributeModifier
import me.racci.bloodnight.specialmobs.mobs.events.HollowsEve2021.dropChances
import me.racci.bloodnight.specialmobs.mobs.events.HollowsEve2021.hollowsKey
import me.racci.bloodnight.util.uuidBossBarNamespace
import me.racci.hollowseve.enums.HollowsEve2021
import me.racci.raccicore.utils.items.builders.ItemBuilder
import me.racci.raccicore.utils.now
import me.racci.raccicore.utils.strings.colour
import me.racci.raccicore.utils.strings.colouredTextOf
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.enchantments.Enchantment.*
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import su.nightexpress.goldencrates.api.GoldenCratesAPI
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.floor

object HollowsEve2021 {

    val hollowsKey: ItemStack
        get() = GoldenCratesAPI.getKeyManager().getKeyById("hollowseve")!!.item

    fun dropChances(e: EntityEquipment?) {
        if (e == null) return
        e.helmetDropChance = 0F
        e.chestplateDropChance = 0F
        e.leggingsDropChance = 0F
        e.bootsDropChance = 0F
        e.itemInMainHandDropChance = 0F
        e.itemInOffHandDropChance = 0F
    }

    fun attributeModifier(
        entity: LivingEntity,
        attribute: Attribute,
        amount: Double,
        operation: AttributeModifier.Operation = AttributeModifier.Operation.ADD_SCALAR
    ) {
        entity.getAttribute(attribute)?.addModifier(AttributeModifier(UUID.randomUUID(), "hollow", amount, operation))
    }

}


class HollowAdventurer(entity: Zombie) : AbstractZombie(entity) {

    init {
        attributeModifier(entity, Attribute.GENERIC_MOVEMENT_SPEED, 50.0)
        attributeModifier(entity, Attribute.GENERIC_MAX_HEALTH, 40.0)
        attributeModifier(entity, Attribute.GENERIC_FOLLOW_RANGE, 150.0)
        var i = 0
        val r = ThreadLocalRandom.current()
        entity.equipment.apply {
            while (i < 2) {
                val s = r.nextInt(1, 4)
                val armour = armorContents[s]
                if (armour.type != Material.AIR) {
                    when (s) {
                        1 -> helmet = ItemStack(Material.IRON_HELMET)
                        2 -> chestplate = ItemStack(Material.IRON_CHESTPLATE)
                        3 -> leggings = ItemStack(Material.IRON_LEGGINGS)
                        else -> boots = ItemStack(Material.IRON_BOOTS)
                    }
                    i++
                } else continue
            }
            setItemInMainHand(ItemStack(Material.GOLDEN_SWORD))
        }
        SpecialMobUtil.spawnParticlesAround(entity, Particle.ASH, 10)
    }
}

class HollowArcher(entity: Skeleton) : AbstractSkeleton(entity) {

    private var r = ThreadLocalRandom.current()

    init {
        attributeModifier(entity, Attribute.GENERIC_MOVEMENT_SPEED, 50.0)
        attributeModifier(entity, Attribute.GENERIC_MAX_HEALTH, 40.0)
        attributeModifier(entity, Attribute.GENERIC_FOLLOW_RANGE, 150.0)
        attributeModifier(entity, Attribute.GENERIC_ARMOR, 50.0, AttributeModifier.Operation.ADD_NUMBER)

        entity.equipment.apply {
            helmet = ItemBuilder.from(Material.IRON_HELMET)
                .enchant(PROTECTION_PROJECTILE, 3)
                .build()
            setItemInMainHand(
                ItemBuilder.from(Material.BOW)
                    .enchant(ARROW_DAMAGE, 8)
                    .enchant(ARROW_KNOCKBACK, 2)
                    .build()
            )
        }
    }

    override fun onProjectileHit(event: ProjectileHitEvent) {
        if (event.hitEntity == null) return
        if (25 < r.nextInt(101)) return
        event.hitEntity!!.freezeTicks = r.nextInt(80, 120)
    }
}

class HollowHaunter(carrier: Phantom) :
    AbstractExtendedPhantom<Phantom, Stray>(carrier, SpecialMobUtil.spawnAndMount(carrier, HollowRider::class.java)) {

    init {
        carrier.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = 70.0
        attributeModifier(carrier, Attribute.GENERIC_MOVEMENT_SPEED, 250.0)
        attributeModifier(carrier, Attribute.GENERIC_FOLLOW_RANGE, 200.0)
    }

    override fun onExtensionDeath(event: EntityDeathEvent) {
        (if (event.entity == baseEntity) {
            passenger
        } else baseEntity).remove()
    }

    override fun onTargetEvent(event: EntityTargetEvent) {
        if (event.target != null) {
            SpecialMobUtil.launchProjectileOnTarget(baseEntity, event.target, SpectralArrow::class.java, 3.0)
        }
    }


}

class HollowRider(entity: Stray) : AbstractStray(entity) {

    init {
        val a = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!
        a.baseValue = 70.0; entity.health = a.value
        attributeModifier(entity, Attribute.GENERIC_FOLLOW_RANGE, 200.0)
        entity.equipment.apply {
            setItemInMainHand(
                ItemBuilder.from(Material.BOW)
                    .enchant(ARROW_DAMAGE, 3)
                    .enchant(EcoEnchants.AIMING)
                    .enchant(EcoEnchants.TRIPLESHOT)
                    .build()
            )
            dropChances(this)
        }
    }

    override fun onProjectileHit(event: ProjectileHitEvent) {
        (event.hitEntity as? LivingEntity)?.addPotionEffect(PotionEffect(PotionEffectType.POISON, 100, 3))
    }

}

class HollowGoliath(entity: Husk) : AbstractZombie(entity) {

    init {
        attributeModifier(entity, Attribute.GENERIC_MOVEMENT_SPEED, -20.0)
        attributeModifier(entity, Attribute.GENERIC_MAX_HEALTH, 300.0)
        attributeModifier(entity, Attribute.GENERIC_ARMOR, 10.0, AttributeModifier.Operation.ADD_NUMBER)
        attributeModifier(entity, Attribute.GENERIC_FOLLOW_RANGE, 300.0)
        entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)!!.baseValue = 0.0
        entity.equipment.apply {
            helmet = ItemStack(Material.NETHERITE_HELMET)
            chestplate = ItemStack(Material.NETHERITE_CHESTPLATE)
            leggings = ItemStack(Material.NETHERITE_LEGGINGS)
            boots = ItemStack(Material.NETHERITE_BOOTS)
            setItemInMainHand(
                ItemBuilder.from(Material.DIAMOND_AXE)
                    .enchant(DAMAGE_ALL, 6)
                    .enchant(KNOCKBACK, 4)
                    .build()
            )
        }
    }
}

class HollowNecromancer(entity: Drowned) : AbstractZombie(entity) {

    private var tracker = false
    private val rand = ThreadLocalRandom.current()

    init {
        attributeModifier(entity, Attribute.GENERIC_MAX_HEALTH, 250.0)
        entity.equipment.apply {
            helmet = ItemStack(Material.WITHER_SKELETON_SKULL)
            setItemInMainHand(ItemStack(Material.BONE))
        }
    }

    override fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        if (tracker) {
            tracker = false; return
        }
        val loc = event.damager.location.clone()
        val fuzz = Vector(rand.nextDouble(-5.0, 5.0), 0.0, rand.nextDouble(-5.0, 5.0))
        val var1 = loc.world.getBlockAt(loc.add(fuzz))
        val var2 = var1.getRelative(0, 1, 0)
        if (var1.type != Material.AIR || var2.type != Material.AIR) return
        tracker = true
        val thrall = SpecialMobUtil.spawnMinion<Skeleton>(event.entity, HollowThrall::class.java, var1.location)
        SpecialMobUtil.spawnParticlesAround(thrall.location, Particle.CAMPFIRE_SIGNAL_SMOKE, 10)
        thrall.playEffect(EntityEffect.ENTITY_POOF)
    }

    override fun onHit(event: EntityDamageByEntityEvent) {
        val healAmount = event.finalDamage * 3.0
        (event.damager as LivingEntity).health += healAmount
    }

}

class HollowThrall(entity: Skeleton) : AbstractSkeleton(entity) {

    init {
        entity.equipment.apply {
            helmet = ItemStack(Material.ZOMBIE_HEAD)
            setItemInMainHand(ItemStack(Material.STONE_SHOVEL))
        }
        val a = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!
        a.baseValue = 40.0; entity.health = a.value
        dropChances(entity.equipment)
    }

}

class HollowHarbinger(entity: WitherSkeleton) : AbstractWitherSkeleton(entity) {

    private val r = ThreadLocalRandom.current()
    private var lastPlayerCheck = now()
    private var lastDamager: Player? = null
    private var lastTeleport: Long = 0
    private val bossBar: BossBar = Bukkit.createBossBar(
        uuidBossBarNamespace(baseEntity),
        colour("&4Hollow Harbinger"),
        BarColor.PURPLE,
        BarStyle.SOLID
    )

    init {
        bossBar.progress = 100.0
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = 1000.0
        entity.getAttribute(Attribute.GENERIC_ARMOR)!!.baseValue = 10.0
        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.baseValue = 20.0
        attributeModifier(entity, Attribute.GENERIC_FOLLOW_RANGE, 150.0)
        entity.equipment.apply {
            helmet = ItemStack(Material.NETHERITE_HELMET)
            chestplate = ItemStack(Material.NETHERITE_CHESTPLATE)
            leggings = ItemStack(Material.NETHERITE_LEGGINGS)
            boots = ItemStack(Material.NETHERITE_BOOTS)
            setItemInMainHand(ItemStack(Material.NETHERITE_SWORD))
        }
        SpecialMobUtil.spawnParticlesAround(entity, Particle.ASH, 10)
    }

    override fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val entity = event.entity as? LivingEntity ?: return
        lastDamager = if (damager is AbstractArrow && damager.shooter is Player) {
            damager.shooter as Player
        } else damager as Player
        var hp = entity.health - event.finalDamage
        if (hp < 0.0) hp = 0.0
        println(hp)
        hp = floor(hp * 100.0) / 100.0
        println(hp)
        hp = hp * 100.0 / entity.maxHealth
        println(hp)
        bossBar.progress = hp
    }

    override fun onKill(event: EntityDeathEvent) {
        val player = event.entity as? Player ?: return
        bossBar.removePlayer(player)
    }

    override fun onDeath(event: EntityDeathEvent) {
        bossBar.removeAll()
        event.deathSound = Sound.ENTITY_WITHER_DEATH
        event.drops.add(hollowsKey.asQuantity(r.nextInt(1, 4)))
        if (15 < r.nextInt(101)) event.drops.add(me.racci.hollowseve.factories.ItemFactory[HollowsEve2021.ONCE_HOLY_SABER])
        Bukkit.broadcast(
            colouredTextOf(
                "#ffaa00&lS#ffbf15&ly#ffd52b&ll#ffea40&lp#ffff55&lh &f&l» &cA &4Harbinger &chas been slain!",
            )
        )
        SpecialMobUtil.dispatchShockwave(ShockwaveSettings(), event.entity.location)
    }

    override fun remove() {
        bossBar.removeAll()
        super.remove()
    }

    override fun tick() {
        if (now() - lastPlayerCheck > 5) {
            lastPlayerCheck = now()
            val nearbyEntities = baseEntity.getNearbyEntities(50.0, 50.0, 50.0)
            for (it in nearbyEntities) {
                if (it !is Player) continue
                if (it in bossBar.players) continue
                bossBar::addPlayer
            }
            for (it in bossBar.players) {
                if (it in nearbyEntities) continue
                bossBar::removePlayer
            }
        }
        val player = lastDamager ?: return
        if (now() - lastTeleport > 15) {
            if (baseEntity.location.distance(player.location) > 50.0) {
                lastDamager = null; return
            }
            lastTeleport = now()
            val loc = player.location
            loc.add(player.eyeLocation.direction.normalize().multiply(-0.7)).add(0.0, 0.5, 0.0)
            baseEntity.target = player
            baseEntity.world.playSound(loc, Sound.ENTITY_GHAST_SCREAM, 1.0f, 1.0f)
            baseEntity.world.playSound(loc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f)
        }
    }


}