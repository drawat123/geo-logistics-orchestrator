package io.github.drawat123.geo_logistics_orchestrator.graph.model;

public record LocationNode(String id, double lat, double lon) {

    // Radius of the Earth in kilometers
    private static final double EARTH_RADIUS = 6371.0;

    public double distanceTo(LocationNode other) {
        // Convert degrees to radians
        double lat1 = Math.toRadians(this.lat);
        double lon1 = Math.toRadians(this.lon);
        double lat2 = Math.toRadians(other.lat());
        double lon2 = Math.toRadians(other.lon());

        // Differences in coordinates
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // Haversine formula
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Result in kilometers
    }
}