package xyz.xenondevs.renderer.scene

import xyz.xenondevs.renderer.model.Axis
import xyz.xenondevs.renderer.model.GeometricalModel
import xyz.xenondevs.renderer.scene.camera.Camera
import xyz.xenondevs.renderer.scene.camera.Ray
import xyz.xenondevs.renderer.scene.geometry.GeometryObject
import xyz.xenondevs.renderer.util.scale
import xyz.xenondevs.renderer.util.translate
import xyz.xenondevs.renderer.vector.Vector3d
import java.awt.image.BufferedImage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class Scene(
    private val model: GeometricalModel,
    private val width: Int,
    private val height: Int,
    cameraDistance: Double,
    fov: Double,
    private val cropVertical: Double,
    private val cropHorizontal: Double
) {
    
    private val objects: List<GeometryObject>
    val camera: Camera
    
    init {
        val camPos = Vector3d(0.0, 0.0, cameraDistance)
            .rotate(Vector3d.ZERO, Axis.X, -model.rotation.x)
            .rotate(Vector3d.ZERO, Axis.Y, -model.rotation.y)
        camera = Camera(camPos, fov, Vector3d.ZERO, Vector3d(0, 1, 0), width, height)
        objects = model.toSceneGeometry(this)
    }
    
    fun render(scaleWidth: Int, scaleHeight: Int): BufferedImage {
        val service = Executors.newFixedThreadPool(12)
        
        var image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (x in 0 until width) {
            service.submit {
                try {
                    for (y in 0 until height) {
                        val ray = camera.getRay(x, y)
                        image.setRGB(x, y, trace(ray))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        service.shutdown()
        service.awaitTermination(5, TimeUnit.MINUTES)
        
        // translate
        val translateX = ((model.translation.x / 16.0) * image.width).toInt()
        val translateY = ((model.translation.y / 16.0) * image.height).toInt()
        if (translateX != 0 || translateY != 0)
            image = image.translate(translateX, -translateY)
        
        // crop
        image = image.getSubimage(
            (width * cropHorizontal).toInt(),
            (height * cropVertical).toInt(),
            (width * (1 - 2 * cropHorizontal)).toInt(),
            (height * (1 - 2 * cropVertical)).toInt()
        )
        
        // scale
        if (scaleWidth != width || scaleHeight != height)
            image = image.scale(scaleWidth, scaleHeight)
        
        return image
    }
    
    private fun trace(ray: Ray): Int {
        val intersections = objects.mapNotNullTo(ArrayList()) { it.trace(ray) }.apply { sort() }
        if (intersections.isEmpty()) return 0
        var alpha = 0.0
        var r = 0.0
        var g = 0.0
        var b = 0.0
        var index = 0
        while (alpha < 1 && index < intersections.size) {
            val next = intersections[index]
            val nextAlpha = ((next.color shr 24) and 0xFF) / 255.0
            val nextR = ((next.color shr 16) and 0xFF) * nextAlpha * next.multiplier
            val nextG = ((next.color shr 8) and 0xFF) * nextAlpha * next.multiplier
            val nextB = (next.color and 0xFF) * nextAlpha * next.multiplier
            r += nextR * (1 - alpha)
            g += nextG * (1 - alpha)
            b += nextB * (1 - alpha)
            alpha += nextAlpha
            ++index
        }
        
        return ((alpha * 255).toInt() shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
    }
    
}