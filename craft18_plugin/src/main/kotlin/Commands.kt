package xyz.gary600.craft18_plugin

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun Location.coordsString() = "$blockX $blockY $blockZ"

object PortCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            for ((i, location) in Craft18Plugin.plugin.config.ports.withIndex()) {
                if (location != null) {
                    sender.sendMessage("Port $i: ${location.coordsString()}")
                }
                else {
                    sender.sendMessage("Port $i: unconnected")
                }
            }
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("Caller must be a player")
            return true
        }
        if (args.size > 2) {
            sender.sendMessage("Unexpected argument")
            return true
        }
        val port = args[0].toIntOrNull()
        if (port == null || port < 0 || port > 7) {
            sender.sendMessage("Invalid port")
            return true
        }
        if (args.size == 2) {
            if (args[1] == "clear") {
                Craft18Plugin.plugin.config.ports[port] = null
                Craft18Plugin.plugin.config.save()
                sender.sendMessage("Cleared port $port")
                return true
            } else {
                sender.sendMessage("Expected 'clear'")
                return true
            }
        }
        val block = sender.getTargetBlock(null, 20)
        if (block.type == Material.AIR) {
            sender.sendMessage("Must target a block")
            return true
        }
        Craft18Plugin.plugin.config.ports[port] = block.location
        Craft18Plugin.plugin.config.save()
        sender.sendMessage("Set port $port to block ${block.location.coordsString()}")
        return true
    }
}

object SerialCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Serial port: ${Craft18Plugin.plugin.serial?.systemPortName}")
            return true
        }
        if (args.size > 1) {
            sender.sendMessage("Invalid command")
            return true
        }
        Craft18Plugin.plugin.serial?.closePort()
        if (args[0] == "close") {
            sender.sendMessage("Closed serial port")
            return true
        }
        try {
            Craft18Plugin.plugin.serial = SerialPort.getCommPort(args[0]).apply {
                baudRate = 115200
                openPort()
            }
        }
        catch (e: SerialPortInvalidPortException) {
            sender.sendMessage("Bad port")
            return true
        }
        if (Craft18Plugin.plugin.serial?.isOpen != true) {
            sender.sendMessage("Failed to open port")
            return true
        }
        sender.sendMessage("Set serial port to ${args[0]}")
        return true
    }
}