package xyz.xenondevs.renderer.model

import xyz.xenondevs.renderer.scene.Scene
import xyz.xenondevs.renderer.scene.geometry.Cuboid
import xyz.xenondevs.renderer.scene.geometry.GeometryObject
import xyz.xenondevs.renderer.vector.Vector3d
import java.awt.RenderingHints
import java.awt.image.BufferedImage

internal data class ElementRotation(val origin: Vector3d, val axis: Axis, val angle: Double, val rescale: Boolean) {
    
    fun offset(scale: Vector3d, translation: Vector3d): ElementRotation {
        return copy(origin = (origin * scale) + translation)
    }
    
}

internal data class Element(val from: Vector3d, val to: Vector3d, val rotation: ElementRotation?, val faces: Map<Direction, BufferedImage>)

sealed interface Model

internal class GeometricalModel(
    private val elements: List<Element>,
    val ambientOcclusion: Boolean,
    val rotation: Vector3d,
    val translation: Vector3d,
    val scale: Vector3d
): Model {
    
    fun toSceneGeometry(scene: Scene): List<GeometryObject> {
        val translation = -Vector3d.HALF * scale
        return elements.map { element ->
            Cuboid(
                scene,
                element.from * scale + translation,
                (element.to - element.from) * scale,
                element.faces,
                element.rotation?.offset(scale, translation),
                ambientOcclusion
            ).apply { applyRotation(ElementRotation(Vector3d.ZERO, Axis.Z, rotation.z, false)) }
        }
    }
    
}

internal class LayeredModel(
    private val layers: List<BufferedImage>
) : Model {
    
    fun toImage(): BufferedImage {
        val width = layers.maxOfOrNull { it.width } ?: 16
        val height = layers.maxOfOrNull { it.height } ?: 16
        
        val image = BufferedImage(
            width,
            height,
            BufferedImage.TYPE_INT_ARGB
        )
        
        val graphics = image.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
        layers.forEach { 
            graphics.drawImage(it, 0, 0, width, height, null)
        }
        graphics.dispose()
        
        return image
    }
    
}