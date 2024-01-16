package xyz.gary600.craft18_plugin

import com.fazecast.jSerialComm.SerialPort
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Craft18Plugin : JavaPlugin() {
    val configFile = File(dataFolder, "config.json")

    var configInner: Config? = null
    val config: Config get() = configInner ?: Config.load().also { configInner = it }

    var serial: SerialPort? = null
    var portsState: Array<Boolean> = Array(8) { false }

    init {
        if (plugin_internal != null) {
            throw IllegalStateException("Plugin already initialized")
        }
        plugin_internal = this
    }

    override fun onEnable() {
        getCommand("port")!!.setExecutor(PortCommand)
        getCommand("serial")!!.setExecutor(SerialCommand)
        Bukkit.getScheduler().runTaskTimer(this, Runnable { updatePortsTask() }, 0, 1)
    }

    override fun onDisable() {
        serial?.closePort()
        serial = null
    }

    // Runs each tick
    private fun updatePortsTask() {
        if (serial == null || !serial!!.isOpen) { return }
        // Receive latest state
        var newState = false
        val buf = ByteArray(1)
        while (serial!!.bytesAvailable() > 0) {
            serial!!.readBytes(buf, 1);
            newState = true
        }
        if (newState) {
            portsState = (0..7).map { (buf[0].toInt() and (1 shl it)) != 0 }.toTypedArray()
        }

        config.ports.zip(portsState).forEach { (port, inVal) ->
            if (port != null) {
                // Make sure the port is set up as an amethyst block and obsidian
                if (port.block.type != Material.AMETHYST_BLOCK) {
                    port.block.type = Material.AMETHYST_BLOCK
                }
                val obsidian = port.clone().add(0.0, -2.0, 0.0).block

                if (obsidian.type != Material.OBSIDIAN) {
                    obsidian.type = Material.OBSIDIAN
                }

                // Power source selection
                val targetType = if (inVal) {
                    Material.REDSTONE_TORCH
                }
                else {
                    Material.AIR
                }
                val torchBlock = port.clone().add(0.0, -1.0, 0.0).block
                if (torchBlock.type != targetType) {
                    torchBlock.type = targetType
                }
            }
        }

        // Send read state
        serial!!.writeBytes(byteArrayOf(
            (0..7)
                // If this port is powered and it's not powered by the other side, output it as powered
                .map { it to if ((config.ports[it]?.block?.run { isBlockPowered } == true) && !portsState[it]) { 1 } else { 0 } }
                .fold(0) { a, (o, bit) -> a or (bit shl o) }.toByte()
        ), 1)

    }

    companion object {
        private var plugin_internal: Craft18Plugin? = null;
        val plugin: Craft18Plugin get() = plugin_internal ?: throw IllegalStateException("Plugin not initialized")
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }
}