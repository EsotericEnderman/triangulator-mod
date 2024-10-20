package dev.enderman.minecraft.mods.triangulator;

import dev.enderman.minecraft.mods.triangulator.utility.VectorUtility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public class Line {

    private final Vector2d startingPoint;
    private final Vector2d directionVector;

    public Vector2d getStartingPoint() {
        return startingPoint;
    }

    public Vector2d getDirectionVector() {
        return directionVector;
    }

    public Line(Vector2d startingPoint, Vector2d directionVector) throws ZeroVectorException {
        if (VectorUtility.isZeroVector(directionVector)) {
            throw new ZeroVectorException();
        }

        this.startingPoint = startingPoint;
        this.directionVector = directionVector;
    }

    @NotNull public Vector2d getPointForParameterValue(double parameterValue) {
        Vector2d startingPointClone = VectorUtility.cloneVector(startingPoint);
        Vector2d directionVectorClone = VectorUtility.cloneVector(directionVector);

        return startingPointClone.add(directionVectorClone.mul(parameterValue));
    }

    public double getParameterValueForPoint(Vector2d point) throws PointNotOnLineException {
        if (!includesPoint(point)) {
            throw new PointNotOnLineException();
        }

        Vector2d pointClone = VectorUtility.cloneVector(point);

        Vector2d distanceFromStartingPoint = pointClone.sub(startingPoint);

        return distanceFromStartingPoint.x / directionVector.x;
    }

    @Nullable public Vector2d getIntersectionPoint(Line otherLine) throws SameLineException {
        if (this.equals(otherLine)) {
            throw new SameLineException();
        }

        Vector2d otherDirectionVector = otherLine.directionVector;

        if (VectorUtility.areLinearlyDependent(otherDirectionVector, directionVector)) { // The lines are parallel but not equal
            return null;
        }

        // Simultaneous equations to solve for t:
        // 1. x01 + dx1 * t1 = x02 + dx2 * t2
        // 2. y01 + dy1 * t1 = y02 + dy2 * t2

        // 1. => t1 = (x02 + dx2 * t2 - x01) / dx1
        // 2. => y01 + dy1 * (x02 + dx2 * t2 - x01) / dx1 = y02 + dy2 * t2
        // 2. => y01 + (dy1 * x02 + dy1 * dx2 * t2 - dy1 * x01) / dx1 = y02 + dy2 * t2
        // 2. => y01 * dx1 + dy1 * x02 + dy1 * dx2 * t2 - dy1 * x01 = y02 * dx1 + dy2 * t2 * dx1
        // 2. => y01 * dx1 + dy1 * x02 - dy1 * x01 = y02 * dx1 + dy2 * t2 * dx1 - dy1 * dx2 * t2
        // 2. => y01 * dx1 + dy1 * x02 - dy1 * x01 = y02 * dx1 + t2 * (dy2 * dx1 - dy1 * dx2)
        // 2. => t2 = (y01 * dx1 + dy1 * x02 - dy1 * x01 - y02 * dx1) / (dy2 * dx1 - dy1 * dx2)

        Vector2d otherStartingPoint = otherLine.startingPoint;

        double x01 = startingPoint.x;
        double dx1 = directionVector.x;

        double x02 = otherStartingPoint.x;
        double dx2 = otherDirectionVector.x;

        double y01 = startingPoint.y;
        double dy1 = directionVector.y;

        double y02 = otherStartingPoint.y;
        double dy2 = otherDirectionVector.y;

        double t2 = (y01 * dx1 + dy1 * x02 - dy1 * x01 - y02 * dx1) / (dy2 * dx1 - dy1 * dx2);

        return otherLine.getPointForParameterValue(t2);
    }

    public boolean includesPoint(@NotNull Vector2d point) {
        Vector2d clonedPoint = new Vector2d(point.x, point.y);

        return clonedPoint.equals(startingPoint) || VectorUtility.areLinearlyDependent(clonedPoint.sub(startingPoint), directionVector);
    }

    @Override
    public boolean equals(Object otherObject) {
        if (super.equals(otherObject)) {
            return true;
        }

        if (otherObject instanceof Line otherLine) {
            if (startingPoint.equals(otherLine.startingPoint) && directionVector.equals(otherLine.directionVector) ) {
                return true;
            }

            return includesPoint(otherLine.startingPoint) && VectorUtility.areLinearlyDependent(directionVector, otherLine.directionVector);
        }

        return false;
    }

    @Override
    public String toString() {
        return "{(" + startingPoint.x + ", " + startingPoint.y + ") + t × (" + directionVector.x + ", " + directionVector.y + ") | t ∈ R}";
    }

    public static class ZeroVectorException extends Exception {
        ZeroVectorException() {
            super("The direction vector must not be the zero vector (0, 0)!");
        }
    }

    public static class SameLineException extends Exception {
        SameLineException() {
            super("Cannot check intersection with the same line.");
        }
    }

    public static class PointNotOnLineException extends Exception {
        PointNotOnLineException() {
            super("Point is not on the line.");
        }
    }
}
