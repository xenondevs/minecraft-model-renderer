package xyz.xenondevs.renderer.scene.geometry.rectangle

import org.joml.Intersectionf
import org.joml.Vector3f
import xyz.xenondevs.renderer.model.ElementRotation
import xyz.xenondevs.renderer.scene.Scene
import xyz.xenondevs.renderer.scene.camera.Ray
import xyz.xenondevs.renderer.scene.geometry.GeometryObject
import xyz.xenondevs.renderer.vector.Point3f
import xyz.xenondevs.renderer.vector.minus
import xyz.xenondevs.renderer.vector.plus
import xyz.xenondevs.renderer.vector.rotate
import kotlin.math.acos

private const val BRIGHTNESS_MULTIPLIER = 1.0
private const val DARKNESS_MULTIPLIER = 0.3

internal abstract class Rectangle(
    override val scene: Scene,
    var origin: Point3f,
    var uVec: Vector3f,
    var vVec: Vector3f,
    var normal: Vector3f,
    rot: ElementRotation?,
    var ambientOcclusion: Boolean
) : GeometryObject {
    
    protected var multiplier: Double = 0.0
    
    init {
        ambientOcclusion = !(rot != null && rot.angle != 0f)
        
        applyRotation(rot)
    }
    
    fun applyRotation(rot: ElementRotation?) {
        if (rot != null) {
            val (rotationOrigin, axis, degrees) = rot
            
            val uPoint = uVec + origin
            val vPoint = vVec + origin
            val normalPoint = normal + origin
            
            origin = origin.rotate(rotationOrigin, axis, degrees)
            uVec = uPoint.rotate(rotationOrigin, axis, degrees) - origin
            vVec = vPoint.rotate(rotationOrigin, axis, degrees) - origin
            normal = normalPoint.rotate(rotationOrigin, axis, degrees) - origin
        }
      
        multiplier = if (ambientOcclusion) {
            val dif = acos(normal.dot(scene.camera.right)) % Math.PI
            if (dif < Math.PI / 2) {
                dif / Math.PI + DARKNESS_MULTIPLIER
            } else BRIGHTNESS_MULTIPLIER
        } else BRIGHTNESS_MULTIPLIER
    }
    
    protected fun traceToVector(ray: Ray): Pair<Vector3f, Float>? {
        val (start, direction) = ray
        
        val t = Intersectionf.intersectRayPlane(ray.origin, ray.direction, origin, normal, 0f)
        
        if (t < 0 || t.isInfinite() || t.isNaN())
            return null
        
        return Vector3f(
            start.x + direction.x * t,
            start.y + direction.y * t,
            start.z + direction.z * t
        ) to t
    }
    
    override fun toString(): String {
        return "Rectangle(originVec=$origin, uVec=$uVec, vVec=$vVec)"
    }
    
}