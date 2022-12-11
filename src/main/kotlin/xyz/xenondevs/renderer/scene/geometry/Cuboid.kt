package xyz.xenondevs.renderer.scene.geometry

import org.joml.Vector3f
import xyz.xenondevs.renderer.model.Axis
import xyz.xenondevs.renderer.model.Direction
import xyz.xenondevs.renderer.model.Direction.*
import xyz.xenondevs.renderer.model.ElementRotation
import xyz.xenondevs.renderer.scene.Scene
import xyz.xenondevs.renderer.scene.camera.Intersection
import xyz.xenondevs.renderer.scene.camera.Ray
import xyz.xenondevs.renderer.scene.geometry.rectangle.Rectangle
import xyz.xenondevs.renderer.scene.geometry.rectangle.TexturedRectangle
import xyz.xenondevs.renderer.vector.addX
import xyz.xenondevs.renderer.vector.addXZ
import xyz.xenondevs.renderer.vector.addYZ
import xyz.xenondevs.renderer.vector.addZ
import xyz.xenondevs.renderer.vector.divided
import xyz.xenondevs.renderer.vector.minus
import java.awt.image.BufferedImage
import kotlin.math.cos

private val RESCALE_22_5 = (1.0 / cos(Math.PI / 8.0)).toFloat()
private val RESCALE_45 = (1.0 / cos(Math.PI / 4.0)).toFloat()

internal class Cuboid(
    override val scene: Scene,
    private var origin: Vector3f,
    private var size: Vector3f,
    textures: Map<Direction, BufferedImage>,
    rotation: ElementRotation?,
    ambientOcclusion: Boolean
) : GeometryObject {
    
    private val rectangles = ArrayList<Rectangle>(6)
    
    init {
        if (rotation != null && rotation.rescale) {
            val multiplier = when (rotation.angle) {
                22.5f, -22.5f -> RESCALE_22_5
                else -> RESCALE_45
            }
            
            val newSize = when (rotation.axis) {
                Axis.X -> Vector3f(size.x, size.y * multiplier, size.z * multiplier)
                Axis.Y -> Vector3f(size.x * multiplier, size.y, size.z * multiplier)
                Axis.Z -> Vector3f(size.x * multiplier, size.y * multiplier, size.z)
            }
            
            origin -= (newSize - size).divided(2f)
            size = newSize
        }
        
        if (size.x == 0f) {
            rectangles += TexturedRectangle(scene, origin, Vector3f(0f, 0f, size.z), Vector3f(0f, size.y, 0f), WEST.normal, textures[WEST]!!, textures[EAST]!!, ambientOcclusion, rotation)
        } else if (size.y == 0f) {
            rectangles += TexturedRectangle(scene, origin.addYZ(size.y, size.z), Vector3f(size.x, 0f, 0f), Vector3f(0f, 0f, -size.z), UP.normal, textures[UP]!!, textures[DOWN]!!, ambientOcclusion, rotation)
        } else if (size.z == 0f) {
            rectangles += TexturedRectangle(scene, origin.addX(size.x), Vector3f(-size.x, 0f, 0f), Vector3f(0f, size.y, 0f), NORTH.normal, textures[NORTH]!!, textures[SOUTH]!!, ambientOcclusion, rotation)
        } else {
            rectangles.addAll(listOf(
                TexturedRectangle(scene, origin.addX(size.x), Vector3f(-size.x, 0f, 0f), Vector3f(0f, size.y, 0f), NORTH.normal, textures[NORTH]!!, ambientOcclusion, rotation), // north
                TexturedRectangle(scene, origin.addXZ(size.x, size.z), Vector3f(0f, 0f, -size.z), Vector3f(0f, size.y, 0f), EAST.normal, textures[EAST]!!, ambientOcclusion, rotation), // east
                TexturedRectangle(scene, origin.addZ(size.z), Vector3f(size.x, 0f, 0f), Vector3f(0f, size.y, 0f), SOUTH.normal, textures[SOUTH]!!, ambientOcclusion, rotation), // south
                TexturedRectangle(scene, origin, Vector3f(0f, 0f, size.z), Vector3f(0f, size.y, 0f), WEST.normal, textures[WEST]!!, ambientOcclusion, rotation), // west
                TexturedRectangle(scene, origin.addYZ(size.y, size.z), Vector3f(size.x, 0f, 0f), Vector3f(0f, 0f, -size.z), UP.normal, textures[UP]!!, ambientOcclusion, rotation), // up
                TexturedRectangle(scene, origin, Vector3f(size.x, 0f, 0f), Vector3f(0f, 0f, size.z), DOWN.normal, textures[DOWN]!!, ambientOcclusion, rotation), // down
            ))
        }
    }
    
    fun applyRotation(elementRotation: ElementRotation) {
        rectangles.forEach { it.applyRotation(elementRotation) }
    }
    
    override fun trace(ray: Ray): Intersection? {
        return rectangles.mapNotNull { it.trace(ray) }.minByOrNull { it.t }
    }
    
    override fun toString(): String {
        return "Cuboid(origin=$origin, size=$size, rectangles=$rectangles)"
    }
    
}