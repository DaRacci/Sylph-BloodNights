package me.racci.bloodnight.util

import org.bukkit.Location
import org.bukkit.util.Vector

object VectorUtil {

    fun getDirectionVector(start: Location, target: Location): Vector {
        return getDirectionVector(start.toVector(), target.toVector())
    }

    private fun getDirectionVector(start: Vector, target: Vector): Vector {
        return Vector(target.x - start.x, target.y - start.y, target.z - start.z)
    }

}