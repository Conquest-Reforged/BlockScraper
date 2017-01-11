package me.dags.scraper.dynmap;

import org.dynmap.modsupport.BlockSide;
import org.dynmap.modsupport.CuboidBlockModel;

import java.util.BitSet;

/**
 * @author dags <dags@dags.me>
 */
public class Cuboid {
    private static final int DOWN = 0, UP = 1, NORTH = 2, SOUTH = 3, EAST = 4, WEST = 5;

    private final double xmin, ymin, zmin;
    private final double xmax, ymax, zmax;
    private final int[] patches;

    private Cuboid(Builder builder) {
        this.xmin = Math.min(builder.xmin, builder.xmax);
        this.xmax = Math.max(builder.xmin, builder.xmax);
        this.ymin = Math.min(builder.ymin, builder.ymax);
        this.ymax = Math.max(builder.ymin, builder.ymax);
        this.zmin = Math.min(builder.zmin, builder.zmax);
        this.zmax = Math.max(builder.zmin, builder.zmax);
        int count = 0;
        for (int i = 0; i < 6; i++) {
            if (builder.faces.get(i)) {
                count++;
            }
        }
        this.patches = new int[count];
        for (int i = 0, j = 0; i < 6 && j  < patches.length; i++) {
            if (builder.faces.get(i)) {
                patches[j++] = i;
            }
        }
    }

    public void addToModel(CuboidBlockModel blockModel) {
        blockModel.addCuboid(xmin, ymin, zmin, xmax, ymax, zmax, patches);
    }

    public static class Builder {

        private double xmin, ymin, zmin;
        private double xmax, ymax, zmax;
        private final BitSet faces = new BitSet(6);

        public Builder min(int x, int y, int z) {
            this.xmin = x / 16D;
            this.ymin = y / 16D;
            this.zmin = z / 16D;
            return this;
        }

        public Builder max(int x, int y, int z) {
            this.xmax = x / 16D;
            this.ymax = y / 16D;
            this.zmax = z / 16D;
            return this;
        }

        public Builder all() {
            for (int i = DOWN; i < 5; i++) {
                faces.set(i, true);
            }
            return this;
        }

        public Builder sides() {
            for (int i = NORTH; i < 5; i++) {
                faces.set(i, true);
            }
            return this;
        }

        public Builder bottom() {
            faces.set(DOWN, true);
            return this;
        }

        public Builder top() {
            faces.set(UP, true);
            return this;
        }

        public Builder north() {
            faces.set(NORTH, true);
            return this;
        }

        public Builder south() {
            faces.set(SOUTH, true);
            return this;
        }

        public Builder east() {
            faces.set(EAST, true);
            return this;
        }

        public Builder west() {
            faces.set(WEST, true);
            return this;
        }

        public Builder side(BlockSide side) {
            switch (side) {
                case ALLFACES:
                    return all();
                case ALLSIDES:
                    return sides();
                case BOTTOM:
                    return bottom();
                case TOP:
                    return top();
                case NORTH:
                    return north();
                case SOUTH:
                    return south();
                case EAST:
                    return east();
                case WEST:
                    return west();
            }
            return this;
        }

        public Cuboid build() {
            return new Cuboid(this);
        }
    }
}
