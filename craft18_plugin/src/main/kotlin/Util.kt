package xyz.gary600.craft18_plugin

import kotlinx.serialization.*
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Bukkit
import org.bukkit.Location

object LocationSerializer : KSerializer<Location> {
    private val delegateSerializer = IntArraySerializer()
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor("Location", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: Location) {
        encoder.encodeSerializableValue(delegateSerializer, intArrayOf(value.blockX, value.blockY, value.blockZ))
    }

    override fun deserialize(decoder: Decoder): Location {
        val arr = decoder.decodeSerializableValue(delegateSerializer)
        if (arr.size != 3) throw SerializationException("invalid Location")
        return Location(Bukkit.getWorlds()[0], arr[0].toDouble(), arr[1].toDouble(), arr[2].toDouble())
    }
}

@Serializable
class Config(
    var ports: Array<@Serializable(with = LocationSerializer::class) Location?> = arrayOfNulls(5)
) {
    fun save() {
        if (!Craft18Plugin.plugin.dataFolder.exists()) Craft18Plugin.plugin.dataFolder.mkdirs()
        Craft18Plugin.plugin.configFile.writeText(Craft18Plugin.json.encodeToString(this))
    }

    companion object {
        fun load(): Config {
            return if (!Craft18Plugin.plugin.configFile.exists())
                Config().also { it.save() } // default
            else
                Craft18Plugin.json.decodeFromString(Craft18Plugin.plugin.configFile.readText())
        }
    }
}