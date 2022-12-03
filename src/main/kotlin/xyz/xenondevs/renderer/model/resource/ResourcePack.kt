package xyz.xenondevs.renderer.model.resource

import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream

interface ResourcePack {
    
    fun getResourceStream(path: String): InputStream?
    
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
        ResourceLoader::class.java.getResourceAsStream("/$path")
    
}