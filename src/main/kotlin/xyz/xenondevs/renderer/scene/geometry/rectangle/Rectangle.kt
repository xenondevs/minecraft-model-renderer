package xyz.xenondevs.renderer.scene.geometry.rectangle

import xyz.xenondevs.renderer.model.ElementRotation
import xyz.xenondevs.renderer.scene.Scene
import xyz.xenondevs.renderer.scene.camera.Ray
import xyz.xenondevs.renderer.scene.geometry.GeometryObject
import xyz.xenondevs.renderer.vector.Point3d
import xyz.xenondevs.renderer.vector.Vector3d
import kotlin.math.acos

private const val BRIGHTNESS_MULTIPLIER = 1.0
private const val DARKNESS_MULTIPLIER = 0.3

internal abstract class Rectangle(
    override val scene: Scene,
    var origin: Point3d,
    var uVec: Vector3d,
    var vVec: Vector3d,
    var normal: Vector3d,
    rot: ElementRotation?,
    var ambientOcclusion: Boolean
) : GeometryObject {
    
    protected var multiplier: Double = 0.0
    
    init {
        ambientOcclusion = !(rot != null && rot.angle != 0.0)
        
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
    
    protected fun traceToVector(ray: Ray): Pair<Vector3d, Double>? {
        val (start, direction) = ray
        
        val denom = normal.x * direction.x + normal.y * direction.y + normal.z * direction.z
        if (denom > 0)
            return null
        
        val t = ((origin.x - ray.origin.x) * normal.x + (origin.y - ray.origin.y) * normal.y + (origin.z - ray.origin.z) * normal.z) / denom
        
        if (t < 0 || t.isInfinite() || t.isNaN())
            return null
        
        return Vector3d(
            x = start.x + direction.x * t,
            y = start.y + direction.y * t,
            z = start.z + direction.z * t
        ) to t
    }
    
    override fun toString(): String {
        return "Rectangle(originVec=$origin, uVec=$uVec, vVec=$vVec)"
    }
    
}