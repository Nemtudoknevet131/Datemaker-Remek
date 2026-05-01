package com.example.datemaker.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import com.example.datemaker.utils.AdPopupManager
import kotlin.math.*

class TankTroubleGameView(context: Context) : View(context) {

    private val paint = Paint().apply { isAntiAlias = true }
    private val wallPaint = Paint().apply { 
        color = Color.DKGRAY 
        strokeWidth = 5f
    }
    private val bulletPaint = Paint().apply { color = Color.BLACK }

    private val buttonPaint = Paint().apply { 
        color = Color.DKGRAY 
        style = Paint.Style.FILL
        alpha = 150
    }
    private val buttonTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val hpPaint = Paint().apply {
        color = Color.BLACK
        textSize = 50f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    private val gameOverPaint = Paint().apply {
        color = Color.WHITE
        textSize = 100f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    private var p1ButtonRect = RectF()
    private var p2ButtonRect = RectF()
    private var gameOver = false
    private var adShown = false
    private var winner: Int? = null

    // Game Objects
    private data class Tank(var x: Float, var y: Float, var angle: Float, val color: Int, var hp: Int = 5)
    private data class Bullet(var x: Float, var y: Float, var vx: Float, var vy: Float, val owner: Int)
    private data class Wall(val rect: RectF)

    private val p1 = Tank(100f, 100f, 0f, Color.BLUE)
    private val p2 = Tank(800f, 800f, 180f, Color.RED)
    private val bullets = mutableListOf<Bullet>()
    private val walls = mutableListOf<Wall>()

    // Controls
    private lateinit var p1Joystick: Joystick
    private lateinit var p2Joystick: Joystick
    private var p1JoystickPointerId = -1
    private var p2JoystickPointerId = -1

    inner class Joystick(var cx: Float, var cy: Float, var radius: Float) {
        var actX = cx
        var actY = cy
        var isPressed = false

        fun update(x: Float, y: Float) {
            val dx = x - cx
            val dy = y - cy
            val dist = sqrt(dx * dx + dy * dy)

            if (dist <= radius) {
                actX = x
                actY = y
            } else {
                val ratio = radius / dist
                actX = cx + dx * ratio
                actY = cy + dy * ratio
            }
        }

        fun reset() {
            actX = cx
            actY = cy
            isPressed = false
        }

        fun getAngle(): Float {
            return atan2(actY - cy, actX - cx) * 180 / PI.toFloat()
        }

        fun getStrength(): Float {
            val dx = actX - cx
            val dy = actY - cy
            return min(1f, sqrt(dx * dx + dy * dy) / radius)
        }
    }

    private val tankSize = 60f
    private val bulletSpeed = 15f
    private val tankSpeed = 5f

    init {
        // Walls will be generated in onSizeChanged
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        val btnSize = 150f
        val margin = 50f
        
        // P1 Button (Bottom Left)
        p1ButtonRect.set(margin, h - btnSize - margin, margin + btnSize, h - margin)
        
        // P2 Button (Top Right)
        p2ButtonRect.set(w - btnSize - margin, margin, w - margin, margin + btnSize)

        // Joysticks
        val joyRadius = 120f
        // P1 Joystick (Bottom Right)
        p1Joystick = Joystick(w - margin - joyRadius, h - margin - joyRadius, joyRadius)

        // P2 Joystick (Top Left)
        p2Joystick = Joystick(margin + joyRadius, margin + joyRadius, joyRadius)

        // Reset Tanks to safe positions
        p1.x = w / 2f
        p1.y = h * 0.85f
        p1.angle = 270f

        p2.x = w / 2f
        p2.y = h * 0.15f
        p2.angle = 90f

        generateRandomMap(w, h)
    }

    private fun generateRandomMap(w: Int, h: Int) {
        walls.clear()
        val cx = w / 2f
        val cy = h / 2f
        val mapType = (0..2).random()
        val size = min(w, h) * 0.25f
        val thick = 20f

        when (mapType) {
            0 -> { // Plus Shape
                walls.add(Wall(RectF(cx - thick, cy - size, cx + thick, cy + size)))
                walls.add(Wall(RectF(cx - size, cy - thick, cx + size, cy + thick)))
            }
            1 -> { // Hollow Box
                walls.add(Wall(RectF(cx - size, cy - size, cx + size, cy - size + thick))) // Top
                walls.add(Wall(RectF(cx - size, cy + size - thick, cx + size, cy + size))) // Bottom
                walls.add(Wall(RectF(cx - size, cy - size, cx - size + thick, cy + size))) // Left
                walls.add(Wall(RectF(cx + size - thick, cy - size, cx + size, cy + size))) // Right
            }
            2 -> { // Parallel Bars
                walls.add(Wall(RectF(cx - size * 1.5f, cy - size/2, cx + size * 1.5f, cy - size/2 + thick)))
                walls.add(Wall(RectF(cx - size * 1.5f, cy + size/2, cx + size * 1.5f, cy + size/2 + thick)))
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw split background
        paint.color = Color.rgb(255, 220, 220) // Light Red for Top (P2)
        canvas.drawRect(0f, 0f, width.toFloat(), height / 2f, paint)

        paint.color = Color.rgb(220, 220, 255) // Light Blue for Bottom (P1)
        canvas.drawRect(0f, height / 2f, width.toFloat(), height.toFloat(), paint)

        if (!gameOver) {
            updateGame()
        }

        // Draw Walls
        for (wall in walls) {
            canvas.drawRect(wall.rect, wallPaint)
        }

        // Draw Tanks
        drawTank(canvas, p1)
        drawTank(canvas, p2)

        // Draw Bullets
        for (b in bullets) {
            canvas.drawCircle(b.x, b.y, 8f, bulletPaint)
        }

        // Draw Buttons
        canvas.drawRoundRect(p1ButtonRect, 20f, 20f, buttonPaint)
        canvas.drawText("SHOOT", p1ButtonRect.centerX(), p1ButtonRect.centerY() + 15f, buttonTextPaint)

        canvas.save()
        canvas.rotate(180f, p2ButtonRect.centerX(), p2ButtonRect.centerY())
        canvas.drawRoundRect(p2ButtonRect, 20f, 20f, buttonPaint)
        canvas.drawText("SHOOT", p2ButtonRect.centerX(), p2ButtonRect.centerY() + 15f, buttonTextPaint)
        canvas.restore()

        // Draw Joysticks
        drawJoystick(canvas, p1Joystick)
        drawJoystick(canvas, p2Joystick)

        // Draw HP
        canvas.drawText("HP: ${p1.hp}", width - 150f, height - 250f, hpPaint)
        
        canvas.save()
        canvas.rotate(180f, 150f, 250f)
        canvas.drawText("HP: ${p2.hp}", 150f, 250f, hpPaint)
        canvas.restore()

        // Draw Game Over
        if (gameOver) {
            paint.color = Color.argb(200, 0, 0, 0)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            
            val winnerText = when(winner) {
                1 -> "Blue Wins!"
                2 -> "Red Wins!"
                else -> "Game Over"
            }
            canvas.drawText(winnerText, width / 2f, height / 2f - 100f, gameOverPaint)
            
            gameOverPaint.textSize = 60f
            canvas.drawText("Tap to Restart", width / 2f, height / 2f + 50f, gameOverPaint)
            gameOverPaint.textSize = 100f
        }

        invalidate()
    }

    private fun drawJoystick(canvas: Canvas, joystick: Joystick) {
        paint.color = Color.argb(100, 100, 100, 100) // Base
        canvas.drawCircle(joystick.cx, joystick.cy, joystick.radius, paint)

        paint.color = Color.argb(200, 50, 50, 50) // Knob
        canvas.drawCircle(joystick.actX, joystick.actY, joystick.radius / 2, paint)
    }

    private fun drawTank(canvas: Canvas, tank: Tank) {
        paint.color = tank.color
        
        canvas.save()
        canvas.rotate(tank.angle, tank.x, tank.y)
        canvas.drawRect(tank.x - tankSize/2, tank.y - tankSize/2, tank.x + tankSize/2, tank.y + tankSize/2, paint)
        // Barrel
        canvas.drawRect(tank.x, tank.y - 5f, tank.x + tankSize, tank.y + 5f, paint)
        canvas.restore()
    }

    private fun updateGame() {
        if (p1Joystick.isPressed) {
            moveTank(p1, p1Joystick.getAngle(), p1Joystick.getStrength())
        }
        if (p2Joystick.isPressed) {
            moveTank(p2, p2Joystick.getAngle(), p2Joystick.getStrength())
        }
        updateBullets()
    }

    private fun moveTank(tank: Tank, angle: Float, strength: Float) {
        if (strength < 0.1f) return

        tank.angle = angle
        val speed = tankSpeed // Constant speed

        val nextX = tank.x + cos(angle * PI.toFloat() / 180) * speed
        val nextY = tank.y + sin(angle * PI.toFloat() / 180) * speed

        if (!checkWallCollision(nextX.toFloat(), nextY.toFloat())) {
            tank.x = nextX.toFloat()
            tank.y = nextY.toFloat()
        }
    }

    private fun checkWallCollision(x: Float, y: Float): Boolean {
        // Screen bounds
        if (x < 0 || x > width || y < 0 || y > height) return true
        
        // Walls
        val r = tankSize/2
        for (w in walls) {
            if (RectF(x-r, y-r, x+r, y+r).intersect(w.rect)) return true
        }
        return false
    }

    private fun updateBullets() {
        val iterator = bullets.iterator()
        while (iterator.hasNext()) {
            val b = iterator.next()
            b.x += b.vx
            b.y += b.vy

            // Bounce off walls (simplified)
            if (b.x < 0 || b.x > width) b.vx *= -1
            if (b.y < 0 || b.y > height) b.vy *= -1
            
            for (w in walls) {
                if (w.rect.contains(b.x, b.y)) {
                    // Simple bounce logic
                    if (b.x < w.rect.left || b.x > w.rect.right) b.vx *= -1
                    else b.vy *= -1
                }
            }
            
            // Hit Tank?
             if (dist(b.x, b.y, p1.x, p1.y) < tankSize/2) {
                 p1.hp--
                 iterator.remove()
                 if (p1.hp <= 0) {
                     gameOver = true
                     winner = 2
                     checkAd()
                 }
             }
             else if (dist(b.x, b.y, p2.x, p2.y) < tankSize/2) {
                 p2.hp--
                 iterator.remove()
                 if (p2.hp <= 0) {
                     gameOver = true
                     winner = 1
                     checkAd()
                 }
             }
        }
    }

    private fun checkAd() {
        if (!adShown) {
            adShown = true
            post {
                AdPopupManager.onGameEnd(context) {
                    // Double money logic here (if applicable)
                }
            }
        }
    }
    
    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1))
    }

    private fun shoot(tank: Tank, owner: Int) {
        val bx = tank.x + cos(tank.angle * PI.toFloat() / 180) * (tankSize + 10)
        val by = tank.y + sin(tank.angle * PI.toFloat() / 180) * (tankSize + 10)
        val vx = cos(tank.angle * PI.toFloat() / 180) * bulletSpeed
        val vy = sin(tank.angle * PI.toFloat() / 180) * bulletSpeed
        bullets.add(Bullet(bx.toFloat(), by.toFloat(), vx.toFloat(), vy.toFloat(), owner))
    }

    private fun restartGame() {
        p1.hp = 5
        p2.hp = 5
        gameOver = false
        adShown = false
        winner = null
        bullets.clear()
        p1.x = width / 2f
        p1.y = height * 0.85f
        p2.x = width / 2f
        p2.y = height * 0.15f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                restartGame()
            }
            return true
        }

