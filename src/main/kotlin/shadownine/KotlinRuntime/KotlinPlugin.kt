package shadownine.KotlinRuntime

import net.risingworld.api.Plugin

fun formatPrint(message: String) {
    println("[${ModInfo.name}] $message")
}

@Suppress("UNUSED_PARAMETER")
open class KotlinPlugin : Plugin() {
    override fun onEnable() {
        formatPrint("[START] ${ModInfo.name} v${ModInfo.version} is starting up.")
        formatPrint("[OK] Plugin successfully loaded!")
    }

    override fun onDisable() {
        formatPrint("[STOP] Plugin disabled")
    }
}
