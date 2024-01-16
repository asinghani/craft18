package xyz.gary600.craft18_plugin

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object PortCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            for ((i, location) in Craft18Plugin.plugin.config.ports.withIndex()) {
                if (location != null) {
                    sender.sendMessage("Port $i: ${location.blockX} ${location.blockY} ${location.blockZ}")
                }
                else {
                    sender.sendMessage("Port $i: unconnected")
                }
            }
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("Caller must be a player")
            return false
        }
        if (args.size != 1) {
            sender.sendMessage("Must specify port")
            return false
        }
        val port = args[0].toIntOrNull()
        if (port == null || port < 0 || port > 7) {
            sender.sendMessage("Invalid port")
            return false
        }
        val block = sender.getTargetBlock(null, 20)
        if (block.type == Material.AIR) {
            sender.sendMessage("Must target a block")
            return false
        }
        Craft18Plugin.plugin.config.ports[port] = block.location
        Craft18Plugin.plugin.config.save()
        sender.sendMessage("Set port $port to block ${block.location.blockX} ${block.location.blockY} ${block.location.blockZ}")
        return true
    }
}

object SerialCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 1) {
            sender.sendMessage("No port specified")
            return false
        }
        Craft18Plugin.plugin.serial?.closePort()
        try {
            Craft18Plugin.plugin.serial = SerialPort.getCommPort(args[0]).apply {
                baudRate = 115200
                openPort()
            }
        }
        catch (e: SerialPortInvalidPortException) {
            sender.sendMessage("Bad port")
            return false
        }
        sender.sendMessage("Set serial port to ${args[0]}")
        return true
    }
}