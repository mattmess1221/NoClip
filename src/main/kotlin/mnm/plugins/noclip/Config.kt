package mnm.plugins.noclip

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

@ConfigSerializable
class Config {

    @Setting(comment = "Set to true to not announce no-clip presence to players without permission.")
    var hideFromPlayers = false


}
