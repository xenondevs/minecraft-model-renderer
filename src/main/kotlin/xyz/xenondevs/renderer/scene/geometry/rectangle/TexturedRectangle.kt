package xyz.xenondevs.renderer.scene.geometry.rectangle

import xyz.xenondevs.renderer.model.ElementRotation
import xyz.xenondevs.renderer.scene.Scene
import xyz.xenondevs.renderer.scene.camera.Intersection
import xyz.xenondevs.renderer.scene.camera.Ray
import xyz.xenondevs.renderer.vector.Point3d
import xyz.xenondevs.renderer.vector.Vector3d
import java.awt.image.BufferedImage
import kotlin.math.min

internal class TexturedRectangle(
    scene: Scene,
    origin: Point3d,
    uVec: Vector3d,
    vVec: Vector3d,
    normal: Vector3d,
    val textureFront: BufferedImage,
    val textureBack: BufferedImage,
    ambientOcclusion: Boolean,
    rot: ElementRotation?
) : Rectangle(scene, origin, uVec, vVec, normal, rot, ambientOcclusion) {
    
    constructor(
        scene: Scene,
        origin: Point3d,
        uVec: Vector3d,
        vVec: Vector3d,
        normal: Vector3d,
        textureFront: BufferedImage,
        ambientOcclusion: Boolean,
        rot: ElementRotation?,
    ) : this(scene, origin, uVec, vVec, normal, textureFront, textureFront, ambientOcclusion, rot)
    
    override fun trace(ray: Ray): Intersection? {
        val intersection = this.traceToVector(ray) ?: return null
        
        val rel = intersection.first - origin
        val u = (uVec.x * rel.x + uVec.y * rel.y + uVec.z * rel.z) / (uVec.x * uVec.x + uVec.y * uVec.y + uVec.z * uVec.z)
        val v = (vVec.x * rel.x + vVec.y * rel.y + vVec.z * rel.z) / (vVec.x * vVec.x + vVec.y * vVec.y + vVec.z * vVec.z)
        if (u < 0 || u > 1 || v < 0 || v > 1) return null
        
        val invertX: Boolean
        val texture: BufferedImage
        if (normal.dot(scene.camera.forward) < 0) {
            invertX = false
            texture = textureFront
        } else {
            invertX = true
            texture = textureBack
        }
        
        val texX = min(if (invertX) (texture.width - u * texture.width).toInt() else (u * texture.width).toInt(), texture.width - 1)
        val texY = min((texture.height - v * texture.height).toInt(), texture.height - 1)
        
        val color = texture.getRGB(texX, texY)
        return Intersection(this.multiplier, intersection.second, color)
    }
    
    override fun toString(): String {
        return "TexturedRectangle(originVec=$origin, uVec=$uVec, vVec=$vVec)"
    }
    
}