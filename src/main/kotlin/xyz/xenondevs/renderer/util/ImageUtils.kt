package xyz.xenondevs.renderer.util

import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

internal fun BufferedImage.getWithUV(fromX: Int, fromY: Int, toX: Int, toY: Int): BufferedImage {
    val width = abs(toX - fromX)
    val height = abs(toY - fromY)
    val image = BufferedImage(width.coerceAtLeast(1), height.coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB)
    
    val stepX = if (fromX < toX) 1 else -1
    val stepY = if (fromY < toY) 1 else -1
    
    var curX = min(fromX, max(toX, fromX) - 1)
    var curY = min(fromY, max(toY, fromY) - 1)
    
    repeat(width) { x ->
        repeat(height) { y ->
            image.setRGB(x, y, getRGB(curX, curY))
            curY += stepY
        }
        curX += stepX
        curY = min(fromY, max(toY, fromY) - 1)
    }
    
    return image
}

internal fun BufferedImage.rotate(angle: Double): BufferedImage {
    val rads = Math.toRadians(angle)
    val sin = abs(sin(rads))
    val cos = abs(cos(rads))
    val newWidth = floor(width * cos + height * sin).toInt()
    val newHeight = floor(height * cos + width * sin).toInt()
    val rotated = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
    val g = rotated.createGraphics()
    val at = AffineTransform()
    at.translate(((newWidth - width) / 2.0), ((newHeight - height) / 2.0))
    val x = width / 2.0
    val y = height / 2.0
    at.rotate(rads, x, y)
    g.transform = at
    g.drawImage(this, 0, 0, null)
    g.dispose()
    return rotated
}

internal fun BufferedImage.scale(newWidth: Int, newHeight: Int): BufferedImage {
    val resized = BufferedImage(newWidth, newHeight, type)
    val g = resized.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.drawImage(this, 0, 0, newWidth, newHeight, 0, 0, width, height, null)
    g.dispose()
    
    return resized
}

internal fun BufferedImage.translate(x: Int, y: Int): BufferedImage {
    val translated = BufferedImage(width, height, type)
    val g = translated.createGraphics()
    g.drawImage(this, x, y, null)
    g.dispose()
    
    return translated
}