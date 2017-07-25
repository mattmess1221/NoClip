package mnm.plugins.noclip

import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.Sponge
import org.spongepowered.api.service.ServiceManager
import java.util.*
import kotlin.reflect.KClass

val sponge get() = Sponge.getGame()

fun <T> Optional<T>.orNull(): T? = this.orElse(null)

operator fun <T : Any> ServiceManager.get(service: KClass<T>): T? = this.provide(service.java).orNull()
