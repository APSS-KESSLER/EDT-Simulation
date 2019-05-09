package brownshome.apss;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GridCell {
    /**
     * Coordinates of the centre of the cell, in the unit of scale
     */
    int x;
    int y;
    int z;

    Vec3 posXposYposZ;
    Vec3 posXposYnegZ;
    Vec3 posXnegYposZ;
    Vec3 posXnegYnegZ;
    Vec3 negXposYposZ;
    Vec3 negXposYnegZ;
    Vec3 negXnegYposZ;
    Vec3 negXnegYnegZ;

    static Map<GridKey, GridCell> map = new HashMap<>();

    private static class GridKey{
        int x, y, z;

        GridKey(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GridKey gridKey = (GridKey) o;
            return x == gridKey.x &&
                    y == gridKey.y &&
                    z == gridKey.z;
        }

        @Override
        public int hashCode() {

            return Objects.hash(x, y, z);
        }
    }

    /**
     * Size of one side of cell (m)
     */
    double gridDimension = 100;

    private GridCell(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        double posX = x * gridDimension + gridDimension/2;
        double posY = y * gridDimension + gridDimension/2;
        double posZ = z * gridDimension + gridDimension/2;

        double negX = x * gridDimension - gridDimension/2;
        double negY = y * gridDimension - gridDimension/2;
        double negZ = z * gridDimension - gridDimension/2;

        posXposYposZ = UnderlyingModels.getGravitationalAcceleration(new Vec3(posX, posY, posZ));
        posXposYnegZ = UnderlyingModels.getGravitationalAcceleration(new Vec3(posX, posY, negZ));
        posXnegYposZ = UnderlyingModels.getGravitationalAcceleration(new Vec3(posX, negY, posZ));
        posXnegYnegZ = UnderlyingModels.getGravitationalAcceleration(new Vec3(posX, negY, negZ));
        negXposYposZ = UnderlyingModels.getGravitationalAcceleration(new Vec3(negX, posY, posZ));
        negXposYnegZ = UnderlyingModels.getGravitationalAcceleration(new Vec3(negX, posY, negZ));
        negXnegYposZ = UnderlyingModels.getGravitationalAcceleration(new Vec3(negX, negY, posZ));
        negXnegYnegZ = UnderlyingModels.getGravitationalAcceleration(new Vec3(negX, negY, negZ));
    }

    public static GridCell getGridCell(int x, int y, int z) {
        return map.computeIfAbsent(new GridKey(x, y, z), key ->
            new GridCell(x, y, z));
    }

    private static Vec3 getGravityatPoint(Vec3 relativeCoordinates) {
        return null;
    }
}
