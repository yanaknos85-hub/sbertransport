package ru.sberbank.ditsib.geo.utils.geometry;

import org.springframework.stereotype.Component;

/**
 * Утилита для работы с точками на сфере (WGS84 spheroid earth model, without height model).
 */
@Component
public class SphereUtil {

    /**
     * Radius of the WGS84 Ellipsoid in m (a).
     */
    private static final double WGS84_RADIUS = 6378137.0; // m

    /**
     * Inverse flattening 1/f of the WGS84 Ellipsoid.
     */
    private static final double WGS84_INV_FLATTENING = 298.257223563;

    /**
     * Flattening f of the WGS84 Ellipsoid.
     */
    private static final double WGS84_FLATTENING = 1 / WGS84_INV_FLATTENING;

    /**
     * Minor axis radius.
     */
    private static final double MINOR_AXIS_RADIUS = WGS84_RADIUS * (1 - WGS84_FLATTENING);

    public double distanceDeg(double lat1, double lng1, double lat2, double lng2) {
        return distanceRad(Math.toRadians(lat1), Math.toRadians(lng1),
                Math.toRadians(lat2), Math.toRadians(lng2));
    }

    private double distanceRad(double lat1, double lng1, double lat2, double lng2) {
        // Vincenty uses minor axis radius!
        return MINOR_AXIS_RADIUS * ellipsoidVincentyFormulaRad(lat1, lng1, lat2, lng2);
    }

    private double ellipsoidVincentyFormulaRad(double lat1, double lon1, double lat2, double lon2) {
        var dlon = lon2 >= lon1 ? lon2 - lon1 : lon1 - lon2;
        var onemf = 1.0 - SphereUtil.WGS84_FLATTENING;
        var ab = 1.0 / onemf;
        var ecc2 = (ab + 1.0) * (ab - 1.0);
        var u1 = Math.atan(onemf * Math.tan(lat1));
        var u2 = Math.atan(onemf * Math.tan(lat2));
        var su1 = Math.sin(u1);
        var cu1 = Math.cos(u1);
        var su2 = Math.sin(u2);
        var cu2 = Math.cos(u2);
        var lambda = dlon;
        var i = 0;

        while (true) {
            var slon = Math.sin(lambda);
            var clon = Math.cos(lambda);
            var term1 = cu2 * slon;
            var term2 = cu1 * su2 - su1 * cu2 * clon;
            var ssig = Math.sqrt(term1 * term1 + term2 * term2);
            var csig = su1 * su2 + cu1 * cu2 * clon;
            if (ssig <= 0.0) {
                return 0.0;
            }

            var sigma = Math.atan2(ssig, csig);
            var salp = cu1 * cu2 * slon / ssig;
            var c2alp = (1.0 + salp) * (1.0 - salp);
            var ctwosigm = Math.abs(c2alp) > 0.0 ? csig - 2.0 * su1 * su2 / c2alp : 0.0;
            var c2twosigm = ctwosigm * ctwosigm;
            var cc = SphereUtil.WGS84_FLATTENING * 0.0625 * c2alp * (4.0 + SphereUtil.WGS84_FLATTENING * (4.0 - 3.0 * c2alp));
            var prevlambda = lambda;
            lambda = dlon + (1.0 - cc) * SphereUtil.WGS84_FLATTENING * salp * (sigma + cc * ssig * (ctwosigm + cc * csig * (-1.0 + 2.0 * c2twosigm)));
            if (Math.abs(prevlambda - lambda) < 1.0E-12 || i >= 20) {
                var usq = c2alp * ecc2;
                var aa = 1.0 + usq / 16384.0 * (4096.0 + usq * (-768.0 + usq * (320.0 - 175.0 * usq)));
                var bb = usq / 1024.0 * (256.0 + usq * (-128.0 + usq * (74.0 - 47.0 * usq)));
                var dsig = bb * ssig * (ctwosigm + 0.25 * bb * (csig * (-1.0 + 2.0 * c2twosigm) - 0.16666666666666666 * bb * ctwosigm * (-3.0 + 4.0 * ssig * ssig) * (-3.0 + 4.0 * c2twosigm)));
                return aa * (sigma - dsig);
            }

            ++i;
        }
    }
}
