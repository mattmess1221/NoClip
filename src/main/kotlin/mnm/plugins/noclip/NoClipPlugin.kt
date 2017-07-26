package mnm.plugins.noclip

import com.google.common.reflect.TypeToken
import com.google.inject.Inject
import eu.crushedpixel.sponge.packetgate.api.registry.PacketGate
import ninja.leaping.configurate.ConfigurationOptions
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import ninja.leaping.configurate.objectmapping.ObjectMappingException
import org.slf4j.Logger
import org.spongepowered.api.Platform
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.cause.entity.damage.DamageType
import org.spongepowered.api.event.cause.entity.damage.DamageTypes.*
import org.spongepowered.api.event.entity.DamageEntityEvent
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.event.game.state.GamePostInitializationEvent
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.event.network.ChannelRegistrationEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.network.ChannelBinding
import org.spongepowered.api.network.ChannelBuf
import org.spongepowered.api.network.PlayerConnection
import org.spongepowered.api.network.RemoteConnection
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.SpongeExecutorService
import java.util.*

@Plugin(id = "noclip",
        name = "NoClip",
        authors = arrayOf("killjoy1221"),
        description = "Allows NoClip via a client mod",
        dependencies = arrayOf(
                Dependency(id = "spotlin", version = "0.1.1"),
                Dependency(id = "PacketGate", version = "0.1.1")))
class NoClipPlugin
@Inject constructor(val logger: Logger,
                    val container: PluginContainer,
                    @DefaultConfig(sharedRoot = true)
                    val loader: ConfigurationLoader<CommentedConfigurationNode>) {

    val PREVENTABLE_DAMAGES = setOf(
            SUFFOCATE,
            VOID,
            CONTACT,
            FALL,
            CONTACT,
            // FLY_INTO_WALL,
            MAGMA
    )

    val CHANNEL = "NOCLIP"

    private lateinit var channel: ChannelBinding.RawDataChannel
    private lateinit var packets: PacketGate

    lateinit var config: Config

    lateinit var sync: SpongeExecutorService

    val noclippers = mutableSetOf<UUID>()

    @Listener
    fun preInit(event: GamePreInitializationEvent) {
        val node = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true))
        val type = TypeToken.of(Config::class.java)
        try {
            config = node.getNode("noclip").getValue(type, ::Config)
        } catch (e: ObjectMappingException) {
            logger.warn("Unable to load config", e)
            config = Config()
            node.getNode("noclip").setValue(type, config)
        }

        loader.save(node)

    }

    @Listener
    fun postInit(event: GamePostInitializationEvent) {

        sync = sponge.scheduler.createSyncExecutor(this)

        channel = sponge.channelRegistrar.createRawChannel(this, CHANNEL).also { it.addListener(this::onPacketListen) }

        packets = sponge.serviceManager[PacketGate::class] ?: throw IllegalStateException("PacketGate service has not started yet.")

        logger.info("{}-{} loaded successfully", container.name, container.version.orElse("unknown"))
    }

    fun onPacketListen(data: ChannelBuf, connection: RemoteConnection, side: Platform.Type) {
        val player = (connection as PlayerConnection).player

        if (hasPermission(player) && data.readBoolean()) {
            noclippers += player.uniqueId
        } else {
            noclippers -= player.uniqueId
        }

        logger.debug("{} {} noclip", player.name, if (player.uniqueId in noclippers) "enabled" else "disabled")
    }

    @Listener
    fun onRegister(event: ChannelRegistrationEvent.Register, @First player: Player) {
        if (event.channel == CHANNEL) {

            val perm = hasPermission(player)

            if (perm) {
                PacketPlayerListener.register(this, packets, player)
            }

            if (!config.hideFromPlayers or perm) {
                this.channel.sendTo(player, { it.writeBoolean(perm) })
            }

            logger.info("{} has joined with NoClip. Has permission? {}.", player.name, perm)
        }
    }

    @Listener
    fun onDamage(e: DamageEntityEvent, @First damage: DamageType) {
        if (checkProtection(e.targetEntity, damage)) {
            e.isCancelled = true
        }
    }

    @Listener
    fun onQuit(e: ClientConnectionEvent.Disconnect) {
        this.noclippers -= e.targetEntity.uniqueId

        // TODO unregister packet listeners (possible mem leak)
        // preferably, PacketGate should unregister a connection's listeners when it disconnects.
    }

    private fun hasPermission(player: Player) = player.hasPermission("noclip.noclip")

    private fun checkProtection(player: Entity, cause: DamageType): Boolean {
        return player is Player && player.uniqueId in noclippers && cause in PREVENTABLE_DAMAGES
    }

}
