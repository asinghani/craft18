# Craft18

Goal: Re-create Minecraft Redstone components in the physical world using _only_ analog circuits, no microcontrollers or CPUs. Then, bridge the "physical-world" Redstone into the game using a custom plugin and Arduino USB-GPIO bridge, while maintaining the ability for "physical-world" Redstone to operate fully independently from the game as well.

## Building the server plugin

Make sure you have Java 17, go into `craft18_plugin` and run `./gradlew shadowJar` to build the plugin.
To run the server automatically, put the spigot jar in `/craft18_plugin/build/server/spigot.jar` and
run `./gradlew runSpigot`.
