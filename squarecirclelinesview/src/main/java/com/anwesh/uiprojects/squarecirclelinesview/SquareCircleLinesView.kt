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
val parts : Int = 2
val backColor : Int = Color.parseColor("#212121")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

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

class SquareCircleLinesView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, parts)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb  : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SCLNode(var i : Int, val state : State = State()) {

        private var next : SCLNode? = null
        private var prev : SCLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SCLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSCLNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SCLNode {
            var curr : SCLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SquareCircleLines(var i : Int) {

        private val root : SCLNode = SCLNode(0)
        private var curr : SCLNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SquareCircleLinesView) {

        private val animator : Animator = Animator(view)
        private val scl : SquareCircleLines = SquareCircleLines(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            scl.draw(canvas, paint)
            animator.animate {
                scl.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            scl.startUpdating {
                animator.start()
            }
        }
    }
}
