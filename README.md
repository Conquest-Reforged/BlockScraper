# BlockScraper

Generate dynmap renderdata for mod blocks

## Info
This scraper works on the assumption that the given mod(s) use the standard
 blockstate and blockmodel json formats.
It will automatically attempt to build a dynmap model definition from a
 given blockstate's model data.

Alternatively, a preset model can be specified by adding the field
 `"dyn_model": "<preset>",` to the blockstate json.

### Usage
This mod can be used client-side or server-side to generate the dynmap data
 \- in both cases, dynmap-forge must also be installed.

All data is ouput to dynmap's config folder found in the root of the game/server
 directory (namely `dynmap/renderdata` and `dynmap/texturepacks`).  
On the client the data is generated when entering a world.  
On the server, the data is generated on each start-up.  

### Available Presets
The following presets assume the mod block shares the same metadata as
 the vanilla equivalent (rotation data etc.)
- `"door"`
- `"fence"`
- `"pane"`
- `"plant"`
- `"wall"`
