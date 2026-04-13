package Ontdekstation013.ClimateChecker.features.map.endpoint;

public class MapConfigDto {
    private double[] center;
    private int zoom;

    public MapConfigDto(double[] center, int zoom) {
        this.center = center;
        this.zoom = zoom;
    }

    public double[] getCenter() {
        return center;
    }

    public void setCenter(double[] center) {
        this.center = center;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }
}
