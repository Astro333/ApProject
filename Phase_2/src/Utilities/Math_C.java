package Utilities;

public class Math_C {

    // prevent instantiation
    private Math_C(){}

    public static int distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}
