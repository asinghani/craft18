package xyz.gary600.craft18_plugin

import com.fazecast.jSerialComm.SerialPort
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Craft18Plugin : JavaPlugin() {
    val configFile = File(dataFolder, "config.json")

    private var configInner: Config? = null
    val config: Config get() = configInner ?: Config.load().also { configInner = it }

    var serial: SerialPort? = null
    private var portsState: Array<Boolean> = Array(8) { false }

    private var ticksSinceUpdate = 0

    init {
        if (plugin_internal != null) throw IllegalStateException("Plugin already initialized")
        plugin_internal = this
    }

    override fun onEnable() {
        getCommand("port")!!.setExecutor(PortCommand)
        getCommand("serial")!!.setExecutor(SerialCommand)
        // Update signals on every tick
        Bukkit.getScheduler().runTaskTimer(this, Runnable { updatePortsTask() }, 0, 1)
        // Every 5s check if remote is not responding
        Bukkit.getScheduler().runTaskTimer(this, Runnable {
            if (serial?.isOpen == true) ticksSinceUpdate += 1
            else ticksSinceUpdate = 0
            if (ticksSinceUpdate >= 100 && ticksSinceUpdate % 100 == 0)
                Bukkit.broadcastMessage("Craft18: Remote not responding for $ticksSinceUpdate ticks...")
        }, 0, 1)
    }

    override fun onDisable() {
        serial?.closePort()
        serial = null
    }

    // Runs each tick
    private fun updatePortsTask() {
        if (serial == null || !serial!!.isOpen) return
        // Receive latest state
        var newState = false
        val buf = ByteArray(1)
        while (serial!!.bytesAvailable() > 0) {
            serial!!.readBytes(buf, 1)
            newState = true
            ticksSinceUpdate = 0
        }
        // Split the bits out into an Array<Boolean>
        if (newState) portsState = (0..4).map { (buf[0].toInt() and (1 shl it)) != 0 }.toTypedArray()

        config.ports.zip(portsState).forEachIndexed { i, (port, inVal) ->
            if (port != null) {
                // Make sure the port is set up as an amethyst block and obsidian
                if (port.block.type != Material.AMETHYST_BLOCK) port.block.type = Material.AMETHYST_BLOCK
                val obsidian = port.clone().add(0.0, -2.0, 0.0).block
                if (obsidian.type != Material.OBSIDIAN) obsidian.type = Material.OBSIDIAN

                // Power source selection
                val torchBlock = port.clone().add(0.0, -1.0, 0.0).block
                val targetType = if (inVal) Material.REDSTONE_TORCH else Material.AIR
                if (torchBlock.type != targetType) torchBlock.type = targetType

                // Move/summon nametag indicator
                val labelLoc = port.clone().add(0.5, 1.2, 0.5)
                port.world!!.entities.filter {
                    it.scoreboardTags.contains("craft18_$i")
                            && it is ArmorStand
                }.getOrNull(0).let {
                    val color = if (inVal) "c" else "4"
                    val state = if (inVal) "ON" else "OFF"
                    val label = "§b§${color}Port $i: $state§r"
                    // Update label if it exists
                    it?.apply {
                        teleport(labelLoc)
                        customName = label
                    }
                    // Summon label if doesn't
                        ?: port.world!!.spawn(labelLoc, ArmorStand::class.java).apply {
                            customName = label
                            isCustomNameVisible = true
                            isVisible = false
                            isMarker = true
                            addScoreboardTag("craft18_$i")
                        }
                }
            }
        }

        // Send read state
        serial!!.writeBytes(byteArrayOf(
            (0..4)
                // If this port is powered and it's not powered by the other side, output it as powered
                .map {
                    it to if ((config.ports[it]?.block?.isBlockPowered == true) && !portsState[it]) 1 else 0
                }.fold(0) { a, (o, bit) -> a or (bit shl o) }.toByte()
        ), 1
        )

    }

    companion object {
        private var plugin_internal: Craft18Plugin? = null
        val plugin: Craft18Plugin get() = plugin_internal ?: throw IllegalStateException("Plugin not initialized")
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }
}