package xyz.xenondevs.renderer.scene.camera

import org.joml.Vector3f
import xyz.xenondevs.renderer.vector.Point3f
import xyz.xenondevs.renderer.vector.crossed
import xyz.xenondevs.renderer.vector.minus
import xyz.xenondevs.renderer.vector.normalized
import xyz.xenondevs.renderer.vector.plus
import xyz.xenondevs.renderer.vector.times
import kotlin.math.atan

internal class Camera(val origin: Point3f, fov: Float, target: Vector3f, up: Vector3f, width: Int, height: Int) {
    
    val forward: Vector3f
    val up: Vector3f
    val right: Vector3f
    val w: Float
    val h: Float
    
    private val rayCache: Array<Array<Ray>>
    
    init {
        this.forward = (target - origin).normalized()
        this.right = (forward.crossed(up)).normalized()
        this.up = right.crossed(forward)
        this.h = atan(Math.toRadians(fov.toDouble())).toFloat()
        this.w = (width.toFloat() / height.toFloat()) * h
        this.rayCache = createRayCache(width, height)
    }
    
    private fun createRayCache(width: Int, height: Int): Array<Array<Ray>> {
        return Array(width) { x -> Array(height) { y -> makeRay(x, y, width, height) } }
    }
    
    fun getRay(pixelX: Int = 0, pixelY: Int = 0): Ray {
        return rayCache[pixelX][pixelY]
    }
    
    /**
     * Creates a ray from the camera's origin to the given pixel coordinates.
     */
    private fun makeRay(pixelX: Int, pixelY: Int, imageWidth: Int, imageHeight: Int): Ray {
        val x = (2f * pixelX) / imageWidth - 1f
        val y = (2f * (imageHeight - pixelY)) / imageHeight - 1f
        
        return makeRay(x, y)
    }
    
    // x and y between -1 and 1
    private fun makeRay(x: Float, y: Float): Ray {
        val direction = (forward + right * w * x + up * h * y).normalize()
        return Ray(origin, direction)
    }
    
    override fun toString(): String {
        return "Camera(origin=$origin, forward=$forward, up=$up, right=$right, w=$w, h=$h)"
    }
    
}