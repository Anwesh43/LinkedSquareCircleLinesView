package com.anwesh.uiprojects.squarecirclelinesview

/**
 * Created by anweshmishra on 08/02/19.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5
val lines : Int = 3
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val sizeFactor : Float = 2.9f
val strokeFactor : Int = 90
val circleColor : Int = Color.parseColor("#01579B")
val lineColor : Int = Color.parseColor("#00C853")
val sqColor : Int = Color.parseColor("#FFAB00")
val backColor : Int = Color.parseColor("#212121")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Int, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawSquare(size : Float, paint : Paint) {
    paint.color = sqColor
    drawRect(RectF(-size, -size, size, size), paint)
}

fun Canvas.drawLine(x : Float, size : Float, paint : Paint) {
    paint.color = lineColor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(x, 0f)
    drawLine(0f, 0f, size, 0f, paint)
    restore()
}

fun Canvas.drawCircle(r : Float, paint : Paint) {
    paint.color = circleColor
    drawCircle(0f, 0f, r, paint)
}

fun Canvas.drawBiLine(x : Float, y : Float, size : Float, paint : Paint) {
    for (j in 0..1) {
        save()
        translate(0f, y)
        scale(1f - 2 * j, 1f)
        drawLine(x, size, paint)
        restore()
    }
}

fun Canvas.drawSCLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val r : Float = size / 3
    val lx : Float = size / 2
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val sc21 : Float = sc2.divideScale(0, 2)
    val sc22 : Float = sc2.divideScale(1, 2)
    val yGap : Float = (2 * size) / lines
    save()
    translate(w/2, gap * (i + 1))
    rotate(90f * sc22)
    drawSquare(size * sc21, paint)
    drawCircle(r * sc1, paint)
    for (j in 0..(lines - 1)) {
        val sc : Float = sc1.divideScale(j, lines)
        drawBiLine(lx, yGap * j, r * sc, paint)
    }
    restore()
}
