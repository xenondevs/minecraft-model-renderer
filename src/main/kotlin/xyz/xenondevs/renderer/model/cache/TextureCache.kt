package xyz.xenondevs.renderer.model.cache

import xyz.xenondevs.renderer.model.resource.ResourceId
import xyz.xenondevs.renderer.model.resource.ResourceLoader
import xyz.xenondevs.renderer.util.getWithUV
import xyz.xenondevs.renderer.util.rotate
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.ceil

private data class TextureOptions(val id: ResourceId, val rotation: Int, val fromX: Double, val fromY: Double, val toX: Double, val toY: Double)

internal class TextureCache(private val loader: ResourceLoader) {
    
    private val imageCache = HashMap<ResourceId, BufferedImage>()
    private val textureCache = HashMap<TextureOptions, BufferedImage>()
    
    private fun get(id: ResourceId): BufferedImage =
        imageCache.getOrPut(id) { loader.getTextureStream(id).use(ImageIO::read) }
    
    private fun get(id: ResourceId, rotation: Int, fromX: Double, fromY: Double, toX: Double, toY: Double): BufferedImage {
        return textureCache.getOrPut(TextureOptions(id, rotation, fromX, fromY, toX, toY)) {
            var image = get(id)
            
            // take first frame of animated textures
            if (image.height > image.width) {
                image = image.getSubimage(0, 0, image.width, image.width)
            }
            
            if (fromX == toX || fromY == toY)
                return@getOrPut EMPTY_TEXTURE
            
            // apply uv
            image = image.getWithUV(
                ceil(fromX * image.width).toInt(),
                ceil(fromY * image.height).toInt(),
                ceil(toX * image.width).toInt(),
                ceil(toY * image.height).toInt(),
            )
            
            // apply rotation
            if (rotation != 0)
                image = image.rotate(rotation.toDouble())
            
            return@getOrPut image
        }
    }
    
    fun get(id: String, rotation: Int, fromX: Double, fromY: Double, toX: Double, toY: Double): BufferedImage {
        return get(ResourceId.of(id), rotation, fromX, fromY, toX, toY)
    }
    
    companion object {
        val EMPTY_TEXTURE = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).apply { setRGB(0, 0, 0) }
    }
    
}