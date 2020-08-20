package org.popcraft.chunky.shape;

import org.popcraft.chunky.ChunkCoordinate;
import org.popcraft.chunky.iterator.ChunkIterator;

public class Star extends AbstractShape {
    private double p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z, p5x, p5z;

    public Star(ChunkIterator chunkIterator) {
        super(chunkIterator);
        this.p1x = xCenter + radius * Math.cos(36);
        this.p1z = zCenter + radius * Math.sin(36);
        this.p2x = xCenter + radius * Math.cos(108);
        this.p2z = zCenter + radius * Math.sin(108);
        this.p3x = xCenter + radius * Math.cos(180);
        this.p3z = zCenter + radius * Math.sin(180);
        this.p4x = xCenter + radius * Math.cos(252);
        this.p4z = zCenter + radius * Math.sin(252);
        this.p5x = xCenter + radius * Math.cos(324);
        this.p5z = zCenter + radius * Math.sin(324);
    }

    @Override
    public boolean isBounding(ChunkCoordinate chunkCoordinate) {
        int xChunk = (chunkCoordinate.x << 4) + 8;
        int zChunk = (chunkCoordinate.z << 4) + 8;
        boolean inside13 = insideLine(p1x, p1z, p3x, p3z, xChunk, zChunk);
        boolean inside24 = insideLine(p2x, p2z, p4x, p4z, xChunk, zChunk);
        boolean inside35 = insideLine(p3x, p3z, p5x, p5z, xChunk, zChunk);
        boolean inside41 = insideLine(p4x, p4z, p1x, p1z, xChunk, zChunk);
//        if (inside13 && inside24 && inside35 && inside41) {
//            System.out.println("inside 1");
//            return true;
//        }
        boolean inside52 = insideLine(p5x, p5z, p2x, p2z, xChunk, zChunk);
        return !inside13 && !inside24 && !inside35 && !inside41 && !inside52;
//        if (inside24 && !inside13 && inside52) {
//            System.out.println("inside 2");
//            return true;
//        }
//        if (inside35 && !inside24 && inside13) {
//            System.out.println("inside 3");
//            return true;
//        }
//        if (inside41 && !inside35 && inside24) {
//            System.out.println("inside 4");
//            return true;
//        }
//        if (inside52 && !inside41 && inside35) {
//            System.out.println("inside 5");
//            return true;
//        }
    }
}
