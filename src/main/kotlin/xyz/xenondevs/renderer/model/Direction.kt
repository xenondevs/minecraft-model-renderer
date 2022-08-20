package xyz.xenondevs.renderer.model

import xyz.xenondevs.renderer.vector.Vector3d

internal enum class Direction(val axis: Axis, val normal: Vector3d) {
    NORTH(Axis.Z, Vector3d(0, 0, -1)),
    EAST(Axis.X, Vector3d(1, 0, 0)),
    SOUTH(Axis.Z, Vector3d(0, 0, 1)),
    WEST(Axis.X, Vector3d(-1, 0, 0)),
    UP(Axis.Y, Vector3d(0, 1, 0)),
    DOWN(Axis.Y, Vector3d(0, -1, 0));
}

internal enum class Axis {
    X,
    Y,
    Z
}