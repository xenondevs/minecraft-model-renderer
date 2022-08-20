package xyz.xenondevs.renderer.vector

import xyz.xenondevs.renderer.model.Axis
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal typealias Point3d = Vector3d

internal data class Vector3d(val x: Double, val y: Double, val z: Double) {
    
    constructor(x: Int, y: Int, z: Int) : this(x.toDouble(), y.toDouble(), z.toDouble())
    
    constructor(all: Double) : this(all, all, all)
    
    constructor(all: Int) : this(all.toDouble())
    
    operator fun plus(other: Vector3d): Vector3d {
        return Vector3d(x + other.x, y + other.y, z + other.z)
    }
    
    operator fun plus(other: Double): Vector3d {
        return Vector3d(x + other, y + other, z + other)
    }
    
    fun addX(x: Double): Vector3d {
        return Vector3d(this.x + x, y, z)
    }
    
    fun addY(y: Double): Vector3d {
        return Vector3d(x, this.y + y, z)
    }
    
    fun addZ(z: Double): Vector3d {
        return Vector3d(x, y, this.z + z)
    }
    
    fun addXZ(x: Double, z: Double): Vector3d {
        return Vector3d(this.x + x, y, this.z + z)
    }
    
    fun addYZ(y: Double, z: Double): Vector3d {
        return Vector3d(x, this.y + y, this.z + z)
    }
    
    fun addXY(x: Double, y: Double): Vector3d {
        return Vector3d(this.x + x, this.y + y, z)
    }
    
    operator fun minus(other: Vector3d): Vector3d {
        return Vector3d(x - other.x, y - other.y, z - other.z)
    }
    
    operator fun times(other: Vector3d): Vector3d {
        return Vector3d(x * other.x, y * other.y, z * other.z)
    }
    
    operator fun div(other: Vector3d): Vector3d {
        return Vector3d(x / other.x, y / other.y, z / other.z)
    }
    
    operator fun times(other: Double): Vector3d {
        return Vector3d(x * other, y * other, z * other)
    }
    
    operator fun times(other: Int): Vector3d {
        return Vector3d(x * other, y * other, z * other)
    }
    
    operator fun div(other: Double): Vector3d {
        return Vector3d(x / other, y / other, z / other)
    }
    
    operator fun div(other: Int): Vector3d {
        return Vector3d(x / other, y / other, z / other)
    }
    
    operator fun unaryMinus(): Vector3d {
        return Vector3d(-x, -y, -z)
    }
    
    operator fun unaryPlus(): Vector3d {
        return Vector3d(x, y, z)
    }
    
    fun length(): Double {
        return sqrt(lengthSquared())
    }
    
    fun lengthSquared(): Double {
        return x * x + y * y + z * z
    }
    
    fun normalize(): Vector3d {
        return this / length()
    }
    
    fun dot(other: Vector3d): Double {
        return x * other.x + y * other.y + z * other.z
    }
    
    fun cross(other: Vector3d): Vector3d {
        return Vector3d(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
    }
    
    inline infix fun x(other: Vector3d): Vector3d {
        return cross(other)
    }
    
    fun distance(other: Vector3d): Double {
        return (this - other).length()
    }
    
    fun distanceSquared(other: Vector3d): Double {
        return (this - other).lengthSquared()
    }
    
    fun rotate(origin: Vector3d, axis: Axis, degrees: Double): Vector3d {
        val radians = Math.toRadians(degrees)
        
        val x = this.x - origin.x
        val y = this.y - origin.y
        val z = this.z - origin.z
        
        when (axis) {
            Axis.X -> {
                val angle = atan2(z, y) + radians
                val h = sqrt(y * y + z * z)
                return Vector3d(x + origin.x, h * cos(angle) + origin.y, h * sin(angle) + origin.z)
            }
            
            Axis.Y -> {
                val angle = atan2(x, z) + radians
                val h = sqrt(x * x + z * z)
                return Vector3d(h * sin(angle) + origin.x, y + origin.y, h * cos(angle) + origin.z)
            }
            
            Axis.Z -> {
                val angle = atan2(y, x) + radians
                val h = sqrt(x * x + y * y)
                return Vector3d(h * cos(angle) + origin.x, h * sin(angle) + origin.y, z + origin.z)
            }
        }
    }
    
    fun toDoubleArray(): DoubleArray {
        return doubleArrayOf(x, y, z)
    }
    
    override fun toString(): String {
        return "($x, $y, $z)"
    }
    
    companion object {
        
        val HALF = Vector3d(.5, .5, .5)
        val ONE = Vector3d(1.0, 1.0, 1.0)
        val ZERO = Vector3d(0.0, 0.0, 0.0)
        
    }
    
}