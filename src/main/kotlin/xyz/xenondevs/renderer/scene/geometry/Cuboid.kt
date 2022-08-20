package xyz.xenondevs.renderer.scene.geometry

import xyz.xenondevs.renderer.model.Axis
import xyz.xenondevs.renderer.model.Direction
import xyz.xenondevs.renderer.model.Direction.*
import xyz.xenondevs.renderer.model.ElementRotation
import xyz.xenondevs.renderer.scene.Scene
import xyz.xenondevs.renderer.scene.camera.Intersection
import xyz.xenondevs.renderer.scene.camera.Ray
import xyz.xenondevs.renderer.scene.geometry.rectangle.Rectangle
import xyz.xenondevs.renderer.scene.geometry.rectangle.TexturedRectangle
import xyz.xenondevs.renderer.vector.Vector3d
import java.awt.image.BufferedImage
import kotlin.math.cos

private val RESCALE_22_5 = 1.0 / cos(Math.PI / 8.0)
private val RESCALE_45 = 1.0 / cos(Math.PI / 4.0)

internal class Cuboid(
    override val scene: Scene,
    private var origin: Vector3d,
    private var size: Vector3d,
    textures: Map<Direction, BufferedImage>,
    rotation: ElementRotation?,
    ambientOcclusion: Boolean
) : GeometryObject {
    
    private val rectangles = ArrayList<Rectangle>(6)
    
    init {
        if (rotation != null && rotation.rescale) {
            val multiplier = when (rotation.angle) {
                22.5, -22.5 -> RESCALE_22_5
                else -> RESCALE_45
            }
            
            val newSize = when (rotation.axis) {
                Axis.X -> Vector3d(size.x, size.y * multiplier, size.z * multiplier)
                Axis.Y -> Vector3d(size.x * multiplier, size.y, size.z * multiplier)
                Axis.Z -> Vector3d(size.x * multiplier, size.y * multiplier, size.z)
            }
            
            origin -= (newSize - size) / 2.0
            size = newSize
        }
        
        if (size.x == 0.0) {
            rectangles += TexturedRectangle(scene, origin, Vector3d(0.0, 0.0, size.z), Vector3d(0.0, size.y, 0.0), WEST.normal, textures[WEST]!!, textures[EAST]!!, ambientOcclusion, rotation)
        } else if (size.y == 0.0) {
            rectangles += TexturedRectangle(scene, origin.addYZ(size.y, size.z), Vector3d(size.x, 0.0, 0.0), Vector3d(0.0, 0.0, -size.z), UP.normal, textures[UP]!!, textures[DOWN]!!, ambientOcclusion, rotation)
        } else if (size.z == 0.0) {
            rectangles += TexturedRectangle(scene, origin.addX(size.x), Vector3d(-size.x, 0.0, 0.0), Vector3d(0.0, size.y, 0.0), NORTH.normal, textures[NORTH]!!, textures[SOUTH]!!, ambientOcclusion, rotation)
        } else {
            rectangles.addAll(listOf(
                TexturedRectangle(scene, origin.addX(size.x), Vector3d(-size.x, 0.0, 0.0), Vector3d(0.0, size.y, 0.0), NORTH.normal, textures[NORTH]!!, ambientOcclusion, rotation), // north
                TexturedRectangle(scene, origin.addXZ(size.x, size.z), Vector3d(0.0, 0.0, -size.z), Vector3d(0.0, size.y, 0.0), EAST.normal, textures[EAST]!!, ambientOcclusion, rotation), // east
                TexturedRectangle(scene, origin.addZ(size.z), Vector3d(size.x, 0.0, 0.0), Vector3d(0.0, size.y, 0.0), SOUTH.normal, textures[SOUTH]!!, ambientOcclusion, rotation), // south
                TexturedRectangle(scene, origin, Vector3d(0.0, 0.0, size.z), Vector3d(0.0, size.y, 0.0), WEST.normal, textures[WEST]!!, ambientOcclusion, rotation), // west
                TexturedRectangle(scene, origin.addYZ(size.y, size.z), Vector3d(size.x, 0.0, 0.0), Vector3d(0.0, 0.0, -size.z), UP.normal, textures[UP]!!, ambientOcclusion, rotation), // up
                TexturedRectangle(scene, origin, Vector3d(size.x, 0.0, 0.0), Vector3d(0.0, 0.0, size.z), DOWN.normal, textures[DOWN]!!, ambientOcclusion, rotation), // down
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