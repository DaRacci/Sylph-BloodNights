package me.racci.bloodnight.specialmobs.mobs.spider

import me.racci.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractSpiderRider
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob

class BlazeRider(carrier: Mob) : AbstractSpiderRider(carrier, SpecialMobUtil.spawnAndMount(carrier, EntityType.BLAZE))