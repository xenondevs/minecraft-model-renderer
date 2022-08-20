package xyz.xenondevs.renderer.model.resource

import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

internal interface ResourcePack {
    
    fun getResourceStream(path: String): InputStream?
    
}

internal class ZipResourcePack(file: File) : ResourcePack {
    
    private val zipFile = ZipFile(file)
    
    override fun getResourceStream(path: String): InputStream? =
        zipFile.getEntry(path)?.let { zipFile.getInputStream(it) }
    
}

internal class DirectoryResourcePack(private val dir: File) : ResourcePack {
    
    override fun getResourceStream(path: String): InputStream? =
        File(dir, path).takeIf(File::exists)?.inputStream()
    
}

internal object InternalResourcePack : ResourcePack {
    
    override fun getResourceStream(path: String): InputStream? =
        ResourceLoader::class.java.getResourceAsStream("/assets/$path")
    
}