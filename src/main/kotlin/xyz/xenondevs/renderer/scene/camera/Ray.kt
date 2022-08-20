package xyz.xenondevs.renderer.scene.camera

import xyz.xenondevs.renderer.vector.Point3d
import xyz.xenondevs.renderer.vector.Vector3d

internal data class Ray(val origin: Point3d, val direction: Vector3d)

internal class Intersection(val multiplier: Double, val t: Double, val color: Int): Comparable<Intersection> {
    
    override fun compareTo(other: Intersection): Int {
        return t.compareTo(other.t)
    }
    
}