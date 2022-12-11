package xyz.xenondevs.renderer.vector

import org.joml.Vector3f
import xyz.xenondevs.renderer.model.Axis
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

typealias Point3f = Vector3f

fun Vector3f(x: Double, y: Double, z: Double) = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

fun Vector3f(all: Double) = Vector3f(all.toFloat())

fun Vector3f(x: Int, y: Int, z: Int) = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

fun Vector3f(all: Int) = Vector3f(all.toFloat())

inline operator fun Vector3f.plus(other: Vector3f): Vector3f {
    return Vector3f(this).add(other)
}

inline operator fun Vector3f.plus(float: Float): Vector3f {
    return Vector3f(this).add(float, float, float)
}

inline operator fun Vector3f.plusAssign(other: Vector3f) {
    add(other)
}

inline operator fun Vector3f.minus(other: Vector3f): Vector3f {
    return Vector3f(this).sub(other)
}

inline operator fun Vector3f.minus(float: Float): Vector3f {
    return Vector3f(this).sub(float, float, float)
}

inline operator fun Vector3f.minusAssign(other: Vector3f) {
    sub(other)
}

inline operator fun Vector3f.times(other: Vector3f): Vector3f {
    return Vector3f(this).mul(other)
}

inline operator fun Vector3f.times(float: Float): Vector3f {
    return Vector3f(this).mul(float)
}

inline operator fun Vector3f.timesAssign(other: Vector3f) {
    mul(other)
}

inline fun Vector3f.divided(other: Vector3f): Vector3f {
    return Vector3f(this).div(other)
}

inline fun Vector3f.divided(float: Float): Vector3f {
    return Vector3f(this).div(float)
}

inline operator fun Vector3f.divAssign(other: Vector3f) {
    div(other)
}

inline operator fun Vector3f.unaryMinus(): Vector3f {
    return Vector3f(this).negate()
}

inline operator fun Vector3f.unaryPlus(): Vector3f {
    return Vector3f(this)
}

inline fun Vector3f.crossed(other: Vector3f): Vector3f {
    return Vector3f(this).cross(other)
}

fun Vector3f.addX(x: Float): Vector3f {
    return Vector3f(this.x + x, y, z)
}

fun Vector3f.addY(y: Float): Vector3f {
    return Vector3f(x, this.y + y, z)
}

fun Vector3f.addZ(z: Float): Vector3f {
    return Vector3f(x, y, this.z + z)
}

fun Vector3f.addXZ(x: Float, z: Float): Vector3f {
    return Vector3f(this.x + x, y, this.z + z)
}

fun Vector3f.addYZ(y: Float, z: Float): Vector3f {
    return Vector3f(x, this.y + y, this.z + z)
}

fun Vector3f.addXY(x: Float, y: Float): Vector3f {
    return Vector3f(this.x + x, this.y + y, z)
}

fun Vector3f.normalized(): Vector3f {
    return this.normalize(Vector3f(0f, 0f, 0f))
}

fun Vector3f.toFloatArray(): FloatArray = floatArrayOf(x, y, z)

operator fun Vector3f.component1() = x

operator fun Vector3f.component2() = y

operator fun Vector3f.component3() = z

internal fun Vector3f.rotate(origin: Vector3f, axis: Axis, degrees: Float): Vector3f {
    val radians = Math.toRadians(degrees.toDouble())
    
    val x = this.x - origin.x
    val y = this.y - origin.y
    val z = this.z - origin.z
    
    when (axis) {
        Axis.X -> {
            val angle = atan2(z, y) + radians
            val h = sqrt(y * y + z * z)
            return Vector3f(x + origin.x, (h * cos(angle) + origin.y).toFloat(), (h * sin(angle) + origin.z).toFloat())
        }
        
        Axis.Y -> {
            val angle = atan2(x, z) + radians
            val h = sqrt(x * x + z * z)
            return Vector3f((h * sin(angle) + origin.x).toFloat(), y + origin.y, (h * cos(angle) + origin.z).toFloat())
        }
        
        Axis.Z -> {
            val angle = atan2(y, x) + radians
            val h = sqrt(x * x + y * y)
            return Vector3f((h * cos(angle) + origin.x).toFloat(), (h * sin(angle) + origin.y).toFloat(), z + origin.z)
        }
    }
}