package xyz.xenondevs.renderer.model

import org.joml.Vector3f
import xyz.xenondevs.renderer.vector.Vector3f

internal enum class Direction(val axis: Axis, val normal: Vector3f) {
    NORTH(Axis.Z, Vector3f(0, 0, -1)),
    EAST(Axis.X, Vector3f(1, 0, 0)),
    SOUTH(Axis.Z, Vector3f(0, 0, 1)),
    WEST(Axis.X, Vector3f(-1, 0, 0)),
    UP(Axis.Y, Vector3f(0, 1, 0)),
    DOWN(Axis.Y, Vector3f(0, -1, 0));
}

internal enum class Axis {
    X,
    Y,
    Z
}