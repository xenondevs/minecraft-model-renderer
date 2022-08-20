package xyz.xenondevs.renderer.scene.geometry

import xyz.xenondevs.renderer.scene.Scene
import xyz.xenondevs.renderer.scene.camera.Intersection
import xyz.xenondevs.renderer.scene.camera.Ray

internal interface GeometryObject {
    
    val scene: Scene
    
    fun trace(ray: Ray): Intersection?
    
}