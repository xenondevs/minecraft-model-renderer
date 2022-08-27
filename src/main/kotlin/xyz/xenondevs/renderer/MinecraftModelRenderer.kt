package xyz.xenondevs.renderer

import xyz.xenondevs.renderer.model.resource.ResourceLoader
import xyz.xenondevs.renderer.scene.Scene
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class MinecraftModelRenderer(
    private val renderWidth: Int,
    private val renderHeight: Int,
    private val exportWidth: Int,
    private val exportHeight: Int,
    resourcePacks: List<File>,
    useInternalResources: Boolean = true,
    private val cameraDistance: Double = 40.0,
    private val fov: Double = 0.95,
    private val cropVertical: Double = 0.1,
    private val cropHorizontal: Double = 0.1
) {
    
    val loader = ResourceLoader(resourcePacks, useInternalResources)
    
    fun renderModel(model: String): BufferedImage {
        val scene = Scene(
            loader.modelCache.get(model).resolve(),
            renderWidth, renderHeight,
            cameraDistance, fov,
            cropVertical, cropHorizontal
        )
        
        return scene.render(exportWidth, exportHeight)
    }
    
    fun renderModelToFile(model: String, file: File) {
        ImageIO.write(renderModel(model), "png", file)
    }
    
}