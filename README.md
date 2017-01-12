# BlockScraper

Generate dynmap renderdata for mod blocks

## Info
This scraper works on the assumption that the given mod(s) use the standard
 blockstate and blockmodel json formats.
It will automatically attempt to build a dynmap model definition from a
 given blockstate's model data.

Alternatively, a preset model can be specified by adding the field
 `"dyn_model": "<preset>",` to the blockstae json.

### Available Presets:
The following presets assume the mod block shares the same metadata as
 the vanilla equivalent (rotation data etc.)
- `"door"`
- `"fence"`
- `"pane"`
- `"plant"`
- `"wall"`