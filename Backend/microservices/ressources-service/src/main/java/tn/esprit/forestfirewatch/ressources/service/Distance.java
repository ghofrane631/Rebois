package tn.esprit.forestfirewatch.ressources.service;

/** Distance à vol d'oiseau entre deux points GPS (formule de Haversine), en km. */
public final class Distance {

    private static final double RAYON_TERRE_KM = 6371.0;

    private Distance() {
    }

    public static double km(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * RAYON_TERRE_KM * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
