package mnm.plugins.noclip

import eu.crushedpixel.sponge.packetgate.api.event.PacketEvent
import eu.crushedpixel.sponge.packetgate.api.listener.PacketListener
import eu.crushedpixel.sponge.packetgate.api.listener.PacketListenerAdapter
import eu.crushedpixel.sponge.packetgate.api.registry.PacketConnection
import eu.crushedpixel.sponge.packetgate.api.registry.PacketGate
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.play.client.CPacketPlayer
import org.spongepowered.api.entity.living.player.Player

class PacketPlayerListener private constructor(val plugin: NoClipPlugin) : PacketListenerAdapter() {

    override fun onPacketRead(event: PacketEvent, connection: PacketConnection) {
        if (event.packet is CPacketPlayer) {
            val player = sponge.server.getPlayer(connection.playerUUID).get()
            if (player is EntityPlayerMP) {
                plugin.sync.submit {
                    val noclip = connection.playerUUID in plugin.noclippers
                    player.noClip = player.noClip or noclip
                }
            }
        }
    }

    companion object {

        fun register(plugin: NoClipPlugin, packets: PacketGate, player: Player) {

            packets.registerListener(
                    PacketPlayerListener(plugin),
                    PacketListener.ListenerPriority.DEFAULT,
                    packets.connectionByPlayer(player).orNull()!!,

                    CPacketPlayer::class.java,
                    CPacketPlayer.Position::class.java,
                    CPacketPlayer.PositionRotation::class.java)
        }

    }

}
