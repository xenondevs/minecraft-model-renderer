package xyz.xenondevs.renderer.model.cache

import com.google.gson.JsonObject
import xyz.xenondevs.renderer.model.UnresolvedModel
import xyz.xenondevs.renderer.model.resource.ResourceId
import xyz.xenondevs.renderer.model.resource.ResourceLoader
import xyz.xenondevs.renderer.util.parseJson

internal class ModelCache(private val loader: ResourceLoader) {
    
    private val models = HashMap<ResourceId, UnresolvedModel>()
    
    fun get(id: String): UnresolvedModel =
        get(ResourceId.of(id))
    
    fun get(id: ResourceId): UnresolvedModel {
        return models.getOrPut(id) {
            UnresolvedModel(id, loader, loader.getModelStream(id).parseJson() as JsonObject)
        }
    }
    
}