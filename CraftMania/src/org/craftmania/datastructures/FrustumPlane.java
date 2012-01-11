package org.craftmania.datastructures;



/**
 * Represents a plane of a view frustum.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class FrustumPlane {

    private double a, b, c, d;

    /**
     * Init. a new view frustum with the default values in place.
     */
    public FrustumPlane() {
        // Do nothing.
    }

    /**
     * Init. a new view frustum with a given plane equation.
     * ax + by + cy + d = 0
     */
    public FrustumPlane(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    /**
     * Normalizes this plane.
     */
    public void normalize() {
        double t = Math.sqrt(a * a + b * b + c * c);
        a /= t;
        b /= t;
        c /= t;
        d /= t;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    void setC(double c) {
        this.c = c;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }
}
