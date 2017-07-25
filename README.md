# NoClip
Enables NoClip on a sponge server via a client mod.
Get the mod [here](https://github.com/killjoy1221/NoClip-client)

## Installing
Copy the jar and depedencies into the `/mods/` folder. 

## Dependencies
These plugins are required for proper functionality.
| Plugin                                                    | Reason       |
|-----------------------------------------------------------|--------------|
|[Spotlin](https://ore.spongepowered.org/pxlpowered/Spotlin)|Kotlin library|
|[PacketGate](https://github.com/CrushedPixel/PacketGate)   |Packet events |

## Default Config
The config is located at `/config/noclip.conf`
```hocon
noclip {
    # Set to true to not announce no-clip presence to players without permission.
    hideFromPlayers=false
}
```

## Permissions
There is currently only one permission.

- **noclip.noclip** - allows noclip use

## Technical Internals
The plugin uses a raw plugin channel named `NOCLIP`. The channel contains a
single value of boolean type. If it is `true`, noclipping will be enabled. If
it is `false`, it will be disabled.
