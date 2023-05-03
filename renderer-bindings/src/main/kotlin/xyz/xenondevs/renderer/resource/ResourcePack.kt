package xyz.xenondevs.renderer.resource

import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream

interface ResourcePack {
    
    fun getResourceStream(path: String): InputStream?
    
    fun getResourceBytes(path: String): ByteArray =
        getResourceStream(path)?.readAllBytes() ?: byteArrayOf()
    
}

class ZipResourcePack(file: Path) : ResourcePack {
    
    private val fs = FileSystems.newFileSystem(file)
    
    override fun getResourceStream(path: String): InputStream? =
        fs.getPath(path).takeIf(Path::exists)?.inputStream()
    
}

internal class DirectoryResourcePack(private val dir: Path) : ResourcePack {
    
    override fun getResourceStream(path: String): InputStream? =
        dir.resolve(path).takeIf(Path::exists)?.inputStream()
    
}

internal object InternalResourcePack : ResourcePack {
    
    override fun getResourceStream(path: String): InputStream? =
        ResourcePack::class.java.getResourceAsStream("/$path")
    
}