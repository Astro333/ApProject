package Utilities;

public class UnitVector {
    private double x, y;
    public UnitVector(double x, double y) {
        update(x, y);
    }

    public void update(double x, double y) {
        assert !(x == 0 && y == 0);
        double sqrt = Math.sqrt(x * x + y * y);
        this.x = x / sqrt;
        this.y = y / sqrt;
    }

    public double dot(double x, double y) {
        return this.x * x + this.y * y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
