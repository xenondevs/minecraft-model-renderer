package xyz.xenondevs.renderer

import xyz.xenondevs.renderer.resource.DirectoryResourcePack
import xyz.xenondevs.renderer.resource.InternalResourcePack
import xyz.xenondevs.renderer.resource.ResourcePack
import xyz.xenondevs.renderer.resource.ZipResourcePack
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

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
    
    @JvmField
    val resourcePacks: Array<ResourcePack> = ArrayList<ResourcePack>().apply {
        if (useInternalResources)
            add(InternalResourcePack)
        
        addAll(resourcePacks.map {
            when {
                it.isDirectory() -> DirectoryResourcePack(it)
                it.extension.equals("zip", true) -> ZipResourcePack(it)
                else -> throw IllegalArgumentException("Unsupported resource pack format: ${it.extension}")
            }
        })
    }.toTypedArray()

    external fun render(models: Array<String>, pipeline: RenderPipeline)
    
}

class RenderPipeline {
    
    fun finish(modelPath: String, bytes: ByteArray) {
        println("Finished rendering $modelPath | "+bytes.contentToString())
    }
    
}