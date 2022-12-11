package xyz.xenondevs.renderer.scene.camera

import org.joml.Vector3f
import xyz.xenondevs.renderer.vector.Point3f

internal data class Ray(val origin: Point3f, val direction: Vector3f)

internal class Intersection(val multiplier: Float, val t: Float, val color: Int): Comparable<Intersection> {
    
    override fun compareTo(other: Intersection): Int {
        return t.compareTo(other.t)
    }
    
}