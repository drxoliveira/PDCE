package pdcerl.model;

public class Point {

    private double x;
    private double y;
    public static final int NUMBER_DIMENSIONS = 2;

    public Point() {
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getCoordinate(int dimension) {
        if (dimension == 0) {
            return this.x;
        } else {
            return this.y;
        }
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

}
