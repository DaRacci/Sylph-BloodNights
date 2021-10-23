package me.racci.bloodnight.util


object Permissions {
    const val BASE = "bloodnight"

    object Admin {
        const val ADMIN = "$BASE.admin"
        const val SPAWN_MOB = "$ADMIN.spawnmob"
        const val RELOAD = "$ADMIN.reload"
        const val MANAGE_DEATH_ACTION = "$ADMIN.managedeathactions"
        const val MANAGE_WORLDS = "$ADMIN.manageworlds"
        const val MANAGE_NIGHT = "$ADMIN.managenight"
        const val MANAGE_MOBS = "$ADMIN.managemobs"
        const val MANAGE_MOB = "$ADMIN.managemob"
        const val FORCE_NIGHT = "$ADMIN.forcenight"
        const val CANCEL_NIGHT = "$ADMIN.cancelnight"
    }

    object Bypass {
        const val BYPASS = "$BASE.bypass"
        const val COMMAND_BLOCK = "$BYPASS.blockedcommands"
    }
}