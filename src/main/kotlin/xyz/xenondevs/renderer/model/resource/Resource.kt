package xyz.xenondevs.renderer.model.resource

private val NAMESPACED_ENTRY = Regex("""^(\w*):([\w/]*)$""")
private val NON_NAMESPACED_ENTRY = Regex("""^([\w/]*)$""")

internal data class ResourceId(val namespace: String, val path: String) {
    
    private val id = "$namespace:$path"
    
    override fun equals(other: Any?): Boolean {
        return other is ResourceId && other.id == id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return id
    }
    
    companion object {
        
        fun of(id: String, fallbackNamespace: String = "minecraft"): ResourceId {
            return if (NON_NAMESPACED_ENTRY.matches(id)) {
                ResourceId(fallbackNamespace, id)
            } else {
                val match = NAMESPACED_ENTRY.matchEntire(id)
                    ?: throw IllegalArgumentException("Invalid resource id: $id")
                
                ResourceId(match.groupValues[1], match.groupValues[2])
            }
        }
        
    }
    
}