package xyz.xenondevs.renderer.scene.camera

import xyz.xenondevs.renderer.vector.Point3d
import xyz.xenondevs.renderer.vector.Vector3d
import kotlin.math.atan

internal class Camera(val origin: Point3d, fov: Double, target: Vector3d, up: Vector3d, width: Int, height: Int) {
    
    val forward: Vector3d
    val up: Vector3d
    val right: Vector3d
    val w: Double
    val h: Double
    
    private val rayCache: Array<Array<Ray>>
    
    init {
        this.forward = (target - origin).normalize()
        this.right = (forward x up).normalize()
        this.up = right x forward
        this.h = atan(Math.toRadians(fov))
        this.w = (width.toDouble() / height.toDouble()) * h
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
        val x = (2.0 * pixelX) / imageWidth - 1.0
        val y = (2.0 * (imageHeight - pixelY)) / imageHeight - 1.0
        
        return makeRay(x, y)
    }
    
    // x and y between -1 and 1
    private fun makeRay(x: Double, y: Double): Ray {
        val direction = (forward + right * w * x + up * h * y).normalize()
        return Ray(origin, direction)
    }
    
    override fun toString(): String {
        return "Camera(origin=$origin, forward=$forward, up=$up, right=$right, w=$w, h=$h)"
    }
    
}