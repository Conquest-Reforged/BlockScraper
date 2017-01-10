package me.dags.scraper.adapter;

import me.dags.scraper.TextureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.dynmap.modsupport.*;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public abstract class Adapter<T extends BlockModel> {

    public void apply(ModTextureDefinition definition, String name, IBlockState blockState, int meta) {
        getModel(definition.getModelDefinition(), name).setMetaValue(meta);

        BlockTextureRecord record = definition.addBlockTextureRecord(name);
        record.setMetaValue(meta);

        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(blockState);
        for (EnumFacing facing : EnumFacing.values()) {
            BlockSide side = sideOf(facing);
            if (side != null) {
                List<BakedQuad> quads = model.getQuads(blockState, facing, 0L);
                if (quads.size() > 0) {
                    TextureAtlasSprite sprite = quads.get(0).getSprite();
                    TextureFile textureFile = TextureHelper.getTexture(definition, sprite);
                    record.setSideTexture(textureFile, side);
                }
            }
        }
    }

    abstract T getModel(ModModelDefinition definition, String name);

    private static BlockSide sideOf(EnumFacing facing) {
        switch (facing) {
            case UP:
                return BlockSide.TOP;
            case DOWN:
                return BlockSide.BOTTOM;
            case NORTH:
                return BlockSide.NORTH;
            case EAST:
                return BlockSide.EAST;
            case SOUTH:
                return BlockSide.SOUTH;
            case WEST:
                return BlockSide.WEST;
            default:
                return BlockSide.ALLFACES;
        }
    }
}
