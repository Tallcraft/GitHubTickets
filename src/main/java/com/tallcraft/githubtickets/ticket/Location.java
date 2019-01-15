package com.tallcraft.githubtickets.ticket;

public class Location {
    private double x;
    private double y;
    private double z;

    public Location(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(long z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return this.x + ", " + this.y + ", " + this.z;
    }
}