        val action = event.actionMasked
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val x = event.getX(index)
        val y = event.getY(index)

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // Check Buttons
                if (p1ButtonRect.contains(x, y)) {
                    shoot(p1, 1)
                } else if (p2ButtonRect.contains(x, y)) {
                    shoot(p2, 2)
                }
                // Check Joysticks
                else if (dist(x, y, p1Joystick.cx, p1Joystick.cy) < p1Joystick.radius * 2) {
                    p1JoystickPointerId = id
                    p1Joystick.isPressed = true
                    p1Joystick.update(x, y)
                } else if (dist(x, y, p2Joystick.cx, p2Joystick.cy) < p2Joystick.radius * 2) {
                    p2JoystickPointerId = id
                    p2Joystick.isPressed = true
                    p2Joystick.update(x, y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pid = event.getPointerId(i)
                    val px = event.getX(i)
                    val py = event.getY(i)

                    if (pid == p1JoystickPointerId) {
                        p1Joystick.update(px, py)
                    }
                    if (pid == p2JoystickPointerId) {
                        p2Joystick.update(px, py)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (id == p1JoystickPointerId) {
                    p1Joystick.reset()
                    p1JoystickPointerId = -1
                }
                if (id == p2JoystickPointerId) {
                    p2Joystick.reset()
                    p2JoystickPointerId = -1
                }
            }
        }
        return true
    }
}
