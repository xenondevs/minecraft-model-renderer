package xyz.xenondevs.renderer.model.resource

import xyz.xenondevs.renderer.model.cache.ModelCache
import xyz.xenondevs.renderer.model.cache.TextureCache
import java.io.File
import java.io.InputStream

internal class ResourceLoader(
    packs: List<File>,
    useInternalResources: Boolean
) {
    
    private val resourcePacks = buildList {
        if (useInternalResources)
            add(InternalResourcePack)
        
        addAll(packs.map {
            when {
                it.isDirectory -> DirectoryResourcePack(it)
                it.extension.equals("zip", true) -> ZipResourcePack(it)
                else -> throw IllegalArgumentException("Unsupported resource pack format: ${it.extension}")
            }
        })
    }
    
    val modelCache = ModelCache(this)
    val textureCache = TextureCache(this)
    
    fun getModelStream(id: ResourceId): InputStream {
        return getStream("assets/${id.namespace}/models/${id.path}.json")
            ?: throw IllegalArgumentException("Model not found: $id")
    }
    
    fun getTextureStream(id: ResourceId): InputStream {
        return getStream("assets/${id.namespace}/textures/${id.path}.png")
            ?: throw IllegalArgumentException("Texture not found: $id")
    }
    
    private fun getStream(path: String): InputStream? {
        return resourcePacks.firstNotNullOfOrNull { it.getResourceStream(path) }
    }
    
}