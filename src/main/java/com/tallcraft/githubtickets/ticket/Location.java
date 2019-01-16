package com.tallcraft.githubtickets.ticket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Location {
    private double x;
    private double y;
    private double z;

    private static Pattern pattern = Pattern.compile("(\\S+), (\\S+), (\\S+)");

    public Location(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private Location(String x, String y, String z) {
        this(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
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

    public static Location fromString(String str) {
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return new Location(matcher.group(1), matcher.group(2), matcher.group(3));
        }
        return null;
    }
}
