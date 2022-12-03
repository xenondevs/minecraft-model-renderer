package xyz.xenondevs.renderer

import xyz.xenondevs.renderer.model.GeometricalModel
import xyz.xenondevs.renderer.model.LayeredModel
import xyz.xenondevs.renderer.model.resource.ResourceLoader
import xyz.xenondevs.renderer.scene.Scene
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.outputStream

class MinecraftModelRenderer(
    private val renderWidth: Int,
    private val renderHeight: Int,
    private val exportWidth: Int,
    private val exportHeight: Int,
    resourcePacks: List<Path>,
    useInternalResources: Boolean = true,
    private val cameraDistance: Double = 40.0,
    private val fov: Double = 0.95,
    private val cropVertical: Double = 0.1,
    private val cropHorizontal: Double = 0.1
) {
    
    val loader = ResourceLoader(resourcePacks, useInternalResources)
    
    fun renderModel(modelPath: String): BufferedImage {
        when (val model = loader.modelCache.get(modelPath).resolve()) {
            is GeometricalModel -> {
                val scene = Scene(
                    model,
                    renderWidth, renderHeight,
                    cameraDistance, fov,
                    cropVertical, cropHorizontal
                )
                
                return scene.render(exportWidth, exportHeight)
            }
            
            is LayeredModel -> {
                return model.toImage()
            }
        }
    }
    
    fun renderModelToFile(model: String, file: File) {
        ImageIO.write(renderModel(model), "png", file)
    }
    
    fun renderModelToFile(model: String, file: Path) {
        file.outputStream().use { out -> ImageIO.write(renderModel(model), "png", out) }
    }
    
}