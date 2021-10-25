package me.racci.bloodnight.specialmobs.mobs.spider

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil
import me.racci.bloodnight.specialmobs.mobs.abstractmobs.AbstractSpiderRider

class BlazeRider(carrier: Mob?) : AbstractSpiderRider(carrier, SpecialMobUtil.spawnAndMount(carrier, EntityType.BLAZE))