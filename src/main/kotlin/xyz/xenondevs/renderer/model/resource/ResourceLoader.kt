package xyz.xenondevs.renderer.model.resource

import xyz.xenondevs.renderer.model.cache.ModelCache
import xyz.xenondevs.renderer.model.cache.TextureCache
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

class ResourceLoader(
    packs: List<Path>,
    useInternalResources: Boolean
) {
    
    val resourcePacks = ArrayList<ResourcePack>().apply {
        if (useInternalResources)
            add(InternalResourcePack)
        
        addAll(packs.map {
            when {
                it.isDirectory() -> DirectoryResourcePack(it)
                it.extension.equals("zip", true) -> ZipResourcePack(it)
                else -> throw IllegalArgumentException("Unsupported resource pack format: ${it.extension}")
            }
        })
    }
    
    internal val modelCache = ModelCache(this)
    internal val textureCache = TextureCache(this)
    
    internal fun getModelStream(id: ResourceId): InputStream {
        return getStream("assets/${id.namespace}/models/${id.path}.json")
            ?: throw IllegalArgumentException("Model not found: $id")
    }
    
    internal fun getTextureStream(id: ResourceId): InputStream {
        return getStream("assets/${id.namespace}/textures/${id.path}.png")
            ?: throw IllegalArgumentException("Texture not found: $id")
    }
    
    private fun getStream(path: String): InputStream? {
        return resourcePacks.firstNotNullOfOrNull { it.getResourceStream(path) }
    }
    
}