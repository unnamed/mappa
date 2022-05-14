package team.unnamed.mappa.bukkit.util;

import team.unnamed.mappa.object.Vector;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface MathUtils {

    /**
     * Fix yaw range to 180 and -180
     *
     * From
     * https://stackoverflow.com/questions/2320986/easy-way-to-keeping-angles-between-179-and-180-degrees
     * @param yaw Yaw to fix.
     * @return Fixed yaw
     */
    static double fixYaw(double yaw) {
        double angle = yaw % 360;
        angle = (angle + 360) % 360;

        if (angle > 180) {
            angle -= 360;
        }
        return roundDecimals(angle);
    }

    static Vector roundVector(Vector vector) {
        double x = roundDecimals(vector.getX());
        double y = roundDecimals(vector.getY());
        double z = roundDecimals(vector.getZ());
        double yaw = roundDecimals(vector.getYaw());
        double pitch = roundDecimals(vector.getPitch());
        return new Vector(x, y, z, yaw, pitch);
    }

    static double roundDecimals(double decimal) {
        BigDecimal bigDecimal = new BigDecimal(decimal);
        bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_EVEN);
        return bigDecimal.doubleValue();
    }

    static float roundDecimals(float decimal) {
        BigDecimal bigDecimal = new BigDecimal(decimal);
        bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_EVEN);
        return bigDecimal.floatValue();
    }

    static double roundAllDecimals(double decimal) {
        BigDecimal bigDecimal = new BigDecimal(decimal);
        bigDecimal = bigDecimal.setScale(0, RoundingMode.HALF_EVEN);
        return bigDecimal.doubleValue();
    }
}
