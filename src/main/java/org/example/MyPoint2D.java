package org.example;

import Jama.Matrix;

public class MyPoint2D {
    double x;
    double y;

    public MyPoint2D() {
    }

    public Matrix returnMatrix() {
        Matrix M = new Matrix(2, 1);
        M.set(0, 0, x);
        M.set(1, 0, y);
        return M;
    }

    public MyPoint2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x1) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

}
