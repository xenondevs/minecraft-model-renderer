package xyz.xenondevs.renderer.model

import com.google.gson.JsonObject
import xyz.xenondevs.renderer.model.cache.TextureCache
import xyz.xenondevs.renderer.model.resource.ResourceId
import xyz.xenondevs.renderer.model.resource.ResourceLoader
import xyz.xenondevs.renderer.util.getAllDoubles
import xyz.xenondevs.renderer.util.getBoolean
import xyz.xenondevs.renderer.util.getDouble
import xyz.xenondevs.renderer.util.getInt
import xyz.xenondevs.renderer.util.getOrNull
import xyz.xenondevs.renderer.util.getString
import xyz.xenondevs.renderer.vector.Vector3d
import java.awt.image.BufferedImage

private fun JsonObject.getVector3d(name: String): Vector3d {
    val values = getAsJsonArray(name).getAllDoubles()
    return Vector3d(values[0], values[1], values[2])
}

private fun JsonObject.getVector3dOrNull(name: String): Vector3d? {
    val values = getOrNull(name)?.asJsonArray?.getAllDoubles() ?: return null
    return Vector3d(values[0], values[1], values[2])
}

internal class UnresolvedModel(id: ResourceId, val loader: ResourceLoader, obj: JsonObject) {
    
    private val parent: UnresolvedModel? = obj.getString("parent")?.let(loader.modelCache::get) ?: if (id.path != "block/block") loader.modelCache.get("block/block") else null
    private val textureNames: Map<String, String> = obj.getOrNull("textures")?.asJsonObject?.entrySet()?.associate { it.key to it.value.asString } ?: emptyMap()
    private val elements: List<UnresolvedElement> = obj.getOrNull("elements")?.asJsonArray?.map { UnresolvedElement(this, it.asJsonObject) } ?: emptyList()
    
    val ambientOcclusion: Boolean?
    val rotation: Vector3d?
    val translation: Vector3d?
    val scale: Vector3d?
    
    init {
        val guiObject = obj.getOrNull("display")?.asJsonObject?.getOrNull("gui")?.asJsonObject
        rotation = guiObject?.getVector3dOrNull("rotation")
        translation = guiObject?.getVector3dOrNull("translation")
        scale = guiObject?.getVector3dOrNull("scale")
        ambientOcclusion = obj.getBoolean("ambientocclusion")
    }
    
    @Suppress("NAME_SHADOWING")
    fun resolve(): Model {
        var elements: List<UnresolvedElement>? = null
        var textures = HashMap<String, String>()
        
        var ambientOcclusion: Boolean? = null
        var rotation: Vector3d? = this.rotation
        var translation: Vector3d? = this.translation
        var scale: Vector3d? = this.scale
        
        var current: UnresolvedModel? = this
        while (current != null) {
            if (elements == null) {
                if (current.elements.isNotEmpty())
                    elements = current.elements
                
            }
            current.textureNames.forEach { (key, value) ->
                if (key !in textures) textures[key] = value
            }
            
            if (rotation == null && translation == null && scale == null) {
                rotation = current.rotation
                translation = current.translation
                scale = current.scale
            }
            
            if (ambientOcclusion == null)
                ambientOcclusion = current.ambientOcclusion
            
            current = current.parent
        }
        
        textures = textures.entries.associateTo(HashMap()) { (key, value) ->
            var value = value
            while (value.startsWith('#')) {
                value = textures[value.substring(1)] ?: break
            }
            
            key to value
        }
        
        val resolvedElements = elements?.map { it.resolve(textures) } ?: emptyList()
        return Model(
            resolvedElements,
            ambientOcclusion ?: true,
            rotation ?: Vector3d.ZERO,
            translation ?: Vector3d.ZERO, 
            scale ?: Vector3d.ONE
        )
    }
    
}

internal class UnresolvedElement(val model: UnresolvedModel, obj: JsonObject) {
    
    val from: Vector3d = obj.getVector3d("from") / 16.0
    val to: Vector3d = obj.getVector3d("to") / 16.0
    
    private val rotation: ElementRotation?
    
    private val faces: Map<Direction, UnresolvedTexture> = obj.getOrNull("faces")?.asJsonObject?.entrySet()
        ?.associate {
            val direction = Direction.valueOf(it.key.uppercase())
            return@associate direction to UnresolvedTexture(this@UnresolvedElement, direction, it.value.asJsonObject)
        } ?: emptyMap()
    
    init {
        rotation = obj.getOrNull("rotation")?.asJsonObject?.let {
            ElementRotation(
                it.getVector3d("origin") / 16.0,
                Axis.valueOf(it.getString("axis")!!.uppercase()),
                it.getDouble("angle")!!,
                it.getBoolean("rescale", false)
            )
        }
        
    }
    
    fun resolve(textures: Map<String, String>): Element {
        val faceTextures = faces.mapValues { it.value.resolve(textures) }.toMutableMap()
        Direction.values().forEach {
            if (!faceTextures.containsKey(it))
                faceTextures[it] = TextureCache.EMPTY_TEXTURE
        }
        
        return Element(
            from,
            to,
            rotation,
            faceTextures
        )
    }
    
}

internal class UnresolvedTexture(val element: UnresolvedElement, direction: Direction, obj: JsonObject) {
    
    private val texture: String = obj.getString("texture")!!.removePrefix("#")
    private val rotation: Int = obj.getInt("rotation") ?: 0
    private val fromX: Double
    private val fromY: Double
    private val toX: Double
    private val toY: Double
    
    init {
        val uv = obj.getOrNull("uv")?.asJsonArray?.getAllDoubles()
        if (uv != null) {
            fromX = uv[0] / 16.0
            fromY = uv[1] / 16.0
            toX = uv[2] / 16.0
            toY = uv[3] / 16.0
        } else {
            val dynamicUV = getDynamicUV(element, direction)
            fromX = dynamicUV[0]
            fromY = dynamicUV[1]
            toX = dynamicUV[2]
            toY = dynamicUV[3]
        }
        
    }
    
    fun resolve(textures: Map<String, String>): BufferedImage {
        return element.model.loader.textureCache.get(textures[texture]!!, rotation, fromX, fromY, toX, toY)
    }
    
    private fun getDynamicUV(parent: UnresolvedElement, direction: Direction): DoubleArray {
        val fromPos = parent.from.toDoubleArray().map { it - 0.5 }
        val toPos = parent.to.toDoubleArray().map { it - 0.5 }
        
        val axisHor: Int // horizontal axis
        val axisVert: Int // vertical axis
        
        when (direction.axis) {
            Axis.X -> { // west or east
                axisHor = 2 // z
                axisVert = 1 // y
            }
            
            Axis.Y -> { // up or down
                axisHor = 0 // x
                axisVert = 2 // z
            }
            
            Axis.Z -> { // north or south
                axisHor = 0 // x
                axisVert = 1 // y
            }
        }
        
        var nx = -1
        var ny = -1
        if (direction == Direction.WEST || direction == Direction.SOUTH) {
            nx = 1
        } else if (direction == Direction.UP) {
            ny = 1
            nx = 1
        }
        
        val x = doubleArrayOf(fromPos[axisHor] * nx, toPos[axisHor] * nx).sorted()
        val y = doubleArrayOf(fromPos[axisVert] * ny, toPos[axisVert] * ny).sorted()
        
        return doubleArrayOf(x[0], y[0], x[1], y[1]).map { it + 0.5 }.toDoubleArray()
    }
    
}
